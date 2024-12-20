package com.compiler;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Compilador {
    private static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Ingresa solo 1 argumento.");
            return;
        } else if (args.length == 1) {
            System.out.println("Ejecutando archivo: " + args[0]);
            runFile(args[0]);
        } else {
            String filePath = selectFile();
            if (filePath != null) {
                System.out.println("Ejecutando archivo: " + filePath);
                runFile(filePath);
            } else {
                System.out.println("No se seleccionó ningún archivo.");
            }
        }
    }

    private static String selectFile() {
        JFileChooser fileChooser = new JFileChooser("src/main/resources/programas");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getPath();
        }
        return null;
    }

    // Ejecuta un archivo de código fuente.
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void run(String source) throws IOException {
        AnalizadorLexico analizadorLexico = new AnalizadorLexico(source);
        List<Token> tokens = analizadorLexico.scanTokens();
        writeTokensToFile(tokens, "src/main/resources/tokens.txt");

        SymbolTable symbolTable = new SymbolTable();
        FunctionTable functionTable = new FunctionTable();
        AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico(tokens, symbolTable, functionTable);
        analizadorSemantico.analyze();

        VCI vci = new VCI(tokens);
        List<Token> vciTokens = vci.getVCI();
        writeVCIToFile(vciTokens, "src/main/resources/vci.txt");

        Execution execution = new Execution(symbolTable);
        execution.executeVCI(vciTokens);
        writeSymbolTableToFile(execution.getSymbolTable(), "src/main/resources/symbolTable.txt", execution.getFunctionTable());
        writeAddressTableToFile(functionTable, vciTokens, "src/main/resources/addressTable.txt");
    }

    private static void writeTokensToFile(List<Token> tokens, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            String format = "%-20s %-15s %-10s %-10s%n";
            writer.write(String.format(format, "Token", "Lexeme", "Literal", "Line"));
            writer.write(String.format(format, "--------------------", "---------------", "----------", "----------"));
            for (Token token : tokens) {
                String tokenType = truncate(token.type.toString(), 20);
                String lexeme = truncate(token.lexeme, 15);
                String literal = truncate(token.literal != null ? token.literal.toString() : "null", 10);
                writer.write(String.format(format, tokenType, lexeme, literal, token.line));
            }
        } catch (IOException e) {
            System.err.println("Error writing tokens to file: " + e.getMessage());
        }
    }

    private static String truncate(String value, int length) {
        if (value.length() > length) {
            return value.substring(0, length - 3) + "...";
        }
        return value;
    }

    private static void writeVCIToFile(List<Token> tokens, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("VCI: ");
            writer.newLine();
            writer.write("[");
            for (Token token : tokens) {
                writer.write(token.lexeme);
                if (token != tokens.get(tokens.size() - 1)){
                    writer.write(", ");
                }
            }
            writer.write("]");
        } catch (IOException e) {
            System.err.println("Error writing VCI to file: " + e.getMessage());
        }
    }

    private static void writeSymbolTableToFile(SymbolTable symbolTable, String fileName, FunctionTable functionTable) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            String format = "%-20s %-15s %-20s %-10s %-10s %-10s %-10s %-10s%n";
            writer.write(String.format(format, "ID", "Token Type", "Value", "D1", "D2", "PTR", "Scope", "Ambito"));
            writer.write(String.format(format, "--------------------", "---------------", "--------------------", "----------", "----------", "----------", "----------", "----------"));
            String ambito = "";
            for (Map.Entry<String, SymbolTable.Symbol> entry : functionTable.getTable().entrySet()) {
                ambito = entry.getKey();
            }

            for (Map.Entry<String, SymbolTable.Symbol> entry : symbolTable.getTable().entrySet()) {
                writer.write(String.format(format, entry.getKey(), entry.getValue().type, entry.getValue().value.getValue(), "0", "0", "null", "Scope", ambito));
            }
        } catch (IOException e) {
            System.err.println("Error writing symbol table to file: " + e.getMessage());
        }
    }

    private static void writeAddressTableToFile(FunctionTable functionTable, List<Token> vciTokens, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            String format = "%-20s %-15s %-10s %-10s%n";
            writer.write(String.format(format, "ID", "Token Type", "Line", "Address"));
            writer.write(String.format(format, "--------------------", "---------------", "----------", "----------"));
            for (Map.Entry<String, SymbolTable.Symbol> entry : functionTable.getTable().entrySet()) {
                String id = entry.getKey();
                String tokenType = entry.getValue().type;
                int line = entry.getValue().value.getLine();
                int address = findAddressInVCI(vciTokens, id);
                writer.write(String.format(format, id, tokenType, line, address));
            }
        } catch (IOException e) {
            System.err.println("Error writing address table to file: " + e.getMessage());
        }
    }

    private static int findAddressInVCI(List<Token> vciTokens, String id) {
        for (int i = 0; i < vciTokens.size(); i++) {
            if (vciTokens.get(i).lexeme.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static void error(int line, String s) {
        report(line, "", s);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message
        );
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
