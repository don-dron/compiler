package com.compiler.ast.statement;

import com.compiler.ast.AstNode;
import com.compiler.ast.IdentifierNode;
import com.compiler.ast.TypeNode;
import com.compiler.ast.expression.ExpressionNode;

import java.util.List;

public class DeclarationStatementNode extends StatementNode {
    private TypeNode typeNode;
    private IdentifierNode identifierNode;
    private ExpressionNode expressionNode;

    public DeclarationStatementNode(TypeNode typeNode, IdentifierNode identifierNode, ExpressionNode expressionNode) {
        super();
        this.typeNode = typeNode;
        this.identifierNode = identifierNode;
        this.expressionNode = expressionNode;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Declaration:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                identifierNode.astDebug(shift + 1) + "\n" +
                (expressionNode == null ? "" : expressionNode.astDebug(shift + 1))).stripTrailing();
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of(expressionNode);
    }
}
