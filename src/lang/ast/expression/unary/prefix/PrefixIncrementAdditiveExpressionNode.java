package lang.ast.expression.unary.prefix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class PrefixIncrementAdditiveExpressionNode extends ExpressionNode {
    private final ExpressionNode expressionNode;

    public PrefixIncrementAdditiveExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "PrefixIncrementAdditiveExpression:\n" +
                expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}