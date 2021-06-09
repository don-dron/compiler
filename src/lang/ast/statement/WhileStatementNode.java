package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class WhileStatementNode extends StatementNode {
    private final ExpressionNode conditionNode;
    private final StatementNode bodyNode;

    public WhileStatementNode(ExpressionNode conditionNode, StatementNode bodyNode) {
        this.conditionNode = conditionNode;
        this.bodyNode = bodyNode;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public StatementNode getBodyNode() {
        return bodyNode;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "While:\n";

        if (conditionNode != null) {
            s += conditionNode.astDebug(shift + 1) + "\n";
        }

        if (bodyNode != null) {
            s += bodyNode.astDebug(shift + 1) + "\n";
        }

        return s.stripTrailing();
    }

    @Override
    public List<AstNode> getChildren() {
        List<AstNode> list = new ArrayList<>();

        if (conditionNode != null) {
            list.add(conditionNode);
        }

        if (bodyNode != null) {
            list.add(bodyNode);
        }

        return list;
    }
}
