package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.TypeNode;
import lang.ast.expression.ExpressionNode;

import java.util.List;

public class DeclarationStatementNode extends StatementNode {
    private final TypeNode typeNode;
    private final IdentifierNode identifierNode;
    private final ExpressionNode expressionNode;

    public DeclarationStatementNode(TypeNode typeNode, IdentifierNode identifierNode, ExpressionNode expressionNode) {
        super();
        this.typeNode = typeNode;
        this.identifierNode = identifierNode;
        this.expressionNode = expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "DeclarationStatement:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                identifierNode.astDebug(shift + 1) + "\n" +
                (expressionNode == null ? "" : expressionNode.astDebug(shift + 1))).stripTrailing();
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
