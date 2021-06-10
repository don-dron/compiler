package lang.ast;

import lang.ast.statement.StatementNode;

import java.util.List;

public class ConstructorDefinitionNode extends StatementNode {
    private final FunctionNode functionNode;
    private final StatementNode statementNode;

    public ConstructorDefinitionNode(FunctionNode functionNode,
                                     StatementNode statementNode) {
        this.functionNode = functionNode;
        this.statementNode = statementNode;
    }

    public FunctionNode getFunctionNode() {
        return functionNode;
    }

    public StatementNode getStatementNode() {
        return statementNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ConstructorDefinition:\n" +
                functionNode.astDebug(shift + 1) + "\n" +
                statementNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(statementNode);
    }
}