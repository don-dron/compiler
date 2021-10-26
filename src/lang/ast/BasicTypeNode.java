package lang.ast;

import lang.Position;
import lang.lexer.Token;

import java.util.List;
import java.util.Objects;

public class BasicTypeNode extends TypeNode {
    private final TypeNode.Type type;
    private final Token token;

    public BasicTypeNode(TypeNode.Type type, Token token) {
        this.type = type;
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

    public TypeNode.Type getType() {
        return type;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "BasicType: " + type;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof BasicTypeNode) {
            BasicTypeNode that = (BasicTypeNode) o;
            return Objects.equals(type, that.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}