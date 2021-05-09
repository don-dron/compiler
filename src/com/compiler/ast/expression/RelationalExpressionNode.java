package com.compiler.ast.expression;

import com.compiler.ast.AstNode;

import java.util.List;

public class RelationalExpressionNode extends BinaryOperationExpressionNode {
    private final RelationalType  type;
    private final ExpressionNode first;
    private final ExpressionNode second;

    public RelationalExpressionNode(RelationalType type, ExpressionNode first, ExpressionNode second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public ExpressionNode getSecond() {
        return second;
    }

    public RelationalType getType() {
        return type;
    }

    public ExpressionNode getFirst() {
        return first;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + type + ":\n" +
                first.astDebug(shift + 1) + "\n" +
                second.astDebug(shift + 1);
    }

    public enum RelationalType {GE, GT, LE, LT}

    @Override
    public List<AstNode> getChildren() {
        return List.of(first, second);
    }
}
