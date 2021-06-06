package com.compiler.ast.expression;

public abstract class BinaryOperationExpressionNode extends ExpressionNode{
    public abstract ExpressionNode getFirst();

    public abstract ExpressionNode getSecond();
}
