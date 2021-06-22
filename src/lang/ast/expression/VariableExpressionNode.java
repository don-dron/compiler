package lang.ast.expression;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;

import java.util.List;

public class VariableExpressionNode extends PrimaryExpressionNode {

    private IdentifierNode identifierNode;
    private AstNode expressionNode;

    public VariableExpressionNode(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "VariableExpression: " + identifierNode.getName();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    @Override
    public String toString() {
        return identifierNode.getName();
    }

    public void setExpression(AstNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public void setIdentifier(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }
}
