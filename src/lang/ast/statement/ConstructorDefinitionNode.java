package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.FunctionNode;
import lang.ast.statement.StatementNode;

import java.util.List;

public class ConstructorDefinitionNode extends StatementNode {
    private final FunctionNode functionNode;
    private final StatementNode statementNode;
    private ClassStatementNode classStatementNode;

    public ConstructorDefinitionNode(FunctionNode functionNode,
                                     StatementNode statementNode) {
        this.functionNode = functionNode;
        this.statementNode = statementNode;
    }

    public ClassStatementNode getClassStatementNode() {
        return classStatementNode;
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

    public void setClass(ClassStatementNode classStatementNode) {
        this.classStatementNode = classStatementNode;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(statementNode);
    }

    @Override
    public String toString() {
        return functionNode.getParametersNode().toString() + " " + statementNode.toString();
    }
}