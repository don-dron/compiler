package lang.ast;

import java.util.List;

public class BasicTypeNode extends TypeNode {
    private final TypeNode.Type type;

    public BasicTypeNode(TypeNode.Type type) {
        this.type = type;
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
}