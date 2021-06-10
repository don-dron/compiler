package lang.ast.expression.binary;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class AdditiveExpressionNode extends BinaryOperationExpressionNode {

    private final AdditiveType type;
    private final ExpressionNode first;
    private final ExpressionNode second;

    public AdditiveExpressionNode(AdditiveType type, ExpressionNode first, ExpressionNode second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public AdditiveType getType() {
        return type;
    }

    public ExpressionNode getFirst() {
        return first;
    }

    public ExpressionNode getSecond() {
        return second;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + type + ":\n" +
                first.astDebug(shift + 1) + "\n" +
                second.astDebug(shift + 1);
    }

    public enum AdditiveType {
        ADD,
        SUB
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(first, second);
    }
}
