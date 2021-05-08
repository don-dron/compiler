package com.compiler.ast.statement;

import com.compiler.ast.expression.ExpressionNode;

public class ExpressionStatementNode extends StatementNode {
    private final ExpressionNode expressionNode;

    public ExpressionStatementNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Expression:\n" + expressionNode.astDebug(shift + 1);
    }
}
