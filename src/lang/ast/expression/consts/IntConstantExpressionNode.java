package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.GlobalBasicType;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

public class IntConstantExpressionNode extends PrimaryExpressionNode {
    private final int value;

    public IntConstantExpressionNode(int value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Int: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return GlobalBasicType.INT_TYPE;
    }

    public int getValue() {
        return value;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
