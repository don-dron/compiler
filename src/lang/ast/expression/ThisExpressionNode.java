package lang.ast.expression;

import lang.ast.AstNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class ThisExpressionNode extends ExpressionNode {

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ThisExpression: ";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

}
