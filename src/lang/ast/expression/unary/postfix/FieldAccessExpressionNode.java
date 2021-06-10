package lang.ast.expression.unary.postfix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class FieldAccessExpressionNode extends ExpressionNode{
    private final ExpressionNode expressionNode;
    private final ExpressionNode next;

    public FieldAccessExpressionNode(ExpressionNode expressionNode, ExpressionNode next) {
        this.expressionNode = expressionNode;
        this.next = next;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "FieldAccessExpression:\n" +
                expressionNode.astDebug(shift + 1) + "\n" +
                next.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
