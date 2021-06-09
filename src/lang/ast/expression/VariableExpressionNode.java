package lang.ast.expression;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;

import java.util.List;

public class VariableExpressionNode extends PrimaryExpressionNode {

    private final IdentifierNode identifierNode;

    public VariableExpressionNode(IdentifierNode identifierNode) {
        this.identifierNode = identifierNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Variable: " + identifierNode.getName();
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }
}
