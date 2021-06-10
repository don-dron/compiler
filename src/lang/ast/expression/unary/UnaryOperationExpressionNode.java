package lang.ast.expression.unary;

import lang.ast.expression.ExpressionNode;

public abstract class UnaryOperationExpressionNode extends ExpressionNode {
    public abstract ExpressionNode getArgument();
}
