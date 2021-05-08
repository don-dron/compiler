package com.compiler.ast.expression;

import com.compiler.ast.AstNode;

import java.util.List;

public class BoolConstantExpressionNode extends PrimaryExpressionNode {

    private final boolean value;

    public BoolConstantExpressionNode(boolean value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Boolean: " + value;
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
