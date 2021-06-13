package lang.ast.expression.binary;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class AssigmentExpressionNode extends BinaryOperationExpressionNode {

    private final ExpressionNode left;
    private final ExpressionNode right;

    public AssigmentExpressionNode(ExpressionNode left, ExpressionNode right) {
        super();
        this.left = left;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "AssigmentExpression:\n" +
                left.astDebug(shift + 1) + "\n" +
                right.astDebug(shift + 1);
    }

    @Override
    public String toString() {
        return left.toString() + " = " + right.toString();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(left, right);
    }
}
