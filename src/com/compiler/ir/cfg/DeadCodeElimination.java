package com.compiler.ir.cfg;

import com.compiler.ir.drive.terminator.Terminator;
import com.compiler.ir.*;
import com.compiler.ir.drive.terminator.Branch;
import com.compiler.ir.drive.terminator.ConditionalBranch;

import java.util.List;
import java.util.stream.Collectors;

import static com.compiler.ir.cfg.Dfs.dfs;

public class DeadCodeElimination {

    public static void paintDeadCode(List<BasicBlock> list, BasicBlock root) {
        list.forEach(BasicBlock::unmark);
        dfs(root, null);
        list.stream().filter(b -> !b.isMarked()).forEach(b -> b.setDead(true));
    }

    public static void removeDeadCode(List<BasicBlock> blocks, BasicBlock root) {
        blocks.removeIf(BasicBlock::isDead);
    }

    public static void removeOneDirectBranches(FunctionBlock functionBlock) {
        for(BasicBlock block : functionBlock.getBlocks()) {
            Terminator terminator = block.getTerminator();

            if(terminator instanceof ConditionalBranch) {
                ConditionalBranch conditionalBranch = (ConditionalBranch) terminator;

                if(conditionalBranch.getFirst().equals(conditionalBranch.getSecond())) {
                    block.setTerminator(new Branch(conditionalBranch.getFirst()));
                }
            }
        }
    }

    public static void removeEmptyBlocks(FunctionBlock functionBlock) {
        List<BasicBlock> emptyBlocks =
                functionBlock.getBlocks().stream()
                        .filter(b -> b.getOperations().isEmpty() && b.getTerminator() instanceof Branch)
                        .collect(Collectors.toList());

        for (BasicBlock e : emptyBlocks) {
            for (BasicBlock b : functionBlock.getBlocks()) {
                Terminator terminator = b.getTerminator();
                if (terminator instanceof Branch) {
                    Branch branch = (Branch) terminator;
                    if (branch.getTarget().equals(e)) {
                        Branch emptyBranch = ((Branch) branch.getTarget().getTerminator());
                        b.setTerminator(emptyBranch);
                    }
                } else if (terminator instanceof ConditionalBranch) {
                    ConditionalBranch conditionalBranch = (ConditionalBranch) terminator;

                    if (conditionalBranch.getFirst().equals(e)) {
                        Branch emptyBranch = ((Branch) conditionalBranch.getFirst().getTerminator());
                        if (conditionalBranch.getFirst().getTerminator() instanceof Branch) {
                            conditionalBranch.setFirst(emptyBranch.getTarget());
                        }
                    }

                    if (conditionalBranch.getSecond().equals(e)) {
                        Branch emptyBranch = ((Branch) conditionalBranch.getSecond().getTerminator());
                        if (conditionalBranch.getSecond().getTerminator() instanceof Branch) {
                            conditionalBranch.setSecond(emptyBranch.getTarget());
                        }
                    }
                }
            }
        }
    }
}
