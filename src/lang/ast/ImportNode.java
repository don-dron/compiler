package lang.ast;

import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ImportNode extends AstNode {
    private final ExpressionNode expressionNode;

    public ImportNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Import:\n" + expressionNode.astDebug(shift + 1));
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
