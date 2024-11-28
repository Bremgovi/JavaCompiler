package com.compiler;
import java.util.*;
import static com.compiler.TokenType.*;

public class VCI {
    private final List<Token> tokens;
    private final List<Token> VCI = new ArrayList<>();
    private final Stack<Token> operatorStack = new Stack<>();
    private final Stack<Token> statementStack = new Stack<>();
    private final Stack<Integer> addressStack = new Stack<>();
    private final Map<String, Integer> operatorPrecedence = new HashMap<>();

    private void initializeOperatorPrecedence() {
        operatorPrecedence.put("*", 60);
        operatorPrecedence.put("/", 60);
        operatorPrecedence.put("%", 60);
        operatorPrecedence.put("+", 50);
        operatorPrecedence.put("-", 50);
        operatorPrecedence.put(">", 40);
        operatorPrecedence.put(">=", 40);
        operatorPrecedence.put("<", 40);
        operatorPrecedence.put("<=", 40);
        operatorPrecedence.put("==", 40);
        operatorPrecedence.put("!=", 40);
        operatorPrecedence.put("not", 30);
        operatorPrecedence.put("and", 20);
        operatorPrecedence.put("or", 10);
        operatorPrecedence.put("=", 0);
    }

    public VCI(List<Token> tokens) {
        this.tokens = tokens;
        initializeOperatorPrecedence();
        processTokens();
    }

    private void processTokens() {
        List<Token> printTokens = new ArrayList<>();
        List<Token> inputTokens = new ArrayList<>();
        boolean isIfBlock = false;
        boolean isWhileBlock = false;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == IDENTIFIER || token.type == NUMBER || token.type == STRING) {
                VCI.add(token);
            } else if (token.lexeme.equals("(")) {
                operatorStack.push(token);
            } else if (token.lexeme.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().lexeme.equals("(")) {
                    VCI.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek().lexeme.equals("(")) {
                    operatorStack.pop();
                }
            } else if (operatorPrecedence.containsKey(token.lexeme)) {
                while (!operatorStack.isEmpty() && operatorPrecedence.containsKey(operatorStack.peek().lexeme) &&
                        operatorPrecedence.get(token.lexeme) <= operatorPrecedence.get(operatorStack.peek().lexeme)) {
                    VCI.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.type == SEMICOLON) {
                if(!printTokens.isEmpty()){
                    VCI.addAll(printTokens);
                    printTokens.clear();
                }
                if(!inputTokens.isEmpty()){
                    VCI.addAll(inputTokens);
                    inputTokens.clear();
                }
                while (!operatorStack.isEmpty()) {
                    VCI.add(operatorStack.pop());
                }
            } else if (token.type == PRINT) {
                printTokens.add(token);
            } else if (token.type == INPUT) {
                inputTokens.add(token);
            } else if (token.type == PROGRAM) {
                if (i + 1 < tokens.size() && tokens.get(i + 1).type == IDENTIFIER) {
                    VCI.add(tokens.get(i + 1));
                    i++;
                }
                VCI.add(token);
            } else if (token.type == IF){
                statementStack.push(token);
                isIfBlock = true;
            } else if (token.type == LEFT_BRACE){
                if (isIfBlock) {
                    if(isWhileBlock) isWhileBlock = false;
                    while (!operatorStack.isEmpty()) {
                        VCI.add(operatorStack.pop());
                    }
                    Token emptyToken = new Token(EMPTY, "", null, 0);
                    VCI.add(emptyToken);
                    addressStack.push(VCI.indexOf(emptyToken));
                    VCI.add(new Token(IF, "IF", null, 0));
                }
                if(isWhileBlock){
                    while (!operatorStack.isEmpty()) {
                        VCI.add(operatorStack.pop());
                    }
                    Token emptyToken = new Token(EMPTY, "", null, 0);
                    VCI.add(emptyToken);
                    addressStack.push(VCI.size()-1);
                    VCI.add(new Token(WHILE, "WHILE", null, 0));
                }
                isWhileBlock = true;
            }else if (token.type == RIGHT_BRACE){
                if (!statementStack.isEmpty() && statementStack.peek().type == IF){
                    statementStack.pop();
                    if (tokens.get(i+1) != null && tokens.get(i+1).type == ELSE){
                        statementStack.push(tokens.get(i+1));
                        i++;
                        int address = addressStack.pop();
                        VCI.set(address, new Token(ADDRESS, String.valueOf(VCI.size() + 2), null, 0));
                        Token emptyToken = new Token(EMPTY, "", null, 0);
                        VCI.add(emptyToken);
                        addressStack.push(VCI.indexOf(emptyToken));
                        VCI.add(new Token(ELSE, "ELSE", null, 0));
                        i++;
                        isIfBlock = false;
                    }else{
                        int address = addressStack.pop();
                        VCI.set(address, new Token(ADDRESS, String.valueOf(VCI.size()), null, 0));
                    }
                } else if (!statementStack.isEmpty() && statementStack.peek().type == ELSE){
                    statementStack.pop();
                    int address = addressStack.pop();
                    VCI.set(address, new Token(ADDRESS, String.valueOf(VCI.size()), null, 0));
                } else if (!statementStack.isEmpty() && statementStack.peek().type == WHILE){
                    statementStack.pop();
                    int address = addressStack.pop();
                    VCI.set(address, new Token(ADDRESS,  String.valueOf(VCI.size()+2), null, 0));
                    address = addressStack.pop();
                    VCI.add(new Token(ADDRESS, String.valueOf(address), null, 0));
                    VCI.add(new Token(END, "END", null, 0));
                    isWhileBlock = false;

                }
            } else if (token.type == WHILE) {
                statementStack.push(token);
                addressStack.push(VCI.size());
                isWhileBlock = true;
            } else if (token.type == TRUE || token.type == FALSE || token.type == NULL) {
                VCI.add(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            VCI.add(operatorStack.pop());
        }

        VCI.addAll(printTokens);
        VCI.addAll(inputTokens);
    }

    public List<Token> getVCI() {
        return VCI;
    }
}