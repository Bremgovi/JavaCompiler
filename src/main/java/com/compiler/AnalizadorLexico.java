package com.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.compiler.TokenType.*;

public class AnalizadorLexico {
    String sourceCode;
    List<Token> tokens = new ArrayList<>();
    int start = 0;
    int current = 0;
    int line = 1;
    private final static Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("if",     IF);
        keywords.put("null",    NULL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("input",  INPUT);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("program", PROGRAM);
        keywords.put("then", THEN);
        keywords.put("end", END);
    }

    public AnalizadorLexico(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    // Escanea los tokens de un código fuente.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // Verifica si se ha llegado al final del código fuente.
    private boolean isAtEnd() {
        return current >= sourceCode.length();
    }

    // Escanea un token especifico
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(MUL);
            case '/' -> addToken(DIV);
            case '%' -> addToken(MOD);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case '!' -> addToken(match('=') ? NOT_EQUAL : NOT);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '"' -> string();
            case ' ', '\t', '\r' -> {}
            case '\n' -> line++;
            case '#' -> {
                while (peek() != '\n' && !isAtEnd()) advance();
            }
            case 'o' -> {
                if (match('r')) {
                    addToken(OR);
                }
            }

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (Character.isLetter(c) || c == '_') {
                    identifier();
                } else {
                    Compilador.error(line, "Caracter inesperado: " + c);
                }
            }
        }
    }

    // Avanza al siguiente caracter.
    private char advance() {
        return sourceCode.charAt(current++);
    }

    // Agrega un token a la lista de tokens.
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = sourceCode.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // Verifica si el caracter actual coincide con el esperado.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (sourceCode.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // Devuelve el caracter actual sin avanzar.
    private char peek() {
        if (isAtEnd()) return '\0';
        return sourceCode.charAt(current);
    }

    // Devuelve el siguiente caracter sin avanzar.
    private char peekNext() {
        if (current + 1 >= sourceCode.length()) return '\0';
        return sourceCode.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Compilador.error(line, "Cadena sin cerrar. Se esperaba '\"'.");
            return;
        }

        advance();

        String value = sourceCode.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    // Verifica si el caracter actual es un digito.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(sourceCode.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = sourceCode.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }


}
