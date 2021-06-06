package com.compiler.ir.cfg;

import com.compiler.ir.BasicBlock;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.compiler.ir.cfg.Dfs.dfs;

public class DominatorsFinder {
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

        dfs(root, null);

        return list.stream()
                .filter(Predicate.not(BasicBlock::isDead))
                .filter(Predicate.not(BasicBlock::isMarked))
                .collect(Collectors.toList());
    }
}
