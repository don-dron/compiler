package com.compiler.ir.optimization;

import com.compiler.ir.*;
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
    private final List<Variable> globals = new ArrayList<>();
    private final Map<Variable, List<BasicBlock>> vars = new HashMap<>();

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
