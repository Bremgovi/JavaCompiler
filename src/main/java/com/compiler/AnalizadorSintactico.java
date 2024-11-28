package com.compiler;

import java.util.List;

public class AnalizadorSintactico {
    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {}

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() {
        while (!isAtEnd()) {
            declaration();
        }
    }

    private void declaration() {
        if (match(TokenType.VAR)) {
            varDeclaration();
        } else {
            statement();
        }
    }

    private void varDeclaration() {
        consume(TokenType.IDENTIFIER, "Expect variable name.");
        if (match(TokenType.EQUAL)) {
            expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
    }

    private void statement() {
        if (match(TokenType.PRINT)) {
            printStatement();
        } else if (match(TokenType.IF)) {
            ifStatement();
        } else if (match(TokenType.WHILE)) {
            whileStatement();
        } else {
            expressionStatement();
        }
    }

    private void printStatement() {
        expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
    }

    private void ifStatement() {
        expression();
        consume(TokenType.LEFT_BRACE, "Expect '{' before if body.");
        statement();
        consume(TokenType.RIGHT_BRACE, "Expect '}' after if body.");
        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACE, "Expect '{' before else body.");
            statement();
            consume(TokenType.RIGHT_BRACE, "Expect '}' after else body.");
        }
    }

    private void whileStatement() {
        expression();
        consume(TokenType.LEFT_BRACE, "Expect '{' before while body.");
        statement();
        consume(TokenType.RIGHT_BRACE, "Expect '}' after while body.");
    }

    private void expressionStatement() {
        expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    }

    private void expression() {
        equality();
    }

    private void equality() {
        comparison();
        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            comparison();
        }
    }

    private void comparison() {
        addition();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            addition();
        }
    }

    private void addition() {
        multiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            multiplication();
        }
    }

    private void multiplication() {
        unary();
        while (match(TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            Token operator = previous();
            unary();
        }
    }

    private void unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            unary();
        } else {
            primary();
        }
    }

    private void primary() {
        if (match(TokenType.FALSE)) return;
        if (match(TokenType.TRUE)) return;
        if (match(TokenType.NUMBER)) return;
        if (match(TokenType.STRING)) return;
        if (match(TokenType.IDENTIFIER)) return;
        if (match(TokenType.LEFT_PAREN)) {
            expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return;
        }
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Compilador.error(token, message);
        return new ParseError();
    }
}