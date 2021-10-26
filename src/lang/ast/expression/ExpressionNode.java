package lang.ast.expression;

import lang.ast.AstNode;
import lang.ast.TypeNode;

public abstract class ExpressionNode extends AstNode {
    public TypeNode resultType;

    public void setResultType(TypeNode type) {
        this.resultType = type;
    }

    public TypeNode getResultType() {
        return resultType;
    }
}
