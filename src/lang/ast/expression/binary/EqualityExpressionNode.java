package lang.ast.expression.binary;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class EqualityExpressionNode extends BinaryOperationExpressionNode {
    private final EqualityType type;
    private final ExpressionNode first;
    private final ExpressionNode second;

    public EqualityExpressionNode(EqualityType type, ExpressionNode first, ExpressionNode second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public EqualityType getType() {
        return type;
    }

    public ExpressionNode getRight() {
        return second;
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

    public enum EqualityType {EQ, NE}

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(first, second);
    }
}
