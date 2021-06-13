package lang.ast.expression.consts;

import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.List;

import static lang.ast.TypeNode.Type.INT;
import static lang.ast.TypeNode.Type.REFERENCE;

public class NullConstantExpressionNode extends PrimaryExpressionNode {
    public static final BasicTypeNode refType = new BasicTypeNode(REFERENCE);
    public NullConstantExpressionNode() {
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Null: ";
    }

    @Override
    public TypeNode getResultType() {
        return refType;
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
