package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.TypeNode.Type.INT;

public class IntConstantExpressionNode extends PrimaryExpressionNode {
    public static final BasicTypeNode intType = new BasicTypeNode(INT);

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
        return intType;
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
