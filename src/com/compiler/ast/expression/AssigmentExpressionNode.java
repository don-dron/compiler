package com.compiler.ast.expression;

import com.compiler.ast.IdentifierNode;
import com.compiler.ast.expression.ExpressionNode;

public class AssigmentExpressionNode extends ExpressionNode {
    private final IdentifierNode identifierNode;
    private final ExpressionNode expressionNode;

    public AssigmentExpressionNode(IdentifierNode identifierNode, ExpressionNode expressionNode) {
        super();
        this.expressionNode = expressionNode;
        this.identifierNode = identifierNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Assigment:\n" +
                identifierNode.astDebug(shift + 1) + "\n" +
                expressionNode.astDebug(shift + 1);
    }
}
