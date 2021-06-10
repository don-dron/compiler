package lang.ast.expression;

import lang.ast.AstNode;

import java.util.List;

public class ConditionalExpressionNode extends ExpressionNode {
    private final ExpressionNode conditionNode;
    private final ExpressionNode thenNode;
    private final ExpressionNode elseNode;

    public ConditionalExpressionNode(ExpressionNode conditionNode, ExpressionNode thenNode, ExpressionNode elseNode) {
        super();
        this.conditionNode = conditionNode;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    public ExpressionNode getConditionNode() {
        return conditionNode;
    }

    public ExpressionNode getElseNode() {
        return elseNode;
    }

    public ExpressionNode getThenNode() {
        return thenNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ConditionalExpression:\n" +
                conditionNode.astDebug(shift + 1) + "\n" +
                thenNode.astDebug(shift + 1) + "\n" +
                elseNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(conditionNode, thenNode, elseNode);
    }
}
