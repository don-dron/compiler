package com.compiler.ir;

import com.compiler.ast.AstNode;
import com.compiler.ast.FunctionNode;
import com.compiler.ast.FunctionsNode;
import com.compiler.ast.expression.ExpressionNode;
import com.compiler.ast.statement.*;

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
        if (handler != null) {
            handler.accept(node);
        }
        node.getChildren().forEach(n -> walk(n, handler));
    }

    private static void scopeWalk(Scope node, Consumer<Scope> handler) {
        if (handler != null) {
            handler.accept(node);
        }
        node.getChildren().forEach(n -> scopeWalk(n, handler));
    }

    private static Type calculateType(ExpressionNode expressionNode) {
        return null;
    }

    private static FunctionBlock driveFunction(FunctionNode functionNode) {
        Scope functionScope = new Scope(null);
        FunctionBlock functionBlock = new FunctionBlock(
                functionNode.getIdentifierNode().getName(),
                functionNode.getTypeNode().getType(),
                functionScope);

        BasicBlock entry = functionBlock.appendBlock("entry");

        List<Variable> variables = functionNode.getParameterNode().getMap().entrySet()
                .stream()
                .map(e -> functionScope.addVariable(e.getKey().getName(), e.getValue().getType(), entry))
                .collect(Collectors.toList());
        functionBlock.addDefines(variables);

        CompoundStatementNode compoundStatementNode = (CompoundStatementNode) functionNode.getStatementNode();
        compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, functionScope, d));

        System.out.println(functionScope.treeDebug(0));

        return functionBlock;
    }

    private static void driveStatement(FunctionBlock functionBlock,
                                       Scope scope,
                                       StatementNode statementNode) {
        if (statementNode instanceof BreakStatementNode) {

        } else if (statementNode instanceof CompoundStatementNode) {
            Scope innerScope = new Scope(scope);
            scope.addScope(innerScope);

            CompoundStatementNode node = (CompoundStatementNode) statementNode;
            node.getStatements().forEach(n -> driveStatement(functionBlock, innerScope, n));
        } else if (statementNode instanceof ContinueStatementNode) {

        } else if (statementNode instanceof DeclarationStatementNode) {
            DeclarationStatementNode declarationStatementNode = (DeclarationStatementNode) statementNode;
            scope.addVariable(
                    declarationStatementNode.getIdentifierNode().getName(),
                    declarationStatementNode.getTypeNode().getType(),
                    functionBlock.getCurrentBlock()
            );
        } else if (statementNode instanceof ExpressionStatementNode) {

        } else if (statementNode instanceof EmptyStatementNode) {

        } else if (statementNode instanceof ForStatementNode) {
            ForStatementNode forStatementNode = (ForStatementNode) statementNode;

            Scope innerScope = new Scope(scope);
            scope.addScope(innerScope);

            driveStatement(functionBlock, innerScope, forStatementNode.getPrev());

            if (forStatementNode.getBody() instanceof CompoundStatementNode) {
                CompoundStatementNode compoundStatementNode = (CompoundStatementNode) forStatementNode.getBody();
                compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, innerScope, d));
            } else {
                driveStatement(functionBlock, innerScope, forStatementNode.getBody());
            }
        } else if (statementNode instanceof IfStatementNode) {
            IfStatementNode ifStatementNode = (IfStatementNode) statementNode;

            if (ifStatementNode.getThenNode() instanceof CompoundStatementNode) {
                Scope thenScope = new Scope(scope);
                scope.addScope(thenScope);
                CompoundStatementNode compoundStatementNode = (CompoundStatementNode) ifStatementNode.getThenNode();
                compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, thenScope, d));
            } else {
                driveStatement(functionBlock, scope, ifStatementNode.getThenNode());
            }

            if (ifStatementNode.getElseNode() != null) {
                if (ifStatementNode.getElseNode() instanceof CompoundStatementNode) {
                    Scope elseScope = new Scope(scope);
                    scope.addScope(elseScope);
                    CompoundStatementNode compoundStatementNode = (CompoundStatementNode) ifStatementNode.getElseNode();
                    compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, elseScope, d));
                } else {
                    driveStatement(functionBlock, scope, ifStatementNode.getElseNode());
                }
            }
        } else if (statementNode instanceof ReturnStatementNode) {

        } else {
            throw new IllegalStateException("Unknown statement " + statementNode);
        }
    }
}
