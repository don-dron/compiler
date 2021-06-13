package lang.ast;

import java.util.List;

public class ParameterNode extends AstNode {
    private final TypeNode typeNode;
    private final IdentifierNode identifierNode;

    public ParameterNode(TypeNode typeNode, IdentifierNode identifierNode) {
        this.typeNode = typeNode;
        this.identifierNode = identifierNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Parameter:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                identifierNode.astDebug(shift + 1)
        ).stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}
