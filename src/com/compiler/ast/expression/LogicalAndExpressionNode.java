package com.compiler.ast.expression;

import com.compiler.ast.AstNode;
import com.compiler.ast.expression.ExpressionNode;

import java.util.List;

public class LogicalAndExpressionNode extends BinaryOperationExpressionNode {
    private final ExpressionNode first;
    private final ExpressionNode second;

    public LogicalAndExpressionNode(ExpressionNode first, ExpressionNode second) {
        this.first = first;
        this.second = second;
    }

    public ExpressionNode getSecond() {
        return second;
    }

    public ExpressionNode getFirst() {
        return first;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "AND:\n" + first.astDebug(shift + 1) + "\n" + second.astDebug(shift + 1);
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of(first, second);
    }
}
