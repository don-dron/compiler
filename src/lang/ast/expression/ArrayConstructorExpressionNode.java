package lang.ast.expression;

import lang.ast.AstNode;
import lang.ast.TypeNode;
import lang.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayConstructorExpressionNode extends ExpressionNode {
    private final TypeNode typeNode;
    private final List<ExpressionNode> sizeExpression;

    public ArrayConstructorExpressionNode(TypeNode typeNode, List<ExpressionNode> sizeExpression) {
        this.typeNode = typeNode;
        this.sizeExpression = sizeExpression;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    public List<ExpressionNode> getSizeExpression() {
        return sizeExpression;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ArrayConstructor:\n" +
                typeNode.astDebug(shift + 1) +
                sizeExpression.stream().map(n -> "\n" + n.astDebug(shift + 1))
                        .collect(Collectors.joining());
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return new ArrayList<>(sizeExpression);
    }

    @Override
    public String toString() {
        return typeNode.toString() + "[" + sizeExpression.toString() + "]";
    }
}
