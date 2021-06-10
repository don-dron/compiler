package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

public class BoolConstantExpressionNode extends PrimaryExpressionNode {

    private final boolean value;

    public BoolConstantExpressionNode(boolean value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Boolean: " + value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}
