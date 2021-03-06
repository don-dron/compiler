package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.GlobalBasicType.FLOAT_TYPE;

public class FloatConstantExpressionNode extends PrimaryExpressionNode {
    private final float value;

    public FloatConstantExpressionNode(float value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Float: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return FLOAT_TYPE;
    }

    public float getValue() {
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
