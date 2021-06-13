package lang.ast.expression.unary.postfix;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ArrayAccessExpressionNode extends ExpressionNode {
    private final ExpressionNode array;
    private final ExpressionNode argument;

    public ArrayAccessExpressionNode(ExpressionNode array, ExpressionNode argument) {
        this.array = array;
        this.argument = argument;
    }

    public ExpressionNode getArgument() {
        return argument;
    }

    public ExpressionNode getArray() {
        return array;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ArrayAccessExpression:\n" +
                array.astDebug(shift + 1) +
                (argument == null
                        ? ""
                        : ("\n" + argument.astDebug(shift + 1)));
    }

    @Override
    public String toString() {
        return array.toString() + "[" + argument.toString() + "]";
    }


    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(argument, array);
    }
}
