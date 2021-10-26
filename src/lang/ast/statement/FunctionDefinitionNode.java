package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.FunctionNode;
import lang.ast.IdentifierNode;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinitionNode extends StatementNode {
    private final FunctionNode functionNode;
    private final IdentifierNode identifierNode;
    private final StatementNode statementNode;
    private final List<ReturnStatementNode> returns = new ArrayList<>();

    public FunctionDefinitionNode(FunctionNode functionNode,
                                  IdentifierNode identifierNode,
                                  StatementNode statementNode) {
        this.functionNode = functionNode;
        this.identifierNode = identifierNode;
        this.statementNode = statementNode;
    }

    public FunctionNode getFunctionNode() {
        return functionNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public StatementNode getStatementNode() {
        return statementNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "FunctionDefinition:\n" +
                functionNode.astDebug(shift + 1) + "\n" +
                identifierNode.astDebug(shift + 1) +
                (statementNode == null ? "" : ("\n" + statementNode.astDebug(shift + 1)));
    }

    @Override
    public String toString() {
        return functionNode.toString() + " " + identifierNode.toString() + " " +
                (statementNode == null ? "" : ("\n" + statementNode.toString()));
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(statementNode);
    }

    public void addReturn(ReturnStatementNode node) {
        returns.add(node);
    }

    public List<ReturnStatementNode> getReturns() {
        return returns;
    }
}