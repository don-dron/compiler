package com.compiler.ast.expression;

import com.compiler.ast.AstNode;
import com.compiler.ast.ExpressionListNode;
import com.compiler.ast.IdentifierNode;

import java.util.List;

public class FunctionCallExpressionNode extends PrimaryExpressionNode {
    private final IdentifierNode identifierNode;
    private final ExpressionListNode parameters;

    public FunctionCallExpressionNode(IdentifierNode identifierNode, ExpressionListNode parameters) {
        this.identifierNode = identifierNode;
        this.parameters = parameters;
    }

    public ExpressionListNode getParameters() {
        return parameters;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "FunctionCall: " + identifierNode.getName() + "\n" +
                        parameters.astDebug(shift+1);
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }
}
