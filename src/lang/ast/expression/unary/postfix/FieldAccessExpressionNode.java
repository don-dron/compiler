package lang.ast.expression.unary.postfix;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.expression.ExpressionNode;
import lang.ast.expression.VariableExpressionNode;

import java.util.List;

public class FieldAccessExpressionNode extends ExpressionNode{
    private final ExpressionNode left;
    private final VariableExpressionNode right;

    public FieldAccessExpressionNode(ExpressionNode left, VariableExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public VariableExpressionNode getRight() {
        return right;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "FieldAccessExpression:\n" +
                left.astDebug(shift + 1) + "\n" +
                right.astDebug(shift + 1);
    }

    @Override
    public String toString() {
        return left.toString() + "." + right.toString();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(left);
    }
}
