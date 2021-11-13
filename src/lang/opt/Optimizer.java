package lang.opt;

import lang.ir.*;
import lang.ir.Module;

import java.util.List;
import java.util.stream.Collectors;

public class Optimizer {
    private final Module module;

    public Optimizer(Module module){
        this.module = module;
    }

    public void optimize() {
        module.getFunctions()
                .forEach(this::optimizeFunction);
    }

    private void optimizeFunction(Function function) {
        removeEmptyBlocks(function);
    }

    private void removeEmptyBlocks(Function function) {
        List<BasicBlock> emptyBlocks =
                function.getBlocks().stream()
                        .filter(b -> b.getCommands().isEmpty()
                                && b.getTerminator() instanceof Branch)
                        .collect(Collectors.toList());

        function.getBlocks().removeIf(emptyBlocks::contains);

        for (BasicBlock e : emptyBlocks) {
            for (BasicBlock b : function.getBlocks()) {
                Terminator terminator = b.getTerminator();
                if (terminator instanceof Branch) {
                    Branch branch = (Branch) terminator;
                    if (branch.getTarget().equals(e)) {
                        b.getOutput().remove(e);
                        Branch emptyBranch = ((Branch) branch.getTarget().getTerminator());
                        b.setTerminator(emptyBranch);
                        b.getOutput().add(emptyBranch.getTarget());
                    }
                } else if (terminator instanceof ConditionalBranch) {
                    ConditionalBranch conditionalBranch = (ConditionalBranch) terminator;

                    if (conditionalBranch.getLeft().equals(e)) {
                        b.getOutput().remove(e);
                        Branch emptyBranch = ((Branch) conditionalBranch.getLeft().getTerminator());
                        if (conditionalBranch.getLeft().getTerminator() instanceof Branch) {
                            conditionalBranch.setLeft(emptyBranch.getTarget());
                            b.addOutput(emptyBranch.getTarget());
                            emptyBranch.getTarget().addInput(b);
                        }
                    }

                    if (conditionalBranch.getRight().equals(e)) {
                        b.getOutput().remove(e);
                        Branch emptyBranch = ((Branch) conditionalBranch.getRight().getTerminator());
                        if (conditionalBranch.getRight().getTerminator() instanceof Branch) {
                            conditionalBranch.setRight(emptyBranch.getTarget());
                            b.addOutput(emptyBranch.getTarget());
                            emptyBranch.getTarget().addInput(b);
                        }
                    }
                }
            }
        }
    }
}
