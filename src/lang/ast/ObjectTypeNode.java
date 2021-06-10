package lang.ast;

import java.util.List;

public class ObjectTypeNode extends TypeNode {
    private final IdentifierNode identifierNode;

    public ObjectTypeNode(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ObjectType:\n" + identifierNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}