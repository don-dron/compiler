package com.compiler.ast.statement;

import com.compiler.ast.AstNode;
import com.compiler.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class IfStatementNode extends StatementNode {
    private final ExpressionNode conditionNode;
    private final StatementNode thenNode;
    private final StatementNode elseNode;

    public IfStatementNode(ExpressionNode conditionNode, StatementNode thenNode, StatementNode elseNode) {
        this.conditionNode = conditionNode;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public StatementNode getElseNode() {
        return elseNode;
    }

    public StatementNode getThenNode() {
        return thenNode;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "If:\n";

        if (conditionNode != null) {
            s += conditionNode.astDebug(shift + 1) + "\n";
        }

        if (thenNode != null) {
            s += thenNode.astDebug(shift + 1) + "\n";
        }

        if (elseNode != null) {
            s += elseNode.astDebug(shift + 1) + "\n";
        }

        return s.stripTrailing();
    }

    @Override
    public List<AstNode> getChildren() {
        List<AstNode> list = new ArrayList<>();

        if (conditionNode != null) {
            list.add(conditionNode);
        }

        if (thenNode != null) {
            list.add(thenNode);
        }

        if (elseNode != null) {
            list.add(elseNode);
        }

        return list;
    }
}
