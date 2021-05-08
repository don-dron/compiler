package com.compiler.ir;

import com.compiler.ast.AstNode;
import com.compiler.ast.FunctionNode;
import com.compiler.ast.FunctionsNode;
import com.compiler.ast.expression.ExpressionNode;
import com.compiler.ast.statement.CompoundStatementNode;
import com.compiler.ast.statement.IfStatementNode;
import com.compiler.ast.statement.ReturnStatementNode;
import com.compiler.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Driver {

    public static Module drive(FunctionsNode functionsNode) {
        return new Module(functionsNode.getFunctionNodes()
                .stream()
                .map(Driver::driveFunction)
                .collect(Collectors.toList()));
    }

    private static void walk(AstNode node, Consumer<AstNode> handler) {
        node.getChildren().forEach(n -> walk(n, handler));
    }

    private static Type calculateType(ExpressionNode expressionNode) {
        return null;
    }

    private static FunctionBlock driveFunction(FunctionNode functionNode) {
        FunctionBlock functionBlock = new FunctionBlock(
                functionNode.getIdentifierNode().getName(),
                functionNode.getTypeNode().getType());

        functionBlock.addVariables(functionNode.getParameterNode().getMap().entrySet().stream()
                .map(e -> new Variable(e.getKey().getName(), e.getValue().getType(), functionBlock))
                .collect(Collectors.toList()));

        driveStatement(functionBlock, functionNode.getStatementNode());

        return functionBlock;
    }

    private static void driveStatement(Scope scope, StatementNode statementNode) {
        if (statementNode instanceof CompoundStatementNode) {
            ((CompoundStatementNode) statementNode).getStatements()
                    .forEach(s -> driveStatement(scope, statementNode));
        } else if (statementNode instanceof IfStatementNode) {

        } else if(statementNode instanceof  )
    }
}
