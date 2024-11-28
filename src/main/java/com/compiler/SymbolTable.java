package com.compiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> table = new HashMap<>();

    public void put(String name, Symbol symbol) {
        table.put(name, symbol);
    }

    public Symbol get(String name) {
        return table.get(name);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public Map<String, Symbol> getTable() {
        return table;
    }
    public static class Value {
        private final String variableName;
        private final Object value;
        private final int line;

        public Value(String variableName, Object value, int line) {
            this.variableName = variableName;
            this.value = value;
            this.line = line;
        }

        public String getVariableName() {
            return variableName;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "variableName='" + variableName + '\'' +
                    ", value=" + value +
                    '}';
        }

        public int getLine() {
            return line;
        }
    }
    public static class Symbol {
        String lexeme;
        String type;
        Value value;

        public Symbol(String lexeme, String type, Value value) {
            this.lexeme = lexeme;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Symbol{" +
                    "lexeme='" + lexeme + '\'' +
                    ", type='" + type + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}