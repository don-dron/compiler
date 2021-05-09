package com.compiler.ir;

import com.compiler.ast.AstNode;
import com.compiler.ast.FunctionNode;
import com.compiler.ast.FunctionsNode;
import com.compiler.ast.IdentifierNode;
import com.compiler.ast.expression.*;
import com.compiler.ast.statement.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Driver {
    public static int count = 0;
    public static int condCount = 0;
    private static final String RET_VAL = "ret$val";
    private static boolean allDoms = false;

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
        functionBlock.setRetValue(retValue);
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

        functionBlock.getCurrentBlock().setTerminator(new Branch(functionBlock.getReturnBlock()));

//        removeEmptyBlocks(functionBlock);

        BasicBlock root = buildCfgGraph(functionBlock);

        paintDeadCode(functionBlock.getBlocks(), root);
        removeDeadCode(functionBlock.getBlocks(), root);
        setDominators(functionBlock.getBlocks(), root);
        setImmediateDominators(functionBlock.getBlocks(), root);
        setDominanceFrontier(functionBlock.getBlocks(), root);

        buildSsaForm(functionBlock.getBlocks(), root);

        return functionBlock;
    }

    private static Set<Variable> getStores(BasicBlock block) {
        return block.getOperations()
                .stream()
                .filter(s -> s instanceof StoreOperation)
                .map(s -> (StoreOperation) s)
                .filter(s -> s.getTarget() instanceof VariableValue)
                .map(s -> (VariableValue) s.getTarget())
                .map(VariableValue::getVariable)
                .filter(variable -> !variable.isLocal())
                .collect(Collectors.toSet());
    }

    private static Set<Variable> getLoads(BasicBlock block) {
        return block.getOperations()
                .stream()
                .filter(s -> s instanceof LoadOperation)
                .map(s -> (LoadOperation) s)
                .filter(s -> s.getSource() instanceof VariableValue)
                .map(s -> (VariableValue) s.getSource())
                .map(VariableValue::getVariable)
                .filter(variable -> !variable.isLocal())
                .collect(Collectors.toSet());
    }

    private static void buildSsaForm(List<BasicBlock> blocks, BasicBlock root) {
        List<Variable> globals = new ArrayList<>();
        Map<Variable, List<BasicBlock>> vars = new HashMap<>();

        for (BasicBlock block : blocks) {
            for (Operation operation : block.getOperations()) {
                if (operation instanceof LoadOperation &&
                        ((LoadOperation) operation).getSource() instanceof VariableValue) {
                    VariableValue variableValue = (VariableValue) ((LoadOperation) operation).getSource();

                    if (!block.getSsaDefines().contains(variableValue.getVariable())) {
                        globals.add(variableValue.getVariable());
                    }
                } else if (operation instanceof StoreOperation &&
                        ((StoreOperation) operation).getTarget() instanceof VariableValue) {
                    VariableValue variableValue = (VariableValue) ((StoreOperation) operation).getTarget();
                    block.getSsaDefines().add(variableValue.getVariable());

                    List<BasicBlock> blockList = vars.
                            computeIfAbsent(variableValue.getVariable(), k -> new ArrayList<>());
                    vars.get(variableValue.getVariable()).add(block);
                }
            }
        }

        for (Variable variable : globals) {
            List<BasicBlock> workList = vars.get(variable);
            List<BasicBlock> garbage = new ArrayList<>();

            while (!workList.isEmpty()) {
                BasicBlock current = workList.remove(0);
                garbage.add(current);

                for (BasicBlock dfNode : current.getDominanceFrontier()) {
                    insertPhiFunction(variable, dfNode);

                    if (!garbage.contains(dfNode) && !workList.contains(dfNode)) {
                        workList.add(dfNode);
                    }
                }
            }
        }

        Map<Variable, Stack<String>> names = new HashMap<>();
        Map<Variable, Integer> counter = new HashMap<>();
        Function<Variable, String> topName = (v) -> {
            if (!names.containsKey(v)) {
                return v.getName();
            } else {
                return names.get(v).peek();
            }
        };

        Function<Variable, String> removeName = (v) -> {
            if (!names.containsKey(v)) {
                return v.getName();
            } else {
                return names.get(v).pop();
            }
        };

        Function<Variable, String> newNameFunction = (v) -> {
            if (!counter.containsKey(v)) {
                return v.getName();
            }
            int i = counter.get(v);

            String name = v.getName() + "_v." + i;
            i++;
            counter.replace(v, i);
            names.get(v).push(name);
            return name;
        };

        for (Variable variable : globals) {
            names.put(variable, new Stack<>());
            counter.put(variable, 0);
            newNameFunction.apply(variable);
        }

        blocks.forEach(BasicBlock::unmark);

        Consumer<BasicBlock> renameFunction = new Consumer<BasicBlock>() {
            @Override
            public void accept(BasicBlock b) {
                b.mark();

                for (Map.Entry<Variable, PhiFunction> entry : b.getPhiFunctions().entrySet()) {
                    entry.getValue().setSource(newNameFunction.apply(entry.getKey()));
                }

                for (Operation operation : b.getOperations()) {
                    if (operation instanceof LoadOperation &&
                            ((LoadOperation) operation).getSource() instanceof VariableValue) {
                        LoadOperation loadOperation = (LoadOperation) operation;
                        VariableValue variableValue = (VariableValue) ((LoadOperation) operation).getSource();

                        loadOperation.setSsaVariant(new LoadOperation(
                                new VariableValue(new Variable(
                                        topName.apply(variableValue.getVariable()),
                                        variableValue.getType(),
                                        variableValue.getVariable().getScope(),
                                        variableValue.getVariable().getDefiningBlock(),
                                        false)),
                                loadOperation.getTarget()
                        ));
                    } else if (operation instanceof StoreOperation &&
                            ((StoreOperation) operation).getTarget() instanceof VariableValue) {
                        StoreOperation storeOperation = (StoreOperation) operation;

                        VariableValue variableValue = (VariableValue) ((StoreOperation) operation).getTarget();

                        storeOperation.setSsaVariant(new StoreOperation(
                                storeOperation.getSource(),
                                new VariableValue(new Variable(
                                        newNameFunction.apply(variableValue.getVariable()),
                                        variableValue.getType(),
                                        variableValue.getVariable().getScope(),
                                        variableValue.getVariable().getDefiningBlock(),
                                        false))
                        ));
                    } else if (operation instanceof AllocationOperation) {
                        AllocationOperation allocationOperation = (AllocationOperation) operation;

                        allocationOperation.setSsaForm(new AllocationOperation(
                                new Variable(
                                        topName.apply(allocationOperation.getVariable()),
                                        allocationOperation.getVariable().getType(),
                                        allocationOperation.getVariable().getScope(),
                                        allocationOperation.getVariable().getDefiningBlock(),
                                        false)
                        ));
                    }
                }

                for (BasicBlock block : b.getOutput()) {
                    for (Map.Entry<Variable, PhiFunction> entry : block.getPhiFunctions().entrySet()) {
                        entry.getValue().addName(topName.apply(entry.getKey()));
                    }
                }

                for (BasicBlock block : b.getDominants()) {
                    if (block.isMarked() || block.isDead() || block.getDominator() != b) {
                        continue;
                    }
                    this.accept(block);
                }

                for (Map.Entry<Variable, PhiFunction> entry : b.getPhiFunctions().entrySet()) {
                    names.get(entry.getKey()).pop();
                }

                for (Operation operation : b.getOperations()) {
                    if (operation instanceof StoreOperation &&
                            ((StoreOperation) operation).getTarget() instanceof VariableValue) {
                        StoreOperation storeOperation = (StoreOperation) operation;

                        VariableValue variableValue = (VariableValue) ((StoreOperation) operation).getTarget();
                        if (globals.contains(variableValue.getVariable())) {
                            removeName.apply(variableValue.getVariable());
                        }
                    }
                }
            }
        };

        renameFunction.accept(root);
    }

    private static void insertPhiFunction(Variable variable, BasicBlock basicBlock) {
        if (!basicBlock.getPhiFunctions().containsKey(variable)) {
            basicBlock.getPhiFunctions().put(variable, new PhiFunction());
        }
    }

    private static void setDominanceFrontier(List<BasicBlock> blocks, BasicBlock root) {
        for (BasicBlock n : blocks) {
            if (n.getInput().size() >= 2) {
                for (BasicBlock p : n.getInput()) {
                    BasicBlock r = p;

                    while (r != null && r != n.getDominator()) {
                        r.addDominanceFrontier(n);
                        r = r.getDominator();
                    }
                }
            }
        }
    }

    private static void setImmediateDominators(List<BasicBlock> blocks, BasicBlock root) {
        for (BasicBlock i : blocks) {
            for (BasicBlock n : i.getDominants()) {
                boolean flag = false;
                for (BasicBlock m : i.getDominants()) {
                    if (m != n && m != i
                            && i.getDominants().contains(m) && m.getDominants().contains(n)) {
                        flag = true;
                    }
                }

                if (!flag) {
                    n.setDominator(i);
                }
            }
        }
    }

    public static void blockWalk(BasicBlock block, Consumer<BasicBlock> consumer) {
        block.setMarked(true);

        if (consumer != null) {
            consumer.accept(block);
        }

        for (BasicBlock b : block.getOutput()) {
            if (!b.isMarked()) {
                blockWalk(b, consumer);
            }
        }
    }

    public static void setDominators(List<BasicBlock> list, BasicBlock root) {
        for (BasicBlock first : list) {
            List<BasicBlock> dominants = calculateDominants(list, root, first);
            first.addDominants(dominants);
        }
    }

    private static List<BasicBlock> calculateDominants(List<BasicBlock> list, BasicBlock root, BasicBlock node) {
        if (root == node) {
            return list.stream().filter(r -> r != root).collect(Collectors.toList());
        }

        list.forEach(BasicBlock::unmark);

        node.mark();

        blockWalk(root, null);

        return list.stream()
                .filter(Predicate.not(BasicBlock::isDead))
                .filter(Predicate.not(BasicBlock::isMarked))
                .collect(Collectors.toList());
    }

    public static void paintDeadCode(List<BasicBlock> list, BasicBlock root) {
        list.forEach(BasicBlock::unmark);

        blockWalk(root, null);

        list.stream().filter(b -> !b.isMarked()).forEach(b -> b.setDead(true));
    }

    private static void removeDeadCode(List<BasicBlock> blocks, BasicBlock root) {
        blocks.removeIf(BasicBlock::isDead);
    }

    public static String graphVizDebug(FunctionBlock functionBlock) {
        StringBuilder s = new StringBuilder("digraph G {\n");

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            String body = basicBlock.getName() + ":\n";// + blockToString(basicBlock, Driver::operationToString);

            String dominanceFrontier = basicBlock.getDominanceFrontier().stream()
                    .map(BasicBlock::getName)
                    .collect(Collectors.joining("\n"));

            String ssaForm = basicBlock.getPhiFunctions().entrySet()
                    .stream()
                    .sorted((a, b) -> a.getKey().getName().compareTo(b.getKey().getName()))
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

            s
                    //.append("\n\nSSA:\n")
                    .append(ssaForm)
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

        if (allDoms) {
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

        s.append("}");
        return s.toString();
    }

    private static BasicBlock buildCfgGraph(FunctionBlock functionBlock) {
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

    private static void removeEmptyBlocks(FunctionBlock functionBlock) {
        List<BasicBlock> emptyBlocks =
                functionBlock.getBlocks().stream()
                        .filter(b -> b.getOperations().isEmpty())
                        .filter(b -> {
                            return !(b.getTerminator() instanceof Branch &&
                                    ((Branch) b.getTerminator()).getTarget().equals(b));
                        })
                        .collect(Collectors.toList());

        functionBlock.getBlocks().forEach(
                b -> {

                    Terminator terminator = b.getTerminator();

                    if (terminator instanceof Branch) {
                        Branch branch = (Branch) terminator;

                        if (emptyBlocks.contains(branch.getTarget()) && !b.equals(branch.getTarget())) {
                            b.setTerminator(branch.getTarget().getTerminator());
                        }
                    } else if (terminator instanceof ConditionalBranch) {
                        ConditionalBranch conditionalBranch = (ConditionalBranch) terminator;

                        if (emptyBlocks.contains(conditionalBranch.getFirst())) {
                            if (conditionalBranch.getFirst().getTerminator() instanceof Branch) {
                                conditionalBranch.setFirst(((Branch)
                                        conditionalBranch.getFirst().getTerminator()).getTarget());
                            }
                        }

                        if (emptyBlocks.contains(conditionalBranch.getSecond())) {
                            if (conditionalBranch.getSecond().getTerminator() instanceof Branch) {
                                conditionalBranch.setSecond(((Branch)
                                        conditionalBranch.getSecond().getTerminator()).getTarget());
                            }
                        }
                    }
                }
        );

        functionBlock.getBlocks().removeAll(emptyBlocks);
    }

    public static String moduleToString(Module module) {
        String s = "; ModuleID = 'main'\n" +
                "source_filename = \"main\"\n   ";
        return s += module.getFunctionBlocks()
                .stream()
                .map(Driver::functionToString)
                .collect(Collectors.joining("\n"));
    }

    private static String functionToString(FunctionBlock functionBlock) {
        String s = "define " + functionBlock.getReturnType().toCode() + " @" +
                functionBlock.getFunctionName() + "() {\n";
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

            if (previous != null) {
                last.setTerminator(new Branch(previous));
                last = previous;
            }

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
                    last.setTerminator(new Branch(condition));
                    endBody.setTerminator(new Branch(condition));
                    condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                } else {
                    last.setTerminator(new Branch(condition));
                    endBody.setTerminator(new Branch(step));
                    condition.setTerminator(new ConditionalBranch(operation.getResult(), body, merge));
                    step.setTerminator(new Branch(condition));
                }
            }
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
            BasicBlock retBlock = functionBlock.appendBlock("local_return");
            last.setTerminator(new Branch(retBlock));

            if (returnStatementNode.getExpressionNode() != null) {
                driveExpression(functionBlock, scope, new AssigmentExpressionNode(
                        new IdentifierNode(RET_VAL),
                        returnStatementNode.getExpressionNode()));
            }

            retBlock.setTerminator(new Branch(functionBlock.getReturnBlock()));

            functionBlock.appendDummyBlock("dummy_block");
        } else {
            throw new IllegalStateException("Unknown statement " + statementNode);
        }
    }

    private static Operation driveExpression(FunctionBlock functionBlock, Scope scope, ExpressionNode expressionNode) {
        if (expressionNode instanceof ConditionalExpressionNode) {
            return handleConditional(functionBlock, scope, (ConditionalExpressionNode) expressionNode);
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

    private static LoadOperation handleConditional(FunctionBlock functionBlock, Scope scope,
                                                   ConditionalExpressionNode expressionNode) {
        throw new IllegalStateException("Not implemented");
//        ConditionalExpressionNode conditionalExpressionNode = expressionNode;
//        BasicBlock last = functionBlock.getCurrentBlock();
//        BasicBlock condition = functionBlock.appendBlock("conditional");
//        BasicBlock thenBlock = null;
//        BasicBlock elseBlock = null;
//        BasicBlock mergeBlock = null;
//
//        Variable variable = new Variable("cond$"+ condCount++, Type.INT, scope, condition, false);
//        VariableValue value = new VariableValue(variable);
//        AllocationOperation allocationOperation = new AllocationOperation(variable);
//        condition.addOperation(allocationOperation);
//        last.setTerminator(new Branch(condition));
//
//        Operation operation = driveExpression(functionBlock, scope, conditionalExpressionNode.getConditionNode());
//
//        BasicBlock endCondition = functionBlock.getCurrentBlock();
//        thenBlock = functionBlock.appendBlock("conditional_then");
//        Value firstArg = null;
//        Value secondArg = null;
//
//        ExpressionNode thenExpression = conditionalExpressionNode.getThenNode();
//        if (isTerm(thenExpression)) {
//            firstArg = driveValue(functionBlock, scope, thenExpression);
//        } else {
//            Value source = null;
//            if (isVariable(thenExpression)) {
//                source = driveValue(functionBlock, scope, thenExpression);
//            } else {
//                source = driveExpression(functionBlock, scope, thenExpression).getResult();
//            }
//
//            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
//                firstArg = source;
//            } else {
//                LoadOperation first = new LoadOperation(source,
//                        new VariableValue(new Variable(genNext(), source.getType(),
//                                scope, functionBlock.getCurrentBlock(), true))
//                );
//                functionBlock.getCurrentBlock().addOperation(first);
//                firstArg = first.getTarget();
//            }
//        }
//        StoreOperation firstStore = new StoreOperation(
//                firstArg, value
//        );
//        thenBlock.addOperation(firstStore);
//        BasicBlock endThen = functionBlock.getCurrentBlock();
//
//        elseBlock = functionBlock.appendBlock("conditional_else");
//        ExpressionNode elseExpression = conditionalExpressionNode.getElseNode();
//        if (isTerm(elseExpression)) {
//            secondArg = driveValue(functionBlock, scope, elseExpression);
//        } else {
//            Value source = null;
//            if (isVariable(elseExpression)) {
//                source = driveValue(functionBlock, scope, elseExpression);
//            } else {
//                source = driveExpression(functionBlock, scope, elseExpression).getResult();
//            }
//
//            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
//                secondArg = source;
//            } else {
//                LoadOperation second = new LoadOperation(source,
//                        new VariableValue(new Variable(genNext(), source.getType(),
//                                scope, functionBlock.getCurrentBlock(), true))
//                );
//                functionBlock.getCurrentBlock().addOperation(second);
//                secondArg = second.getTarget();
//            }
//        }
//
//        StoreOperation secondStore = new StoreOperation(
//                secondArg, value
//        );
//        elseBlock.addOperation(secondStore);
//        BasicBlock endElse = functionBlock.getCurrentBlock();
//
//        mergeBlock = functionBlock.appendBlock("conditional_result");
//
//        endElse.setTerminator(new Branch(mergeBlock));
//        endThen.setTerminator(new Branch(mergeBlock));
//        endCondition.setTerminator(new ConditionalBranch(operation.getResult(), thenBlock, elseBlock));
//
//        LoadOperation loadOperation = new LoadOperation(value,
//                new VariableValue(new Variable(genNext(), variable.getType(),
//                        scope, functionBlock.getCurrentBlock(), true))
//        );
//        mergeBlock.addOperation(loadOperation);
//        return loadOperation;
    }

    private static Operation handleOr(FunctionBlock functionBlock, Scope scope, LogicalOrExpressionNode expressionNode) {
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
        }
        Operation operation = new BinOperation(BinOpType.OR, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleAnd(FunctionBlock functionBlock, Scope scope, LogicalAndExpressionNode expressionNode) {
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                secondArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
        }
        Operation operation = new BinOperation(BinOpType.AND, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        Type.BOOL,
                        scope,
                        functionBlock.getCurrentBlock(), true)));
        functionBlock.getCurrentBlock().addOperation(operation);
        return operation;
    }

    private static Operation handleEquality(FunctionBlock functionBlock, Scope scope, EqualityExpressionNode expressionNode) {
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                secondArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
        }
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

    private static Operation handleRelational(FunctionBlock functionBlock, Scope scope, RelationalExpressionNode expressionNode) {
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                secondArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
        }

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

    private static StoreOperation handleAssigment(FunctionBlock functionBlock, Scope scope, AssigmentExpressionNode assigmentExpressionNode) {
        Value source = null;
        Value arg = null;

        ExpressionNode expressionNode = ((AssigmentExpressionNode) assigmentExpressionNode).getExpressionNode();

        if (isTerm(expressionNode)) {
            arg = driveValue(functionBlock, scope, expressionNode);
        } else {
            if (isVariable(expressionNode)) {
                source = driveValue(functionBlock, scope, expressionNode);
            } else {
                source = driveExpression(functionBlock, scope, expressionNode).getResult();
            }

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                arg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                arg = second.getTarget();
            }
        }

        StoreOperation storeOperation = new StoreOperation(
                arg,
                variableValueByName(functionBlock, scope,
                        (assigmentExpressionNode.getIdentifierNode().getName())));
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                secondArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                firstArg = source;
            } else {
                LoadOperation first = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(first);
                firstArg = first.getTarget();
            }
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

            if (source instanceof VariableValue && ((VariableValue) source).getVariable().isLocal()) {
                secondArg = source;
            } else {
                LoadOperation second = new LoadOperation(source,
                        new VariableValue(new Variable(genNext(), source.getType(),
                                scope, functionBlock.getCurrentBlock(), true))
                );
                functionBlock.getCurrentBlock().addOperation(second);
                secondArg = second.getTarget();
            }
        }
        BinOpType binOpType;
        switch (expressionNode.getType()) {
            case MUL:
                binOpType = BinOpType.MUL;
                break;
            case DIV:
            default:
                binOpType = BinOpType.DIV;
        }
        Operation operation = new BinOperation(binOpType, firstArg, secondArg,
                new VariableValue(new Variable(genNext(),
                        binOperationType(firstArg, secondArg),
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
