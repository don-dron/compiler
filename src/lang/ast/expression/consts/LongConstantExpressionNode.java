package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.GlobalBasicType;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

public class LongConstantExpressionNode extends PrimaryExpressionNode {
    private final long value;

    public LongConstantExpressionNode(long value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Long: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return GlobalBasicType.LONG_TYPE;
    }

    public long getValue() {
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
