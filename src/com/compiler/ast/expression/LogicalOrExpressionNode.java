package com.compiler.ast.expression;

import com.compiler.ast.expression.ExpressionNode;

public class LogicalOrExpressionNode extends BinaryOperationExpressionNode {
    private final ExpressionNode first;
    private final ExpressionNode second;

    public LogicalOrExpressionNode(ExpressionNode first, ExpressionNode second) {
        this.first = first;
        this.second = second;
    }

    public ExpressionNode getSecond() {
        return second;
    }

    public ExpressionNode getFirst() {
        return first;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "OR:\n" + first.astDebug(shift + 1) + "\n" + second.astDebug(shift + 1);
    }
}
