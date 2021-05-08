package com.compiler.ast.expression;

public class FloatConstantExpressionNode extends PrimaryExpressionNode {

    private final float value;

    public FloatConstantExpressionNode(float value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Float: " + value;
    }
}
