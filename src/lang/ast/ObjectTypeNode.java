package lang.ast;

import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.InterfaceStatementNode;

import java.util.List;

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
    public String toString() {
        return identifierNode.toString();
    }

    public void setDefinition(AstNode node) {
        this.definitionNode = node;
    }
}