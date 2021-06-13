package lang.ast.expression.binary;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;
import lang.ast.expression.binary.BinaryOperationExpressionNode;

import java.util.List;

public class RelationalExpressionNode extends BinaryOperationExpressionNode {
    private final RelationalType  type;
    private final ExpressionNode first;
    private final ExpressionNode second;

    public RelationalExpressionNode(RelationalType type, ExpressionNode first, ExpressionNode second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public ExpressionNode getRight() {
        return second;
    }

    public RelationalType getType() {
        return type;
    }

    public ExpressionNode getLeft() {
        return first;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + type + ":\n" +
                first.astDebug(shift + 1) + "\n" +
                second.astDebug(shift + 1);
    }

    @Override
    public String toString() {
        return first.toString() + " " + type + " " + second.toString();
    }

    public enum RelationalType {GE, GT, LE, LT}

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(first, second);
    }
}
