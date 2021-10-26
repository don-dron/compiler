package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ExpressionStatementNode extends StatementNode {
    private final ExpressionNode expressionNode;

    public ExpressionStatementNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ExpressionStatement:\n" + expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }

    @Override
    public String toString() {
        return expressionNode.toString();
    }
}
