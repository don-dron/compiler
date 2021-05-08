package com.compiler.ir;

import com.compiler.ast.AstNode;
import com.compiler.ast.FunctionNode;
import com.compiler.ast.FunctionsNode;
import com.compiler.ast.IdentifierNode;
import com.compiler.ast.expression.*;
import com.compiler.ast.statement.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Driver {
    public static int count = 0;
    private static final String RET_VAL = "$ret_val";

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
        functionScope.addVariable(RET_VAL, functionNode.getTypeNode().getType(), entry);

        CompoundStatementNode compoundStatementNode = (CompoundStatementNode) functionNode.getStatementNode();
        compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, functionScope, d));

        System.out.println(functionScope.treeDebug(0));
        System.out.println(blocksToString(functionBlock.getBlocks()));

        return functionBlock;
    }

    private static String blocksToString(List<BasicBlock> blocks) {
        return blocks.stream().map(Driver::blockToString).collect(Collectors.joining("\n"));
    }

    private static String blockToString(BasicBlock basicBlock) {
        String s = basicBlock.getName() + ":\n";
        s += basicBlock.getOperations().stream()
                .map(Driver::operationToString)
                .map(t -> "\t" + t)
                .collect(Collectors.joining("\n"));
        return s;
    }

    private static String operationToString(Operation operation) {
        return operation.toString();
    }

    private static void driveStatement(FunctionBlock functionBlock,
                                       Scope scope,
                                       StatementNode statementNode) {
        if (statementNode instanceof BreakStatementNode) {
            // add break
        } else if (statementNode instanceof CompoundStatementNode) {
            Scope innerScope = new Scope(scope);
            scope.addScope(innerScope);

            CompoundStatementNode node = (CompoundStatementNode) statementNode;
            node.getStatements().forEach(n -> driveStatement(functionBlock, innerScope, n));
        } else if (statementNode instanceof ContinueStatementNode) {
            // add continue
        } else if (statementNode instanceof DeclarationStatementNode) {
            DeclarationStatementNode declarationStatementNode = (DeclarationStatementNode) statementNode;
            Variable variable = scope.addVariable(
                    declarationStatementNode.getIdentifierNode().getName(),
                    declarationStatementNode.getTypeNode().getType(),
                    functionBlock.getCurrentBlock()
            );

            functionBlock.getCurrentBlock().addDefine(variable);

            if (declarationStatementNode.getExpressionNode() != null) {
                driveExpression(functionBlock, scope,
                        new AssigmentExpressionNode(declarationStatementNode.getIdentifierNode(),
                                declarationStatementNode.getExpressionNode()));
            }
        } else if (statementNode instanceof ExpressionStatementNode) {
            ExpressionStatementNode expressionStatementNode = (ExpressionStatementNode) statementNode;
            driveExpression(functionBlock, scope, expressionStatementNode.getExpressionNode());
        } else if (statementNode instanceof EmptyStatementNode) {
        } else if (statementNode instanceof ForStatementNode) {
            ForStatementNode forStatementNode = (ForStatementNode) statementNode;

            Scope innerScope = new Scope(scope);
            scope.addScope(innerScope);

            if (forStatementNode.getPrev() != null) {
                driveStatement(functionBlock, innerScope, forStatementNode.getPrev());
            }

//            functionBlock.appendBlock("for_previous");
//            functionBlock.appendBlock("for_condition");
//            functionBlock.appendBlock("for_step");
//            functionBlock.appendBlock("for_body");
//            functionBlock.appendBlock("for_merge");

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

//            functionBlock.appendBlock("if_condition");
//            functionBlock.appendBlock("if_then");
//            functionBlock.appendBlock("if_else");
//            functionBlock.appendBlock("if_merge");

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
            ReturnStatementNode returnStatementNode = (ReturnStatementNode) statementNode;
//            functionBlock.appendBlock("local_return");

            if (returnStatementNode.getExpressionNode() != null) {
                driveExpression(functionBlock, scope,
                        new AssigmentExpressionNode(new IdentifierNode(RET_VAL),
                                ((ReturnStatementNode) statementNode).getExpressionNode()));
            }
        } else {
            throw new IllegalStateException("Unknown statement " + statementNode);
        }
    }

    private static Operation driveExpression(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof ConditionalExpressionNode) {
            ConditionalExpressionNode conditionalExpressionNode = (ConditionalExpressionNode) expressionNode;
            LoadOperation loadOperation = new LoadOperation(null, null);
            functionBlock.getCurrentBlock().addOperation(loadOperation);
            return loadOperation;
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            LogicalOrExpressionNode logicalOrExpressionNode = (LogicalOrExpressionNode) expressionNode;

            LoadOperation first = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);

            LoadOperation second = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(second);
            return first;
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            LogicalAndExpressionNode logicalAndExpressionNode = (LogicalAndExpressionNode) expressionNode;

            LoadOperation first = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);

            LoadOperation second = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(second);
            return first;
        } else if (expressionNode instanceof EqualityExpressionNode) {
            EqualityExpressionNode equalityExpressionNode = (EqualityExpressionNode) expressionNode;
            String type;

            switch (equalityExpressionNode.getType()) {
                case EQ:
                    type = " == ";
                    break;
                case NE:
                default:
                    type = " != ";
            }
            LoadOperation first = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);

            LoadOperation second = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(second);
            return first;
        } else if (expressionNode instanceof RelationalExpressionNode) {
            RelationalExpressionNode relationalExpressionNode = (RelationalExpressionNode) expressionNode;
            String type;

            switch (relationalExpressionNode.getType()) {
                case GE:
                    type = " >= ";
                    break;
                case GT:
                    type = " > ";
                    break;
                case LE:
                    type = " <= ";
                    break;
                case LT:
                default:
                    type = " < ";
            }
            LoadOperation first = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);

            LoadOperation second = new LoadOperation(null,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(second);
            return first;
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            AdditiveExpressionNode additiveExpressionNode = (AdditiveExpressionNode) expressionNode;
            Value source = null;

            if (isTerm(additiveExpressionNode.getFirst())) {
                source = driveValue(functionBlock, scope, additiveExpressionNode.getFirst());
            } else {
                source = driveExpression(functionBlock, scope, additiveExpressionNode.getFirst()).getResult();
            }
            LoadOperation first = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);


            source = null;
            if (isTerm(additiveExpressionNode.getSecond())) {
                source = driveValue(functionBlock, scope, additiveExpressionNode.getSecond());
            } else {
                source = driveExpression(functionBlock, scope, additiveExpressionNode.getSecond()).getResult();
            }
            LoadOperation second = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );

            functionBlock.getCurrentBlock().addOperation(second);

            Operation operation = null;
            switch (additiveExpressionNode.getType()) {
                case ADD:
                    operation = new BinOperation(BinOpType.ADD, first.getTarget(), second.getTarget(),
                            new VariableValue(new Variable(genNext(),
                                    Type.INT,
                                    scope,
                                    functionBlock.getCurrentBlock())));
                    break;
                case SUB:
                default:
                    operation = new BinOperation(BinOpType.SUB, first.getTarget(), second.getTarget(),
                            new VariableValue(new Variable(genNext(),
                                    Type.INT,
                                    scope,
                                    functionBlock.getCurrentBlock())));
            }
            functionBlock.getCurrentBlock().addOperation(operation);
            return operation;
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            MultiplicativeExpressionNode multiplicativeExpressionNode = (MultiplicativeExpressionNode) expressionNode;
            Value source = null;

            if (isTerm(multiplicativeExpressionNode.getFirst())) {
                source = driveValue(functionBlock, scope, multiplicativeExpressionNode.getFirst());
            } else {
                source = driveExpression(functionBlock, scope, multiplicativeExpressionNode.getFirst()).getResult();
            }
            LoadOperation first = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );
            functionBlock.getCurrentBlock().addOperation(first);

            source = null;
            if (isTerm(multiplicativeExpressionNode.getSecond())) {
                source = driveValue(functionBlock, scope, multiplicativeExpressionNode.getSecond());
            } else {
                source = driveExpression(functionBlock, scope, multiplicativeExpressionNode.getSecond()).getResult();
            }
            LoadOperation second = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), Type.INT, scope, functionBlock.getCurrentBlock()))
            );

            functionBlock.getCurrentBlock().addOperation(second);

            Operation operation = null;
            switch (multiplicativeExpressionNode.getType()) {
                case MUL:
                    operation = new BinOperation(BinOpType.MUL, first.getTarget(), second.getTarget(),
                            new VariableValue(new Variable(genNext(),
                                    Type.INT,
                                    scope,
                                    functionBlock.getCurrentBlock())));
                    break;
                case DIV:
                default:
                    operation = new BinOperation(BinOpType.DIV, first.getTarget(), second.getTarget(),
                            new VariableValue(new Variable(genNext(),
                                    Type.INT,
                                    scope,
                                    functionBlock.getCurrentBlock())));
            }
            functionBlock.getCurrentBlock().addOperation(operation);
            return operation;
        } else if (expressionNode instanceof AssigmentExpressionNode) {
            AssigmentExpressionNode assigmentExpressionNode = (AssigmentExpressionNode) expressionNode;
            Value source = null;

            if (isTerm(assigmentExpressionNode.getExpressionNode())) {
                source = driveValue(functionBlock, scope, assigmentExpressionNode.getExpressionNode());
            } else {
                source = driveExpression(functionBlock, scope, assigmentExpressionNode.getExpressionNode()).getResult();
            }

            StoreOperation storeOperation = new StoreOperation(
                    source,
                    variableValueByName(functionBlock, scope,
                            (assigmentExpressionNode.getIdentifierNode().getName())));
            functionBlock.getCurrentBlock().addOperation(storeOperation);
            return storeOperation;
        } else {
            throw new IllegalStateException("Unknown expression type " + expressionNode);
        }
    }

    private static VariableValue variableValueByName(FunctionBlock functionBlock, Scope scope, String name) {
        return new VariableValue(functionBlock.getScope().getVariable(functionBlock.getScope().getNewName(name)));
    }

    private static boolean isTerm(ExpressionNode expressionNode) {
        return expressionNode instanceof VariableExpressionNode ||
                expressionNode instanceof IntConstantExpressionNode ||
                expressionNode instanceof FloatConstantExpressionNode ||
                expressionNode instanceof BoolConstantExpressionNode;
    }

    private static Value driveValue(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof VariableExpressionNode) {
            return variableValueByName(functionBlock, scope,
                    ((VariableExpressionNode) expressionNode).getIdentifierNode().getName());
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            return new IntValue(((IntConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            return new BoolValue(((BoolConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            return new FloatValue(((FloatConstantExpressionNode) expressionNode).getValue());
        } else {
            throw new IllegalStateException("Unknown value type " + expressionNode);
        }
    }

    private static String genNext() {
        return "%" + count++;
    }

    private static String expressionToString(Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof ConditionalExpressionNode) {
            ConditionalExpressionNode conditionalExpressionNode = (ConditionalExpressionNode) expressionNode;
            return "(" + expressionToString(scope, conditionalExpressionNode.getConditionNode()) + " ? " +
                    expressionToString(scope, conditionalExpressionNode.getThenNode()) + " : " +
                    expressionToString(scope, conditionalExpressionNode.getElseNode()) + ")";
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            LogicalOrExpressionNode logicalOrExpressionNode = (LogicalOrExpressionNode) expressionNode;
            return "(" + expressionToString(scope, logicalOrExpressionNode.getFirst()) + " || "
                    + expressionToString(scope, logicalOrExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            LogicalAndExpressionNode logicalAndExpressionNode = (LogicalAndExpressionNode) expressionNode;
            return "(" + expressionToString(scope, logicalAndExpressionNode.getFirst()) + " && "
                    + expressionToString(scope, logicalAndExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof EqualityExpressionNode) {
            EqualityExpressionNode equalityExpressionNode = (EqualityExpressionNode) expressionNode;
            String type;

            switch (equalityExpressionNode.getType()) {
                case EQ:
                    type = " == ";
                    break;
                case NE:
                default:
                    type = " != ";
            }

            return "(" + expressionToString(scope, equalityExpressionNode.getFirst()) + type +
                    expressionToString(scope, equalityExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof RelationalExpressionNode) {
            RelationalExpressionNode relationalExpressionNode = (RelationalExpressionNode) expressionNode;
            String type;

            switch (relationalExpressionNode.getType()) {
                case GE:
                    type = " >= ";
                    break;
                case GT:
                    type = " > ";
                    break;
                case LE:
                    type = " <= ";
                    break;
                case LT:
                default:
                    type = " < ";
            }

            return "(" + expressionToString(scope, relationalExpressionNode.getFirst()) + type +
                    expressionToString(scope, relationalExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            AdditiveExpressionNode additiveExpressionNode = (AdditiveExpressionNode) expressionNode;
            String type;

            switch (additiveExpressionNode.getType()) {
                case ADD:
                    type = " + ";
                    break;
                case SUB:
                default:
                    type = " - ";
            }

            return "(" + expressionToString(scope, additiveExpressionNode.getFirst()) + type +
                    expressionToString(scope, additiveExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            MultiplicativeExpressionNode multiplicativeExpressionNode = (MultiplicativeExpressionNode) expressionNode;
            String type;

            switch (multiplicativeExpressionNode.getType()) {
                case MUL:
                    type = " * ";
                    break;
                case DIV:
                default:
                    type = " / ";
            }

            return "(" + expressionToString(scope, multiplicativeExpressionNode.getFirst()) + type +
                    expressionToString(scope, multiplicativeExpressionNode.getSecond()) + ")";
        } else if (expressionNode instanceof VariableExpressionNode) {
            return scope.getNewName(((VariableExpressionNode) expressionNode).getIdentifierNode().getName());
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            return String.valueOf(((IntConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            return String.valueOf(((BoolConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            return String.valueOf(((FloatConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof AssigmentExpressionNode) {
            AssigmentExpressionNode assigmentExpressionNode = (AssigmentExpressionNode) expressionNode;
            return scope.getNewName(assigmentExpressionNode.getIdentifierNode().getName()) + " <- " +
                    expressionToString(scope, assigmentExpressionNode.getExpressionNode());
        } else {
            throw new IllegalStateException("Unknown expression type " + expressionNode);
        }
    }
}
