package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.GlobalBasicType;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.AstNode.SHIFT;

public class StringConstantExpressionNode extends PrimaryExpressionNode {
    private final String value;

    public StringConstantExpressionNode(String value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "String: " + value;
    }

    public String getValue() {
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
