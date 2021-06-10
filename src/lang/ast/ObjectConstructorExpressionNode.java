package lang.ast;

import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ObjectConstructorExpressionNode extends ExpressionNode {
    private final ObjectTypeNode typeNode;
    private final ExpressionListNode expressionListNode;

    public ObjectConstructorExpressionNode(ObjectTypeNode typeNode, ExpressionListNode expressionListNode) {
        this.typeNode = typeNode;
        this.expressionListNode = expressionListNode;
    }

    public ObjectTypeNode getTypeNode() {
        return typeNode;
    }

    public ExpressionListNode getExpressionListNode() {
        return expressionListNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ObjectConstructor:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                expressionListNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}
