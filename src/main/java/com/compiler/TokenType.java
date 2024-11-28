package com.compiler;

public enum TokenType {
    // Caracteres simples.
    LEFT_PAREN, RIGHT_PAREN, COMMA, DOT, MINUS, PLUS, SEMICOLON, MUL, DIV, MOD, LEFT_BRACE, RIGHT_BRACE,

    // Caracteres compuestos.
    NOT, NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literales.
    IDENTIFIER, STRING, NUMBER,

    // Palabras reservadas.
    AND, OR,
    IF, ELSE,
    FALSE, TRUE,
    RETURN,
    WHILE,
    PRINT,
    PROGRAM,
    THEN,
    END,
    INPUT,
    VAR, NULL,

    // Especiales
    EOF,
    COMMENT,
    ADDRESS,
    EMPTY
}
