package lang.ast;

import lang.ast.statement.StatementNode;

import java.util.List;

public class FunctionDefinitionNode extends StatementNode {
    private final FunctionNode functionNode;
    private final IdentifierNode identifierNode;
    private final StatementNode statementNode;

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
                (statementNode == null ? "" : ("\n" +statementNode.astDebug(shift + 1)));
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(statementNode);
    }
}