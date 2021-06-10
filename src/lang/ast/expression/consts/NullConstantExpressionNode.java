package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

public class NullConstantExpressionNode extends PrimaryExpressionNode {
    public NullConstantExpressionNode() {
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Null: ";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}
