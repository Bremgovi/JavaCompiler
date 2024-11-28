package com.compiler;

public class Token {
    TokenType type; String lexeme; Object literal; int line; int identifier;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        identifier = -1;
        if (type == TokenType.IDENTIFIER) {
            identifier = -2;
        }
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal + " " + identifier + " " + line ;
    }
}
