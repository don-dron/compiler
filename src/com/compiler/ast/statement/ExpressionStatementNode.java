package com.compiler.ast.statement;

import com.compiler.ast.AstNode;
import com.compiler.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

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
        return SHIFT.repeat(shift) + "Expression statement:\n" + expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
