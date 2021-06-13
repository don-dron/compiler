package lang.ast.expression.unary.postfix;

import lang.ast.AstNode;
import lang.ast.ExpressionListNode;
import lang.ast.expression.ExpressionNode;
import lang.ast.expression.PrimaryExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallExpressionNode extends PrimaryExpressionNode {
    private final ExpressionNode function;
    private final ExpressionListNode parameters;

    public FunctionCallExpressionNode(ExpressionNode function, ExpressionListNode parameters) {
        this.function = function;
        this.parameters = parameters;
    }

    public ExpressionNode getFunction() {
        return function;
    }

    public ExpressionListNode getParameters() {
        return parameters;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "FunctionCallExpression:\n" +
                function.astDebug(shift + 1) + "\n" +
                parameters.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(function);
        expressionNodes.addAll(parameters.getList());
        return expressionNodes;
    }

    @Override
    public String toString() {
        return function.toString() + "(" + parameters.toString() + ")";
    }
}
