package com.compiler.ast;

import com.compiler.ast.statement.StatementNode;

import java.util.Arrays;
import java.util.List;

public class FunctionNode extends AstNode {
    private final TypeNode typeNode;
    private final IdentifierNode identifierNode;
    private final ParameterNode parameterNode;
    private final StatementNode statementNode;

    public FunctionNode(TypeNode typeNode,
                        IdentifierNode identifierNode,
                        ParameterNode parameterNode,
                        StatementNode statementNode) {
        this.typeNode = typeNode;
        this.identifierNode = identifierNode;
        this.parameterNode = parameterNode;
        this.statementNode = statementNode;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public ParameterNode getParameterNode() {
        return parameterNode;
    }

    public StatementNode getStatementNode() {
        return statementNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift + 1) + "Function: " +
                identifierNode.getName() + "\n" +
                typeNode.astDebug(shift + 2) + "\n" +
                parameterNode.astDebug(shift + 2) + "\n" +
                statementNode.astDebug(shift + 2);
    }

    @Override
    public List<AstNode> getChildren() {
        return Arrays.asList(statementNode);
    }
}
