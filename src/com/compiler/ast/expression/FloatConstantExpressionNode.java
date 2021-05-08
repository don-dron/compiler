package com.compiler.ast.expression;

import com.compiler.ast.AstNode;

import java.util.List;

public class FloatConstantExpressionNode extends PrimaryExpressionNode {

    private final float value;

    public FloatConstantExpressionNode(float value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Float: " + value;
    }


    public float getValue() {
        return value;
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
