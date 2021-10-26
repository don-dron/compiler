package lang.ast.expression.consts;

import lang.Position;
import lang.ast.AstNode;
import lang.ast.TypeNode;
import lang.ast.expression.PrimaryExpressionNode;
import lang.lexer.Token;

import java.util.List;

import static lang.ast.GlobalBasicType.BOOL_TYPE;

public class BoolConstantExpressionNode extends PrimaryExpressionNode {
    private final boolean value;
    private final Token token;

    public BoolConstantExpressionNode(boolean value, Token token) {
        this.value = value;
        this.token = token;
    }

    @Override
    public Position getStart() {
        return token.getStart();
    }

    @Override
    public Position getEnd() {
        return token.getEnd();
    }

    @Override
    public String getFormattedText() {
        return token.getContent();
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Boolean: " + value;
    }

    @Override
    public TypeNode getResultType() {
        return BOOL_TYPE;
    }

    public boolean getValue() {
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
