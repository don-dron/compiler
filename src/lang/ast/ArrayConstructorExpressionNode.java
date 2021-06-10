package lang.ast;

import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ArrayConstructorExpressionNode extends ExpressionNode {
    private final TypeNode typeNode;
    private final ExpressionNode sizeExpression;

    public ArrayConstructorExpressionNode(TypeNode typeNode, ExpressionNode sizeExpression) {
        this.typeNode = typeNode;
        this.sizeExpression = sizeExpression;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    public ExpressionNode getSizeExpression() {
        return sizeExpression;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ArrayConstructor:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                sizeExpression.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(sizeExpression);
    }
}
