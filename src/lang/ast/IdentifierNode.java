package lang.ast;

import lang.lexer.Token;

import java.util.List;

public class IdentifierNode  extends AstNode {
    private final String name;
    private final Token token;

    public IdentifierNode(String name, Token token) {
        this.name = name;
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Identifier: " + name;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return name;
    }
}