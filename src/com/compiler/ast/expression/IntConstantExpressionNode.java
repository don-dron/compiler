package com.compiler.ast.expression;

public class IntConstantExpressionNode extends PrimaryExpressionNode {

    private final int value;

    public IntConstantExpressionNode(int value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Int: " + value;
    }
}
