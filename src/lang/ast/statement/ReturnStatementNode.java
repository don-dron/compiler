package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ReturnStatementNode extends StatementNode {
    private final ExpressionNode expressionNode;
    private FunctionDefinitionNode functionDefinitionNode;

    public ReturnStatementNode(ExpressionNode expressionNode) {
        super();
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public FunctionDefinitionNode getFunctionDefinitionNode() {
        return functionDefinitionNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "ReturnStatement:\n" +
                (expressionNode == null ? "" : expressionNode.astDebug(shift + 1))).stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return expressionNode == null ? List.of() : List.of(expressionNode);
    }

    @Override
    public String toString() {
        return "return";
    }

    public void setFunction(FunctionDefinitionNode functionDefinitionNode) {
        this.functionDefinitionNode = functionDefinitionNode;
    }
}
