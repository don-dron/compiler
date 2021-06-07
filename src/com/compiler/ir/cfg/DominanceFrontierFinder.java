package com.compiler.ir.cfg;

import com.compiler.ir.BasicBlock;

import java.util.List;

public class DominanceFrontierFinder {

    public static void setDominanceFrontier(List<BasicBlock> blocks, BasicBlock root) {
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

}
