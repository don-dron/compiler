package lang.ast.expression.binary;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class LogicalOrExpressionNode extends BinaryOperationExpressionNode {
    private final ExpressionNode first;
    private final ExpressionNode second;

    public LogicalOrExpressionNode(ExpressionNode first, ExpressionNode second) {
        this.first = first;
        this.second = second;
    }

    public ExpressionNode getSecond() {
        return second;
    }

    public ExpressionNode getFirst() {
        return first;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "OR:\n" + first.astDebug(shift + 1) + "\n" + second.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(first, second);
    }
}
