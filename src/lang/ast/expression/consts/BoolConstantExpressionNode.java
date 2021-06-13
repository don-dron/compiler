package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.TypeNode.Type.BOOL;

public class BoolConstantExpressionNode extends PrimaryExpressionNode {
    public static final BasicTypeNode boolType = new BasicTypeNode(BOOL);
    private final boolean value;

    public BoolConstantExpressionNode(boolean value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Boolean: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return boolType;
    }

    public boolean getValue() {
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
