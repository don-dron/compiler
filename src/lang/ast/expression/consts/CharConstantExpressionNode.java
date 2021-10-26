package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.GlobalBasicType;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

public class CharConstantExpressionNode extends PrimaryExpressionNode {
    private final char value;

    public CharConstantExpressionNode(char  value) {
        this.value = value;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Char: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return GlobalBasicType.CHAR_TYPE;
    }

    public char  getValue() {
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
