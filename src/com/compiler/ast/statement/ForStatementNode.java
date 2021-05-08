package com.compiler.ast.statement;

import com.compiler.ast.expression.ExpressionNode;

public class ForStatementNode extends StatementNode {
    private final StatementNode prev;
    private final ExpressionNode predicate;
    private final ExpressionNode step;
    private final StatementNode body;

    public ForStatementNode(StatementNode prev, ExpressionNode predicate, ExpressionNode step, StatementNode body) {
        this.prev = prev;
        this.predicate = predicate;
        this.step = step;
        this.body = body;
    }

    public ExpressionNode getPredicate() {
        return predicate;
    }

    public ExpressionNode getStep() {
        return step;
    }

    public StatementNode getBody() {
        return body;
    }

    public StatementNode getPrev() {
        return prev;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "For:\n";

        if (prev != null) {
            s = s + prev.astDebug(shift + 1) + "\n";
        }

        if (predicate != null) {
            s = s + predicate.astDebug(shift + 1) + "\n";
        }

        if (step != null) {
            s = s + step.astDebug(shift + 1) + "\n";
        }

        if (body != null) {
            s = s + body.astDebug(shift + 1) + "\n";
        }

        return s.stripTrailing();
    }
}
