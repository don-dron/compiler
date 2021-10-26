package lang.ast.expression.binary;

import lang.ast.expression.ExpressionNode;

public abstract class BinaryOperationExpressionNode extends ExpressionNode {
    public abstract ExpressionNode getLeft();

    public abstract ExpressionNode getRight();
}
