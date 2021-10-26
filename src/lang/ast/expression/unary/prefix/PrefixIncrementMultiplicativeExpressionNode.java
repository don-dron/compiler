package lang.ast.expression.unary.prefix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class PrefixIncrementMultiplicativeExpressionNode extends ExpressionNode {
    private final ExpressionNode expressionNode;

    public PrefixIncrementMultiplicativeExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "PrefixIncrementMultiplicativeExpression:\n" +
                expressionNode.astDebug(shift + 1);
    }

    @Override
    public String toString() {
        return "**" + expressionNode.toString();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
