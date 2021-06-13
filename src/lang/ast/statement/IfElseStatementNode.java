package lang.ast.statement;

import lang.ast.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IfElseStatementNode extends StatementNode {
    private final IfStatementNode ifStatementNode;
    private final List<ElifStatementNode> elifStatementNodes;
    private final ElseStatementNode elseStatementNode;

    public IfElseStatementNode(IfStatementNode ifStatementNode,
                               List<ElifStatementNode> elifStatementNodes,
                               ElseStatementNode elseStatementNode) {
        this.ifStatementNode = ifStatementNode;
        this.elifStatementNodes = elifStatementNodes;
        this.elseStatementNode = elseStatementNode;
    }

    public ElseStatementNode getElseStatementNode() {
        return elseStatementNode;
    }

    public IfStatementNode getIfStatementNode() {
        return ifStatementNode;
    }

    public List<ElifStatementNode> getElifStatementNodes() {
        return elifStatementNodes;
    }

    @Override
    public String astDebug(int shift) {
        String s = SHIFT.repeat(shift) + "IfStatement:\n";

        if (ifStatementNode != null) {
            s += ifStatementNode.astDebug(shift + 1) + "\n";
        }

        if (elifStatementNodes != null) {
            s += elifStatementNodes.stream()
                    .map(st -> st.astDebug(shift + 1))
                    .collect(Collectors.joining("\n"))
                    + "\n";
        }

        if (elseStatementNode != null) {
            s += elseStatementNode.astDebug(shift + 1);
        }

        return s.stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        List<AstNode> list = new ArrayList<>();

        if (ifStatementNode != null) {
            list.add(ifStatementNode);
        }

        if (elifStatementNodes != null) {
            list.addAll(elifStatementNodes);
        }

        if (elseStatementNode != null) {
            list.add(elseStatementNode);
        }

        return list;
    }

    @Override
    public String toString() {
        return ifStatementNode.toString() + "\n" +
                (elifStatementNodes == null ? "" : (elifStatementNodes.toString() + "\n"))
                + (elseStatementNode == null ? "" : elseStatementNode.toString());
    }
}
