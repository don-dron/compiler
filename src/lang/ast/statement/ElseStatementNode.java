package lang.ast.statement;

import lang.ast.AstNode;

import java.util.ArrayList;
import java.util.List;

public class ElseStatementNode extends StatementNode {
    private final StatementNode elseNode;

    public ElseStatementNode(StatementNode elseNode) {
        this.elseNode = elseNode;
    }

    public StatementNode getElseNode() {
        return elseNode;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "ElseStatement:\n";
        if (elseNode != null) {
            s += elseNode.astDebug(shift + 1) + "\n";
        }

        return s.stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        List<AstNode> list = new ArrayList<>();

        if (elseNode != null) {
            list.add(elseNode);
        }

        return list;
    }

    @Override
    public String toString() {
        return "else\n" + elseNode.toString();
    }
}
