package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class IfStatementNode extends StatementNode {
    private final ExpressionNode conditionNode;
    private final StatementNode thenNode;

    public IfStatementNode(ExpressionNode conditionNode, StatementNode thenNode) {
        this.conditionNode = conditionNode;
        this.thenNode = thenNode;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public StatementNode getThenNode() {
        return thenNode;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "IfStatement:\n";

        if (conditionNode != null) {
            s += conditionNode.astDebug(shift + 1) + "\n";
        }

        if (thenNode != null) {
            s += thenNode.astDebug(shift + 1) + "\n";
        }

        return s.stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        List<AstNode> list = new ArrayList<>();

        if (conditionNode != null) {
            list.add(conditionNode);
        }

        if (thenNode != null) {
            list.add(thenNode);
        }

        return list;
    }

    @Override
    public String toString() {
        return "if " + conditionNode.toString() + "\n" + thenNode.toString();
    }
}
