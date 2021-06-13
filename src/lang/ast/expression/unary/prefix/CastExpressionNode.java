package lang.ast.expression.unary.prefix;

import lang.ast.AstNode;
import lang.ast.TypeNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class CastExpressionNode extends ExpressionNode {
    private final TypeNode typeNode;
    private final ExpressionNode expressionNode;

    public CastExpressionNode(TypeNode typeNode, ExpressionNode expressionNode) {
        this.typeNode = typeNode;
        this.expressionNode = expressionNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "CastExpression:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                expressionNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }

    @Override
    public String toString() {
        return "(" + typeNode + ")" + expressionNode.toString();
    }
}
