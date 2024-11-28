package com.compiler;

import java.util.HashMap;
import java.util.Map;

public class FunctionTable {
    private final Map<String, SymbolTable.Symbol> table = new HashMap<>();

    public void put(String name, SymbolTable.Symbol symbol) {
        table.put(name, symbol);
    }

    public SymbolTable.Symbol get(String name) {
        return table.get(name);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public Map<String, SymbolTable.Symbol> getTable() {
        return table;
    }

    public static class Symbol {
        String lexeme;
        String type;
        Object value;

        public Symbol(String lexeme, String type, Object value) {
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
