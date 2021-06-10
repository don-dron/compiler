package lang.ast.expression.unary.prefix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class PrefixDecrementSubtractionExpressionNode extends ExpressionNode {
    private final ExpressionNode expressionNode;

    public PrefixDecrementSubtractionExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "PrefixDecrementSubtractionExpression:\n" +
                expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
