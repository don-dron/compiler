package lang.ast;

import lang.Position;
import lang.lexer.Token;

import java.util.List;

public class IdentifierNode  extends AstNode {
    private String name;
    private final Token token;

    public IdentifierNode(String name, Token token) {
        this.name = name;
        this.token = token;
    }

    @Override
    public Position getStart() {
        return token.getStart();
    }

    @Override
    public Position getEnd() {
        return token.getEnd();
    }

    @Override
    public String getFormattedText() {
        return token.getContent();
    }

    public Token getToken() {
        return token;
    }

    public void setName(String name) {
        this.name = name;
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