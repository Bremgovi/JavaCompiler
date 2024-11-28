package com.compiler;

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
            System.out.println("Ejecutando shell interactivo.");
            runShell();
        }
    }

    // Ejecuta un archivo de código fuente.
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }
    // Ejecuta un shell interactivo.
    private static void runShell() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while(true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }


    private static void run(String source) throws IOException {
        AnalizadorLexico analizadorLexico = new AnalizadorLexico(source);
        List<Token> tokens = analizadorLexico.scanTokens();
        writeTokensToFile(tokens, "src/main/resources/tokens.txt");

        VCI vci = new VCI(tokens);
        List<Token> vciTokens = vci.getVCI();
        writeVCIToFile(vciTokens, "src/main/resources/vci.txt");

        Execution execution = new Execution();
        execution.executeVCI(vciTokens);
        writeSymbolTableToFile(execution.getSymbolTable(), "src/main/resources/symbolTable.txt", execution.getFunctionTable());

    }

    private static void writeTokensToFile(List<Token> tokens, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Token token : tokens) {
                writer.write(token.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing tokens to file: " + e.getMessage());
        }
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
            String ambito = "";
            for (Map.Entry<String, SymbolTable.Symbol> entry : functionTable.getTable().entrySet()) {
                ambito = entry.getKey();
            }

            for (Map.Entry<String, SymbolTable.Symbol> entry : symbolTable.getTable().entrySet()) {
                writer.write("ID: " + entry.getKey() + " TOKEN: " + entry.getValue().type + " VALOR: " + entry.getValue().value + " D1: 0 " + " D2: 0 " + " PTR: null " + " AMBITO: " + ambito);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error writing symbol table to file: " + e.getMessage());
        }
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
