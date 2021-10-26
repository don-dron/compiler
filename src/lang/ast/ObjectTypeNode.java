package lang.ast;

import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.InterfaceStatementNode;

import java.util.List;
import java.util.Objects;

public class ObjectTypeNode extends TypeNode {
    private final IdentifierNode identifierNode;
    private AstNode definitionNode;

    public ObjectTypeNode(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        String name = "";

        if (definitionNode instanceof ClassStatementNode) {
            name = ((ClassStatementNode) definitionNode).getIdentifierNode().getName();
        } else if (definitionNode instanceof InterfaceStatementNode) {
            name = ((InterfaceStatementNode) definitionNode).getIdentifierNode().getName();
        }

        return SHIFT.repeat(shift) + "ObjectType:\n" + identifierNode.astDebug(shift + 1) + " : " + name;
    }

    public AstNode getDefinitionNode() {
        return definitionNode;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectTypeNode that = (ObjectTypeNode) o;
        return Objects.equals(definitionNode, that.definitionNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierNode, definitionNode);
    }

    @Override
    public String toString() {
        return identifierNode.toString();
    }

    public void setDefinition(AstNode node) {
        this.definitionNode = node;
    }
}