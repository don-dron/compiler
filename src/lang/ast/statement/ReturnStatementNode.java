package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ReturnStatementNode extends StatementNode {
    private final ExpressionNode expressionNode;

    public ReturnStatementNode(ExpressionNode expressionNode) {
        super();
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Return:\n" +
                (expressionNode == null ? "" : expressionNode.astDebug(shift + 1))).stripTrailing();
    }

    @Override
    public List<AstNode> getChildren() {
        return expressionNode == null ? List.of() : List.of(expressionNode);
    }
}
