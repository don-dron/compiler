package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class ElifStatementNode extends StatementNode {
    private final ExpressionNode conditionNode;
    private final StatementNode elseNode;

    public ElifStatementNode(ExpressionNode conditionNode, StatementNode elseNode) {
        this.conditionNode = conditionNode;
        this.elseNode = elseNode;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public StatementNode getElseNode() {
        return elseNode;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "Elif:\n";

        if (conditionNode != null) {
            s += conditionNode.astDebug(shift + 1) + "\n";
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

        if (elseNode != null) {
            list.add(elseNode);
        }

        return list;
    }
}
