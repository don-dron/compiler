package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.TypeNode.Type.FLOAT;

public class FloatConstantExpressionNode extends PrimaryExpressionNode {
    public static final BasicTypeNode floatType = new BasicTypeNode(FLOAT);

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
        return floatType;
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
