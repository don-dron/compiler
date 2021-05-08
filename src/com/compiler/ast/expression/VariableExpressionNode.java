package com.compiler.ast.expression;

import com.compiler.ast.IdentifierNode;

public class VariableExpressionNode extends PrimaryExpressionNode {

    private final IdentifierNode identifierNode;

    public VariableExpressionNode(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Variable: " + identifierNode.getName();
    }
}
