package com.compiler;

import java.util.List;
import static com.compiler.TokenType.*;

public class AnalizadorSemantico {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final FunctionTable functionTable;
    private static class ParseError extends RuntimeException {}

    public AnalizadorSemantico(List<Token> tokens, SymbolTable symbolTable, FunctionTable functionTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        this.functionTable = functionTable;
    }

    public void analyze() {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == VAR) {
                if (tokens.get(i + 1).type != IDENTIFIER) {
                    throw error(token, "Expect variable name after 'var'.");
                } else if(symbolTable.contains(tokens.get(i + 1).lexeme)){
                    throw error(token, "Variable already declared: " + tokens.get(i + 1).lexeme);
                }
                Token identifier = tokens.get(i + 1);
                Token valueToken = null;
                if (tokens.size() > i + 2 && tokens.get(i + 2).type == EQUAL) {
                    valueToken = tokens.get(i + 3);
                }
                symbolTable.put(identifier.lexeme, new SymbolTable.Symbol(identifier.lexeme, "IDENTIFIER", new SymbolTable.Value(identifier.lexeme, valueToken != null ? valueToken.literal : null, identifier.line)));
            } else if (token.type == PROGRAM) {
                if (tokens.get(i + 1).type != IDENTIFIER) {
                    throw error(token, "Expect program name after 'program'.");
                } else if(functionTable.contains(tokens.get(i + 1).lexeme)){
                    throw error(token, "Program already declared: " + tokens.get(i + 1).lexeme);
                }
                Token identifier = tokens.get(i + 1);
                i++;
                functionTable.put(identifier.lexeme, new SymbolTable.Symbol(identifier.lexeme, "PROGRAM", new SymbolTable.Value(identifier.lexeme, null, identifier.line)));
            } else if (token.type == IDENTIFIER) {
                if (!symbolTable.contains(token.lexeme)) {
                    throw error(token, "Undefined variable: " + token.lexeme);
                }else{
                    symbolTable.put(token.lexeme, new SymbolTable.Symbol(token.lexeme, "IDENTIFIER", new SymbolTable.Value(token.lexeme, null, token.line)));
                }
            }
        }
    }

    private ParseError error(Token token, String message) {
        Compilador.error(token, message);
        return new ParseError();
    }

}