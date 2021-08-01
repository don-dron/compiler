package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.GlobalBasicType.REF_TYPE;

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

    @Override
    public String toString() {
        return "null";
    }
}
