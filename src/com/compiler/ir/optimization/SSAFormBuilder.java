package com.compiler.ir.optimization;

import com.compiler.ir.BasicBlock;
import com.compiler.ir.drive.operation.AllocationOperation;
import com.compiler.ir.drive.operation.LoadOperation;
import com.compiler.ir.drive.operation.Operation;
import com.compiler.ir.drive.operation.StoreOperation;
import com.compiler.ir.drive.value.Variable;
import com.compiler.ir.drive.value.VariableValue;

import java.util.*;

public class SSAFormBuilder {
    private final Map<Variable, Stack<String>> names = new HashMap<>();
    private final Map<Variable, Integer> counter = new HashMap<>();
    private final Set<Variable> globals = new HashSet<>();
    private final Map<Variable, List<BasicBlock>> vars = new HashMap<>();
    private final Map<Variable, BasicBlock> allocations = new HashMap<>();

    public void buildSsaForm(List<BasicBlock> blocks, BasicBlock root) {
        prepareNames(blocks);

        blocks.forEach(BasicBlock::unmark);

        renamingInBlock(root);
    }

    private void prepareNames(List<BasicBlock> blocks) {
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

                    vars.computeIfAbsent(variableValue.getVariable(), k -> new ArrayList<>()).add(block);
                } else if (operation instanceof AllocationOperation) {
                    Variable variable = (Variable) ((AllocationOperation) operation).getVariable();
                    allocations.put(variable, block);
                }
            }
        }

        for (Variable variable : globals) {
            List<BasicBlock> workList = new ArrayList<>(vars.get(variable));
            List<BasicBlock> garbage = new ArrayList<>();

            while (!workList.isEmpty()) {
                BasicBlock current = workList.remove(0);
                garbage.add(current);

                for (BasicBlock dfNode : current.getDominanceFrontier()) {
                    BasicBlock bottom = dfNode;
                    boolean found = false;
                    while (bottom != null) {
                        if (allocations.get(variable).equals(bottom)) {
                            found = true;
                            break;
                        }
                        bottom = bottom.getDominator();
                    }

                    // Проверка на аллокации - добавлять phi-функцию только если аллокация
                    // есть выше по дереву доминаторов
                    if (found) {
                        insertPhiFunction(variable, dfNode);
                    }

                    if (!garbage.contains(dfNode) && !workList.contains(dfNode)) {


                        workList.add(dfNode);
                    }
                }
            }
        }

        for (Variable variable : globals) {
            names.put(variable, new Stack<>());
            counter.put(variable, 0);
            newNameFunction(variable);
        }
    }

    private String removeName(Variable v) {
        if (!names.containsKey(v)) {
            return v.getName();
        } else {
            return names.get(v).pop();
        }
    }

    private String topName(Variable v) {
        if (!names.containsKey(v)) {
            return v.getName();
        } else {
            return names.get(v).peek();
        }
    }

    private String newNameFunction(Variable v) {
        if (!counter.containsKey(v)) {
            return v.getName();
        }
        int i = counter.get(v);

        String name = v.getName() + "_v." + i;
        i++;
        counter.replace(v, i);
        names.get(v).push(name);
        return name;
    }

    private void insertPhiFunction(Variable variable, BasicBlock basicBlock) {
        if (!basicBlock.getPhiFunctions().containsKey(variable)) {
            basicBlock.getPhiFunctions().put(variable, new PhiFunction());
        }
    }

    private void renamingInBlock(BasicBlock b) {
        b.mark();

        for (Map.Entry<Variable, PhiFunction> entry : b.getPhiFunctions().entrySet()) {
            entry.getValue().setSource(newNameFunction(entry.getKey()));
        }

        for (Operation operation : b.getOperations()) {
            if (operation instanceof LoadOperation &&
                    ((LoadOperation) operation).getSource() instanceof VariableValue) {
                LoadOperation loadOperation = (LoadOperation) operation;
                VariableValue variableValue = (VariableValue) ((LoadOperation) operation).getSource();

                loadOperation.setSsaVariant(new LoadOperation(
                        new VariableValue(new Variable(
                                topName(variableValue.getVariable()),
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
                                newNameFunction(variableValue.getVariable()),
                                variableValue.getType(),
                                variableValue.getVariable().getScope(),
                                variableValue.getVariable().getDefiningBlock(),
                                false))
                ));
            } else if (operation instanceof AllocationOperation) {
                AllocationOperation allocationOperation = (AllocationOperation) operation;

                allocationOperation.setSsaForm(new AllocationOperation(
                        new Variable(
                                topName(allocationOperation.getVariable()),
                                allocationOperation.getVariable().getType(),
                                allocationOperation.getVariable().getScope(),
                                allocationOperation.getVariable().getDefiningBlock(),
                                false)
                ));
            }
        }

        for (BasicBlock block : b.getOutput()) {
            for (Map.Entry<Variable, PhiFunction> entry : block.getPhiFunctions().entrySet()) {
                entry.getValue().addName(topName(entry.getKey()));
            }
        }

        for (BasicBlock block : b.getDominants()) {
            if (block.isMarked() || block.isDead() || block.getDominator() != b) {
                continue;
            }
            renamingInBlock(block);
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
                    removeName(variableValue.getVariable());
                }
            }
        }
    }
}
