package com.compiler;

import java.util.List;
import java.util.Stack;
import static com.compiler.TokenType.*;

public class Execution {
    private final Stack<SymbolTable.Symbol> executionStack = new Stack<>();
    private final SymbolTable symbolTable = new SymbolTable();
    private final FunctionTable functionTable = new FunctionTable();

    public void executeVCI(List<Token> vci) {
        int i = 0;
        while (i < vci.size()) {
            Token token = vci.get(i);
            if (token.type == IDENTIFIER) {
                if (symbolTable.contains(token.lexeme)) {
                    executionStack.push(symbolTable.get(token.lexeme));
                } else {
                    executionStack.push(new SymbolTable.Symbol(token.lexeme, "IDENTIFIER", new SymbolTable.Value(token.lexeme, null)));
                }
            } else if (token.type == NUMBER || token.type == ADDRESS) {
                executionStack.push(new SymbolTable.Symbol(token.lexeme, "NUMBER", new SymbolTable.Value(null, Double.parseDouble(token.lexeme))));
            } else if (token.type == STRING) {
                executionStack.push(new SymbolTable.Symbol(token.lexeme, "STRING", new SymbolTable.Value(null, token.literal)));
            } else if (token.type == TRUE || token.type == FALSE) {
                executionStack.push(new SymbolTable.Symbol(token.lexeme, "BOOLEAN", new SymbolTable.Value(null, Boolean.parseBoolean(token.lexeme))));
            } else if (token.type == PLUS || token.type == MINUS || token.type == MUL || token.type == DIV || token.type == MOD ||
                    token.type == EQUAL || token.type == AND || token.type == OR || token.type == NOT ||
                    token.type == GREATER || token.type == GREATER_EQUAL || token.type == LESS ||
                    token.type == LESS_EQUAL || token.type == EQUAL_EQUAL || token.type == NOT_EQUAL) {
                processOperator(token.lexeme);
            } else if (token.type == PRINT) {
                processPrint();
            } else if (token.type == PROGRAM) {
                processProgram();
            } else if (token.type == IF) {
                SymbolTable.Symbol address = executionStack.pop();
                SymbolTable.Symbol condition = executionStack.pop();
                boolean booleanCondition = (Boolean) condition.value.getValue();
                if (!booleanCondition) {
                    i = Integer.parseInt(address.lexeme);
                    continue;
                }
            } else if (token.type == ELSE) {
                SymbolTable.Symbol address = executionStack.pop();
                i = Integer.parseInt(address.lexeme);
                continue;
            } else if (token.type == WHILE) {
                SymbolTable.Symbol address = executionStack.pop();
                SymbolTable.Symbol condition = executionStack.pop();
                boolean booleanCondition = (Boolean) condition.value.getValue();
                if (!booleanCondition) {
                    i = Integer.parseInt(address.lexeme);
                    continue;
                }
            } else if (token.type == END) {
                SymbolTable.Symbol address = executionStack.pop();
                i = Integer.parseInt(address.lexeme);
                continue;
            }
            i++;
        }
    }

    private void processOperator(String operator) {
        if (operator.equals("NOT")) {
            if (executionStack.isEmpty()) {
                throw new IllegalStateException("Not enough operands for the operator: " + operator);
            }
            SymbolTable.Symbol a = executionStack.pop();
            boolean result = !(Boolean) a.value.getValue();
            executionStack.push(new SymbolTable.Symbol(String.valueOf(result), "BOOLEAN", new SymbolTable.Value(null, result)));
            return;
        }

        SymbolTable.Symbol b = executionStack.pop();
        SymbolTable.Symbol a = executionStack.pop();
        Object result = null;
        String resultType = "BOOLEAN";
        switch (operator) {
            case "+" -> {
                if (a.type.equals("NUMBER") && b.type.equals("NUMBER")) {
                    result = (Double) a.value.getValue() + (Double) b.value.getValue();
                    resultType = "NUMBER";
                } else {
                    result = a.value.getValue().toString() + b.value.getValue().toString();
                    resultType = "STRING";
                }
            }
            case "-" -> {
                result = (Double) a.value.getValue() - (Double) b.value.getValue();
                resultType = "NUMBER";
            }
            case "*" -> {
                result = (Double) a.value.getValue() * (Double) b.value.getValue();
                resultType = "NUMBER";
            }
            case "/" -> {
                result = (Double) a.value.getValue() / (Double) b.value.getValue();
                resultType = "NUMBER";
            }
            case "=" -> {
                symbolTable.put(a.lexeme, new SymbolTable.Symbol(a.lexeme, b.type, b.value));
                return;
            }
            case "%" -> {
                result = (Double) a.value.getValue() % (Double) b.value.getValue();
                resultType = "NUMBER";
            }
            case "and" -> result = (Boolean) a.value.getValue() && (Boolean) b.value.getValue();
            case "or" -> result = (Boolean) a.value.getValue() || (Boolean) b.value.getValue();
            case ">" -> result = (Double) a.value.getValue() > (Double) b.value.getValue();
            case ">=" -> result = (Double) a.value.getValue() >= (Double) b.value.getValue();
            case "<" -> result = (Double) a.value.getValue() < (Double) b.value.getValue();
            case "<=" -> result = (Double) a.value.getValue() <= (Double) b.value.getValue();
            case "==" -> result = a.value.getValue().equals(b.value.getValue());
            case "!=" -> result = !a.value.getValue().equals(b.value.getValue());

        }
        executionStack.push(new SymbolTable.Symbol(result.toString(), resultType, new SymbolTable.Value(null, result)));
    }

    private void processPrint() {
        if (executionStack.isEmpty()) {
            throw new IllegalStateException("Nothing to print");
        }

        SymbolTable.Symbol value = executionStack.pop();
        if (value.type.equals("IDENTIFIER") && symbolTable.contains(value.lexeme)) {
            System.out.println(symbolTable.get(value.lexeme).value.getValue());
        } else {
            System.out.println(value.value.getValue());
        }
    }

    private void processProgram() {
        SymbolTable.Symbol value = executionStack.pop();
        functionTable.put(value.lexeme, value);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public FunctionTable getFunctionTable() {
        return functionTable;
    }
}