package com.compiler.ir.drive;

import com.compiler.ast.FunctionNode;
import com.compiler.ast.expression.*;
import com.compiler.ast.statement.*;
import com.compiler.ir.BasicBlock;
import com.compiler.ir.FunctionBlock;
import com.compiler.ir.Module;
import com.compiler.ir.Scope;
import com.compiler.ir.drive.operation.*;
import com.compiler.ir.drive.terminator.Branch;
import com.compiler.ir.drive.terminator.ConditionalBranch;
import com.compiler.ir.drive.terminator.Return;
import com.compiler.ir.drive.value.*;
import com.compiler.ir.optimization.SSAFormBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.compiler.ir.cfg.DeadCodeElimination.*;
import static com.compiler.ir.cfg.DominanceFrontierFinder.setDominanceFrontier;
import static com.compiler.ir.cfg.DominatorsFinder.setDominators;
import static com.compiler.ir.cfg.ImmediateDominatorsFinder.setImmediateDominators;

public final class Driver {
    private static final boolean ALL_DOMS = false;
    private final String RET_VAL = "ret$val";
    private int count = 0;
    private static Map<String, Type> functionsToType = new HashMap<>();

    private int condCount = 0;
    private final Stack<BasicBlock> forStack = new Stack<>();

    private final Map<BasicBlock, BasicBlock> forToNext = new HashMap<>();
    private final Map<BasicBlock, BasicBlock> forToMerge = new HashMap<>();
    private final Map<BasicBlock, BasicBlock> breakToFor = new HashMap<>();
    private final Map<BasicBlock, BasicBlock> continueToFor = new HashMap<>();

    public FunctionBlock driveFunction(FunctionNode functionNode) {
        Scope functionScope = new Scope(null);
        FunctionBlock functionBlock = new FunctionBlock(
                functionNode.getIdentifierNode().getName(),
                functionNode.getTypeNode().getType(),
                functionScope);

        functionsToType.put(functionBlock.getFunctionName(), functionBlock.getReturnType());

        BasicBlock skip = functionBlock.appendBlock("skip");

        List<Variable> variables = functionNode.getParameterNode().getMap().entrySet()
                .stream()
                .map(e -> {
                    Variable v = functionScope.addVariable(e.getKey().getName(), e.getValue().getType(), skip);
                    skip.addOperation(new AllocationOperation(v));
                    skip.addOperation(new StoreOperation(
                            new VariableValue(new Variable(genNext(),
                                    v.getType(),
                                    functionScope,
                                    functionBlock.getCurrentBlock(), true)),
                            new VariableValue(v)
                    ));
                    return v;
                }).collect(Collectors.toList());

        functionBlock.setParameters(variables);
        BasicBlock returnBlock = null;
        if (functionNode.getTypeNode().getType() != Type.VOID) {
            Variable retValue = functionScope.addVariable(RET_VAL,
                    functionNode.getTypeNode().getType(),
                    skip);
            functionBlock.setRetValue(retValue);
            skip.addOperation(new AllocationOperation(retValue));

            returnBlock = functionBlock.appendBlock("return");
            Variable variable = new Variable(genNext(), functionNode.getTypeNode().getType(), functionScope,
                    returnBlock, true);

            VariableValue variableValue = new VariableValue(variable);
            returnBlock.addOperation(new LoadOperation(
                    new VariableValue(retValue),
                    variableValue
            ));

            returnBlock.setTerminator(new Return(variableValue));
        } else {
            returnBlock = functionBlock.appendBlock("return");
            returnBlock.setTerminator(new Return(null));
        }

        BasicBlock entry = functionBlock.appendBlock("entry");
        skip.setTerminator(new Branch(entry));
        entry.setTerminator(new Branch(returnBlock));
        functionBlock.setReturnBlock(returnBlock);

        CompoundStatementNode compoundStatementNode = (CompoundStatementNode) functionNode.getStatementNode();
        compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, functionScope, d));

        functionBlock.getCurrentBlock().setTerminator(new Branch(functionBlock.getReturnBlock()));

        breakToFor.forEach((key1, value1) -> key1.setTerminator(new Branch(forToMerge.get(value1))));
        continueToFor.forEach((key, value) -> key.setTerminator(new Branch(forToNext.get(value))));

        removeEmptyBlocks(functionBlock);
        removeOneDirectBranches(functionBlock);

        BasicBlock root = buildCfgGraph(functionBlock);

        paintDeadCode(functionBlock.getBlocks(), root);
        removeDeadCode(functionBlock.getBlocks(), root);
        setDominators(functionBlock.getBlocks(), root);
        setImmediateDominators(functionBlock.getBlocks(), root);
        setDominanceFrontier(functionBlock.getBlocks(), root);

        SSAFormBuilder ssaFormBuilder = new SSAFormBuilder();
        ssaFormBuilder.buildSsaForm(functionBlock.getBlocks(), root);

        return functionBlock;
    }

    public static String graphVizDebug(FunctionBlock functionBlock) {
        StringBuilder s = new StringBuilder("");

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            String body = basicBlock.getName() + ":\n";

            String dominanceFrontier = basicBlock.getDominanceFrontier().stream()
                    .map(BasicBlock::getName)
                    .collect(Collectors.joining("\n"));

            String ssaForm = basicBlock.getPhiFunctions().entrySet()
                    .stream()
                    .sorted(Comparator.comparing(a -> a.getKey().getName()))
                    .map(e -> e.getValue().toString())
                    .collect(Collectors.joining("\n")) + "\n" + blockToSsaBody(basicBlock);
            s.append("\"")
                    .append(basicBlock.getName())
                    .append("\"")
                    .append(" ")
                    .append("[fillcolor=")
                    .append(basicBlock.isDead() ? "grey" : "white")
                    .append(", style=filled, shape=box, label=\"")
                    .append(body);
            s.append(ssaForm)
                    .append("\n\n")
                    .append(dominanceFrontier)
                    .append("\"];\n");
        }

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            for (BasicBlock other : basicBlock.getOutput()) {
                s.append("\"")
                        .append(basicBlock.getName())
                        .append("\"")
                        .append(" -> ")
                        .append("\"")
                        .append(other.getName())
                        .append("\";\n");
            }
        }

        if (ALL_DOMS) {
            for (BasicBlock basicBlock : functionBlock.getBlocks()) {
                for (BasicBlock other : basicBlock.getDominants()) {
                    s.append("\"")
                            .append(basicBlock.getName())
                            .append("\"")
                            .append(" -> ")
                            .append("\"")
                            .append(other.getName())
                            .append("\"")
                            .append("[color=blue,penwidth=3.0]")
                            .append(";\n");
                }
            }
        }

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            for (BasicBlock other : basicBlock.getDominants()) {
                if (other.getDominator() == basicBlock) {
                    s.append("\"")
                            .append(basicBlock.getName())
                            .append("\"")
                            .append(" -> ")
                            .append("\"")
                            .append(other.getName())
                            .append("\"")
                            .append("[color=red,penwidth=3.0]")
                            .append(";\n");
                }
            }
        }

        return s.toString();
    }

    private BasicBlock buildCfgGraph(FunctionBlock functionBlock) {
        BasicBlock root = functionBlock.getBlocks().get(0);

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            if (basicBlock.getTerminator() instanceof Branch) {
                Branch branch = (Branch) basicBlock.getTerminator();

                basicBlock.addOutput(branch.getTarget());

                branch.getTarget().addInput(basicBlock);
            } else if (basicBlock.getTerminator() instanceof ConditionalBranch) {
                ConditionalBranch conditionalBranch = (ConditionalBranch) basicBlock.getTerminator();

                basicBlock.addOutput(conditionalBranch.getFirst());
                basicBlock.addOutput(conditionalBranch.getSecond());

                conditionalBranch.getFirst().addInput(basicBlock);
                conditionalBranch.getSecond().addInput(basicBlock);
            }
        }

        return root;
    }

    public static String moduleToString(Module module) {
        String s = "; ModuleID = 'main'\n" +
                "source_filename = \"main\"\n" +
                "declare i32 @putchar(i32)\n" +
                "declare i32 @getchar()\n";
        return s + module.getFunctionBlocks()
                .stream()
                .map(Driver::functionToString)
                .collect(Collectors.joining("\n"));
    }

    private static String functionToString(FunctionBlock functionBlock) {
        String s = "define " + functionBlock.getReturnType().toCode() + " @" +
                functionBlock.getFunctionName() + "("
                + functionBlock.getParameters().stream()
                .map(v -> v.getType().toCode()).collect(Collectors.joining(","))
                + ") {\n";
        s += blocksToString(functionBlock.getBlocks());
        s += "\n}";
        return s;
    }

    private static String blocksToString(List<BasicBlock> blocks) {
        return blocks.stream()
                .filter(Predicate.not(BasicBlock::isDummy))
                .map(b -> b.getName() + ":\n" + blockToIRBody(b))
                .collect(Collectors.joining("\n"));
    }

    private static String blockToIRBody(BasicBlock basicBlock) {
        return blockToString(basicBlock, Driver::operationToString);
    }

    private static String blockToSsaBody(BasicBlock basicBlock) {
        return blockToString(basicBlock, Driver::operationSsaToString);
    }

    private static String blockToString(BasicBlock basicBlock, Function<Operation, String> operationStringFunction) {
        String s = "";
        s += basicBlock.getOperations().stream()
                .map(operationStringFunction)
                .map(t -> "\t" + t)
                .collect(Collectors.joining("\n"));
        s = s.stripTrailing();
        if (basicBlock.getTerminator() != null) {
            s += "\n\t" + basicBlock.getTerminator().toString();
        }
        return s;
    }

    private static String operationSsaToString(Operation operation) {
        return operation.hasSsaForm() ? operation.getSsa().toString() : operation.toString();
    }

    private static String operationToString(Operation operation) {
        return operation.toString();
    }

    private void driveStatement(FunctionBlock functionBlock,
                                Scope scope,
                                StatementNode statementNode) {
        if (statementNode instanceof BreakStatementNode) {
            BasicBlock last = functionBlock.getCurrentBlock();

            BasicBlock forBlock = forStack.peek();

            BasicBlock breakBlock = functionBlock.appendBlock("break");
            last.setTerminator(new Branch(breakBlock));

            breakToFor.put(breakBlock, forBlock);

            functionBlock.appendDummyBlock("dummy_block");
        } else if (statementNode instanceof CompoundStatementNode) {
            Scope innerScope = new Scope(scope);
            scope.addScope(innerScope);

            CompoundStatementNode node = (CompoundStatementNode) statementNode;
            node.getStatements().forEach(n -> driveStatement(functionBlock, innerScope, n));
        } else if (statementNode instanceof ContinueStatementNode) {
            BasicBlock last = functionBlock.getCurrentBlock();

            BasicBlock forBlock = forStack.peek();

            BasicBlock continueBlock = functionBlock.appendBlock("continue");
            last.setTerminator(new Branch(continueBlock));

            continueToFor.put(continueBlock, forBlock);

            functionBlock.appendDummyBlock("dummy_block");
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
            // Nothing
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
            forStack.push(body);

            if (forStatementNode.getBody() instanceof CompoundStatementNode) {
                CompoundStatementNode compoundStatementNode = (CompoundStatementNode) forStatementNode.getBody();
                compoundStatementNode.getStatements().forEach(d -> driveStatement(functionBlock, innerScope, d));
            } else {
                driveStatement(functionBlock, innerScope, forStatementNode.getBody());
            }

            BasicBlock endBody = functionBlock.getCurrentBlock();
            forStack.pop();
            merge = functionBlock.appendBlock("for_merge");

            if (previous != null) {
                last.setTerminator(new Branch(previous));
                last = previous;
            }

            if (condition == null) {
                if (step == null) {
                    last.setTerminator(new Branch(body));
                    endBody.setTerminator(new Branch(body));
                    forToNext.put(body, body);
                } else {
                    last.setTerminator(new Branch(body));
                    endBody.setTerminator(new Branch(step));
                    step.setTerminator(new Branch(body));
                    forToNext.put(body, step);
                }
            } else {
                if (step == null) {
                    last.setTerminator(new Branch(condition));
                    endBody.setTerminator(new Branch(condition));
                    condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                    forToNext.put(body, condition);
                } else {
                    last.setTerminator(new Branch(condition));
                    endBody.setTerminator(new Branch(step));
                    condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                    step.setTerminator(new Branch(condition));
                    forToNext.put(body, step);
                }
            }
            forToMerge.put(body, merge);
        } else if (statementNode instanceof IfStatementNode) {
            IfStatementNode ifStatementNode = (IfStatementNode) statementNode;

            BasicBlock last = functionBlock.getCurrentBlock();
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

            last.setTerminator(new Branch(condition));
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
            StoreOperation retStore = null;

            if (returnStatementNode.getExpressionNode() != null) {
                Value arg = driveArgument(functionBlock, scope, returnStatementNode.getExpressionNode());

                last = functionBlock.getCurrentBlock();
                retStore = new StoreOperation(
                        arg,
                        variableValueByName(functionBlock, scope, RET_VAL));
            }

            BasicBlock retBlock = functionBlock.appendBlock("local_return");

            if (retStore != null) {
                retBlock.addOperation(retStore);
            }


            last.setTerminator(new Branch(retBlock));

            retBlock.setTerminator(new Branch(functionBlock.getReturnBlock()));

            functionBlock.appendDummyBlock("dummy_block");
        } else {
            throw new IllegalStateException("Unknown statement " + statementNode);
        }
    }

    private Operation driveExpression(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof ConditionalExpressionNode) {
            return handleConditional(functionBlock, scope, ((ConditionalExpressionNode) expressionNode));
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
        } else if (expressionNode instanceof FunctionCallExpressionNode) {
            return handleFunctionCall(functionBlock, scope, (FunctionCallExpressionNode) expressionNode);
        } else {
            return null;
        }
    }

    private LoadOperation handleConditional(FunctionBlock functionBlock, Scope scope,
                                            ConditionalExpressionNode expressionNode) {
        BasicBlock last = functionBlock.getCurrentBlock();
        BasicBlock condition = functionBlock.appendBlock("conditional");
        BasicBlock thenBlock = null;
        BasicBlock elseBlock = null;
        BasicBlock mergeBlock = null;

        Variable variable = new Variable("cond$" + condCount++, Type.INT, scope, condition, false);
        VariableValue value = new VariableValue(variable);
        AllocationOperation allocationOperation = new AllocationOperation(variable);
        condition.addOperation(allocationOperation);
        last.setTerminator(new Branch(condition));

        Operation operation = driveExpression(functionBlock, scope, expressionNode.getConditionNode());

        BasicBlock endCondition = functionBlock.getCurrentBlock();

        thenBlock = functionBlock.appendBlock("conditional_then");
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getThenNode());
        BasicBlock endThen = functionBlock.getCurrentBlock();

        StoreOperation firstStore = new StoreOperation(firstArg, value);
        endThen.addOperation(firstStore);

        elseBlock = functionBlock.appendBlock("conditional_else");
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getElseNode());
        BasicBlock endElse = functionBlock.getCurrentBlock();

        StoreOperation secondStore = new StoreOperation(secondArg, value);
        endElse.addOperation(secondStore);

        mergeBlock = functionBlock.appendBlock("conditional_result");

        endThen.setTerminator(new Branch(mergeBlock));
        endElse.setTerminator(new Branch(mergeBlock));
        endCondition.setTerminator(new ConditionalBranch(operation.getResult(), thenBlock, elseBlock));

        LoadOperation loadOperation = new LoadOperation(value,
                new VariableValue(new Variable(genNext(), variable.getType(),
                        scope, functionBlock.getCurrentBlock(), true))
        );
        mergeBlock.addOperation(loadOperation);
        return loadOperation;
    }

    private Operation handleOr(FunctionBlock functionBlock, Scope scope, LogicalOrExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());
        Operation operation = new BinOperation(BinOpType.OR, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private Operation handleAnd(FunctionBlock functionBlock, Scope scope, LogicalAndExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());
        Operation operation = new BinOperation(BinOpType.AND, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private Operation handleEquality(FunctionBlock functionBlock, Scope scope, EqualityExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());

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
        Operation operation = new BinOperation(binOpType, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private Operation handleRelational(FunctionBlock functionBlock, Scope scope, RelationalExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());

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
        Operation operation = new BinOperation(binOpType, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private StoreOperation handleAssigment(FunctionBlock functionBlock, Scope scope, AssigmentExpressionNode assigmentExpressionNode) {
        Value arg = driveArgument(functionBlock, scope, assigmentExpressionNode.getExpressionNode());
        StoreOperation storeOperation = new StoreOperation(
                arg,
                variableValueByName(functionBlock, scope,
                        (assigmentExpressionNode.getIdentifierNode().getName())));
        functionBlock.getCurrentBlock().addOperation(storeOperation);
        return storeOperation;
    }

    private Operation handleAdditive(FunctionBlock functionBlock, Scope scope, AdditiveExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());

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

    private Operation handleMultiplicative(FunctionBlock functionBlock, Scope scope, MultiplicativeExpressionNode expressionNode) {
        Value firstArg = driveArgument(functionBlock, scope, expressionNode.getFirst());
        Value secondArg = driveArgument(functionBlock, scope, expressionNode.getSecond());

        BinOpType binOpType;
        switch (expressionNode.getType()) {
            case MUL:
                binOpType = BinOpType.MUL;
                break;
            case DIV:
                binOpType = BinOpType.DIV;
                break;
            case MOD:
            default:
                binOpType = BinOpType.MOD;
        }
        Operation operation = new BinOperation(binOpType, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        binOperationType(firstArg, secondArg),
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private Operation handleFunctionCall(FunctionBlock functionBlock, Scope scope, FunctionCallExpressionNode expressionNode) {
        List<Value> values = expressionNode.getParameters().getList().stream()
                .map(p -> driveArgument(functionBlock, scope, p))
                .collect(Collectors.toList());

        Type type = getFunctionType(expressionNode.getIdentifierNode().getName());
        Operation operation = new FunctionCallOperation(expressionNode.getIdentifierNode(), values,
                type == Type.VOID ? null :
                new VariableValue(new Variable(genNext(),
                        type,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private Type getFunctionType(String name) {
        return functionsToType.getOrDefault(name, Type.INT);
    }

    private Value driveArgument(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        Value value = null;
        if (isTerm(expressionNode)) {
            value = driveValue(functionBlock, scope, expressionNode);
        } else {
            Value source = null;
            if (isVariable(expressionNode)) {
                source = driveValue(functionBlock, scope, expressionNode);
            } else {
                source = driveExpression(functionBlock, scope, expressionNode).getResult();
            }

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                value = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                value = first.getTarget();
            }
        }

        return value;
    }

    private Type binOperationType(Value first, Value second) {
        return first.getType() == Type.INT && second.getType() == Type.INT ? Type.INT : Type.FLOAT;
    }

    private VariableValue variableValueByName(FunctionBlock functionBlock, Scope scope, String name) {
        return new VariableValue(scope.getVariable(scope.getNewName(name)));
    }

    private boolean isVariable(ExpressionNode expressionNode) {
        return expressionNode instanceof VariableExpressionNode;
    }

    private boolean isTerm(ExpressionNode expressionNode) {
        return expressionNode instanceof IntConstantExpressionNode ||
                expressionNode instanceof FloatConstantExpressionNode ||
                expressionNode instanceof BoolConstantExpressionNode;
    }

    private Value driveValue(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof VariableExpressionNode) {
            return variableValueByName(functionBlock, scope,
                    ((VariableExpressionNode) expressionNode).getIdentifierNode().getName());
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            return new IntValue(((IntConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            return new BoolValue(((BoolConstantExpressionNode) expressionNode).getValue());
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            return new FloatValue(((FloatConstantExpressionNode) expressionNode).getValue());
//        } else if(expressionNode instanceof  FunctionCallExpressionNode) {
//            return new
        } else {
            throw new IllegalStateException("Unknown value type " + expressionNode);
        }
    }

    private String genNext() {
        return String.valueOf(count++);
    }

    private String expressionToString(Scope scope, ExpressionNode expressionNode) {
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