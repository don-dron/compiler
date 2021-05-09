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
    private static final String RET_VAL = "ret$val";

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

        BasicBlock skip = functionBlock.appendBlock("skip");

        List<Variable> variables = functionNode.getParameterNode().getMap().entrySet()
                .stream()
                .map(e -> functionScope.addVariable(e.getKey().getName(), e.getValue().getType(), skip))
                .collect(Collectors.toList());
        functionBlock.addDefines(variables);
        Variable retValue = functionScope.addVariable(RET_VAL, functionNode.getTypeNode().getType(), skip);

        skip.addOperation(new AllocationOperation(retValue));

        BasicBlock returnBlock = functionBlock.appendBlock("return");
        Variable variable = new Variable(genNext(), functionNode.getTypeNode().getType(), functionScope,
                returnBlock, true);
        VariableValue variableValue = new VariableValue(variable);
        returnBlock.addOperation(new LoadOperation(
                new VariableValue(retValue),
                variableValue
        ));
        returnBlock.setTerminator(new Return(variableValue));

        BasicBlock entry = functionBlock.appendBlock("entry");
        skip.setTerminator(new Branch(entry));
        entry.setTerminator(new Branch(returnBlock));

        functionBlock.setReturnBlock(returnBlock);

        CompoundStatementNode compoundStatementNode = (CompoundStatementNode) functionNode.getStatementNode();
        compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, functionScope, d));

        System.out.println(functionScope.treeDebug(0));
        System.out.println(functionToString(functionBlock));

        return functionBlock;
    }

    private static String functionToString(FunctionBlock functionBlock) {
        String s = "; ModuleID = 'main'\n" +
                "source_filename = \"main\"\n" +
                "define " + functionBlock.getReturnType().toCode() + " @" +
                functionBlock.getFunctionName() + "() {\n";
        s += blocksToString(functionBlock.getBlocks());
        s += "\n}";
        return s;
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
        s = s.stripTrailing();
        if (basicBlock.getTerminator() != null) {
            s += "\n\t" + basicBlock.getTerminator().toString();
        }
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

            functionBlock.getCurrentBlock().addOperation(new AllocationOperation(variable));

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

            BasicBlock last = functionBlock.getCurrentBlock();
            BasicBlock previous = null;
            BasicBlock condition = null;
            BasicBlock step = null;
            BasicBlock body = null;
            BasicBlock merge = null;

            if (forStatementNode.getPrev() != null) {
                previous = functionBlock.appendBlock("for_previous");
                driveStatement(functionBlock, innerScope, forStatementNode.getPrev());
            }

            Operation operation = null;
            if (forStatementNode.getPredicate() != null) {
                condition = functionBlock.appendBlock("for_condition");
                operation = driveExpression(functionBlock, innerScope, forStatementNode.getPredicate());
            }

            if (forStatementNode.getStep() != null) {
                step = functionBlock.appendBlock("for_step");
                driveStatement(functionBlock, innerScope, new ExpressionStatementNode(forStatementNode.getStep()));
            }

            body = functionBlock.appendBlock("for_body");
            if (forStatementNode.getBody() instanceof CompoundStatementNode) {
                CompoundStatementNode compoundStatementNode = (CompoundStatementNode) forStatementNode.getBody();
                compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, innerScope, d));
            } else {
                driveStatement(functionBlock, innerScope, forStatementNode.getBody());
            }

            BasicBlock endBody = functionBlock.getCurrentBlock();

            merge = functionBlock.appendBlock("for_merge");

            if (previous == null) {
                if (condition == null) {
                    if (step == null) {
                        last.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(body));
                    } else {
                        last.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(step));
                        step.setTerminator(new Branch(body));
                    }
                } else {
                    if (step == null) {
                        last.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(condition));
                        condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                    } else {
                        last.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(step));
                        condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                        step.setTerminator(new Branch(condition));
                    }
                }
            } else {
                last.setTerminator(new Branch(previous));

                if (condition == null) {
                    if (step == null) {
                        previous.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(body));
                    } else {
                        previous.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(step));
                        step.setTerminator(new Branch(body));
                    }
                } else {
                    if (step == null) {
                        previous.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(condition));
                        condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                    } else {
                        previous.setTerminator(new Branch(body));
                        endBody.setTerminator(new Branch(step));
                        condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                        step.setTerminator(new Branch(condition));
                    }
                }
            }
        } else if (statementNode instanceof IfStatementNode) {
            IfStatementNode ifStatementNode = (IfStatementNode) statementNode;

            BasicBlock condition = functionBlock.appendBlock("if_condition");
            BasicBlock thenBlock = null;
            BasicBlock elseBlock = null;
            BasicBlock mergeBlock = null;

            Operation operation = driveExpression(functionBlock, scope, ifStatementNode.getConditionNode());

            thenBlock = functionBlock.appendBlock("if_then");
            if (ifStatementNode.getThenNode() instanceof CompoundStatementNode) {
                Scope thenScope = new Scope(scope);
                scope.addScope(thenScope);
                CompoundStatementNode compoundStatementNode = (CompoundStatementNode) ifStatementNode.getThenNode();
                compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, thenScope, d));
            } else {
                driveStatement(functionBlock, scope, ifStatementNode.getThenNode());
            }

            BasicBlock endThen = functionBlock.getCurrentBlock();
            BasicBlock endElse = null;
            if (ifStatementNode.getElseNode() != null) {
                elseBlock = functionBlock.appendBlock("if_else");
                if (ifStatementNode.getElseNode() instanceof CompoundStatementNode) {
                    Scope elseScope = new Scope(scope);
                    scope.addScope(elseScope);
                    CompoundStatementNode compoundStatementNode = (CompoundStatementNode) ifStatementNode.getElseNode();
                    compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, elseScope, d));
                } else {
                    driveStatement(functionBlock, scope, ifStatementNode.getElseNode());
                }
                endElse = functionBlock.getCurrentBlock();
            }

            mergeBlock = functionBlock.appendBlock("if_merge");

            endThen.setTerminator(new Branch(mergeBlock));

            if (elseBlock != null) {
                condition.setTerminator(new ConditionalBranch(operation.getResult(), thenBlock, elseBlock));
                endElse.setTerminator(new Branch(mergeBlock));
            } else {
                condition.setTerminator(new ConditionalBranch(operation.getResult(), thenBlock, mergeBlock));
            }
        } else if (statementNode instanceof ReturnStatementNode) {
            ReturnStatementNode returnStatementNode = (ReturnStatementNode) statementNode;
            BasicBlock last = functionBlock.getCurrentBlock();
            BasicBlock retBlock = functionBlock.appendBlock("local_return");
            last.setTerminator(new Branch(retBlock));

            if (returnStatementNode.getExpressionNode() != null) {
                driveExpression(functionBlock, scope,
                        new AssigmentExpressionNode(new IdentifierNode(RET_VAL),
                                ((ReturnStatementNode) statementNode).getExpressionNode()));
            }

            retBlock.setTerminator(new Branch(functionBlock.getReturnBlock()));
        } else {
            throw new IllegalStateException("Unknown statement " + statementNode);
        }
    }

    private static Operation driveExpression(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof ConditionalExpressionNode) {
            return handleConditional(functionBlock, (ConditionalExpressionNode) expressionNode);
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            return handleOr(functionBlock, scope, (LogicalOrExpressionNode) expressionNode);
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            return handleAnd(functionBlock, scope, (LogicalAndExpressionNode) expressionNode);
        } else if (expressionNode instanceof EqualityExpressionNode) {
            return handleEquality(functionBlock, scope, (EqualityExpressionNode) expressionNode);
        } else if (expressionNode instanceof RelationalExpressionNode) {
            return handleRelational(functionBlock, scope, (RelationalExpressionNode) expressionNode);
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            return handleAdditive(functionBlock, scope, (AdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            return handleMultiplicative(functionBlock, scope, (MultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof AssigmentExpressionNode) {
            return handleAssigment(functionBlock, scope, (AssigmentExpressionNode) expressionNode);
        } else {
            throw new IllegalStateException("Unknown expression type " + expressionNode);
        }
    }

    private static LoadOperation handleConditional(FunctionBlock functionBlock, ConditionalExpressionNode expressionNode) {
        ConditionalExpressionNode conditionalExpressionNode = expressionNode;
        LoadOperation loadOperation = new LoadOperation(null, null);
        functionBlock.getCurrentBlock().addOperation(loadOperation);
        return loadOperation;
    }

    private static Operation handleOr(FunctionBlock functionBlock, Scope scope, LogicalOrExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getFirst())) {
            source = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
        }
        LoadOperation first = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );
        functionBlock.getCurrentBlock().addOperation(first);

        source = null;
        if (isTerm(expressionNode.getSecond())) {
            source = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
        }
        LoadOperation second = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );

        functionBlock.getCurrentBlock().addOperation(second);

        Operation operation = new BinOperation(BinOpType.OR, first.getTarget(), second.getTarget(),
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleAnd(FunctionBlock functionBlock, Scope scope, LogicalAndExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getFirst())) {
            source = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
        }
        LoadOperation first = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(), scope,
                        functionBlock.getCurrentBlock(), true))
        );
        functionBlock.getCurrentBlock().addOperation(first);


        source = null;
        if (isTerm(expressionNode.getSecond())) {
            source = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
        }
        LoadOperation second = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );

        functionBlock.getCurrentBlock().addOperation(second);

        Operation operation = new BinOperation(BinOpType.AND, first.getTarget(), second.getTarget(),
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleEquality(FunctionBlock functionBlock, Scope scope, EqualityExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getFirst())) {
            source = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
        }
        LoadOperation first = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );
        functionBlock.getCurrentBlock().addOperation(first);


        source = null;
        if (isTerm(expressionNode.getSecond())) {
            source = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
        }
        LoadOperation second = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );

        functionBlock.getCurrentBlock().addOperation(second);

        BinOpType binOpType = null;
        switch (expressionNode.getType()) {
            case EQ:
                binOpType = BinOpType.EQ;
                break;
            case NE:
            default:
                binOpType = BinOpType.NE;
                break;

        }
        Operation operation = new BinOperation(binOpType, first.getTarget(), second.getTarget(),
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleRelational(FunctionBlock functionBlock, Scope scope, RelationalExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getFirst())) {
            source = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
        }
        LoadOperation first = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );
        functionBlock.getCurrentBlock().addOperation(first);


        source = null;
        if (isTerm(expressionNode.getSecond())) {
            source = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
        }
        LoadOperation second = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );

        functionBlock.getCurrentBlock().addOperation(second);

        BinOpType binOpType = null;
        switch (expressionNode.getType()) {
            case LT:
                binOpType = BinOpType.LT;
                break;
            case GT:
                binOpType = BinOpType.GT;
                break;
            case LE:
                binOpType = BinOpType.LE;
                break;
            case GE:
            default:
                binOpType = BinOpType.GE;
        }
        Operation operation = new BinOperation(binOpType, first.getTarget(), second.getTarget(),
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static StoreOperation handleAssigment(FunctionBlock functionBlock, Scope scope, AssigmentExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getExpressionNode())) {
            source = driveValue(functionBlock, scope, expressionNode.getExpressionNode());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getExpressionNode()).getResult();
        }

        StoreOperation storeOperation = new StoreOperation(
                source,
                variableValueByName(functionBlock, scope,
                        (expressionNode.getIdentifierNode().getName())));
        functionBlock.getCurrentBlock().addOperation(storeOperation);
        return storeOperation;
    }

    private static Operation handleAdditive(FunctionBlock functionBlock, Scope scope, AdditiveExpressionNode expressionNode) {
        Value firstArg = null;
        Value secondArg = null;

        if (isTerm(expressionNode.getFirst())) {
            firstArg = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            Value source = null;
            if (isVariable(expressionNode.getFirst())) {
                source = driveValue(functionBlock, scope, expressionNode.getFirst());
            } else {
                source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
            }
            LoadOperation first = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), source.getType(),
                            scope, functionBlock.getCurrentBlock(), true))
            );
            functionBlock.getCurrentBlock().addOperation(first);
            firstArg = first.getTarget();
        }

        if (isTerm(expressionNode.getSecond())) {
            secondArg = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            Value source = null;
            if (isVariable(expressionNode.getSecond())) {
                source = driveValue(functionBlock, scope, expressionNode.getSecond());
            } else {
                source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
            }
            LoadOperation second = new LoadOperation(source,
                    new VariableValue(new Variable(genNext(), source.getType(),
                            scope, functionBlock.getCurrentBlock(), true))
            );
            functionBlock.getCurrentBlock().addOperation(second);
            secondArg = second.getTarget();
        }

        BinOpType binOpType;
        switch (expressionNode.getType()) {
            case ADD:
                binOpType = BinOpType.ADD;
                break;
            case SUB:
            default:
                binOpType = BinOpType.SUB;
        }
        Operation operation = new BinOperation(binOpType, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        binOperationType(firstArg, secondArg),
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleMultiplicative(FunctionBlock functionBlock, Scope scope, MultiplicativeExpressionNode expressionNode) {
        Value source = null;

        if (isTerm(expressionNode.getFirst())) {
            source = driveValue(functionBlock, scope, expressionNode.getFirst());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getFirst()).getResult();
        }
        LoadOperation first = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );
        functionBlock.getCurrentBlock().addOperation(first);


        source = null;
        if (isTerm(expressionNode.getSecond())) {
            source = driveValue(functionBlock, scope, expressionNode.getSecond());
        } else {
            source = driveExpression(functionBlock, scope, expressionNode.getSecond()).getResult();
        }
        LoadOperation second = new LoadOperation(source,
                new VariableValue(new Variable(genNext(), source.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );

        functionBlock.getCurrentBlock().addOperation(second);

        BinOpType binOpType;
        switch (expressionNode.getType()) {
            case MUL:
                binOpType = BinOpType.MUL;
                break;
            case DIV:
            default:
                binOpType = BinOpType.DIV;
        }
        Operation operation = new BinOperation(binOpType, first.getTarget(), second.getTarget(),
                new VariableValue(new Variable(genNext(),
                        binOperationType(first.getTarget(), second.getTarget()),
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Type binOperationType(Value first, Value second) {
        return first.getType() == Type.INT && second.getType() == Type.INT ? Type.INT : Type.FLOAT;
    }

    private static VariableValue variableValueByName(FunctionBlock functionBlock, Scope scope, String name) {
        return new VariableValue(scope.getVariable(scope.getNewName(name)));
    }

    private static boolean isVariable(ExpressionNode expressionNode) {
        return expressionNode instanceof VariableExpressionNode;
    }

    private static boolean isTerm(ExpressionNode expressionNode) {
        return expressionNode instanceof IntConstantExpressionNode ||
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
        return String.valueOf(count++);
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
