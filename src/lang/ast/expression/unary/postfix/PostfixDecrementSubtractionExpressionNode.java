package lang.ast.expression.unary.postfix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class PostfixDecrementSubtractionExpressionNode extends ExpressionNode {
    private final ExpressionNode expressionNode;

    public PostfixDecrementSubtractionExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "PostfixDecrementSubtractionExpression:\n" +
                expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }

    @Override
    public String toString() {
        return expressionNode.toString() + "--";
    }
}
