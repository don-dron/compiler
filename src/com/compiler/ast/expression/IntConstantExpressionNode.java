package com.compiler.ast.expression;

import com.compiler.ast.AstNode;

import java.util.List;

public class IntConstantExpressionNode extends PrimaryExpressionNode {

    private final int value;

    public IntConstantExpressionNode(int value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Int: " + value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
