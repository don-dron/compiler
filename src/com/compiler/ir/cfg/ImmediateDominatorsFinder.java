package com.compiler.ir.cfg;

import com.compiler.ir.BasicBlock;

import java.util.List;

public class ImmediateDominatorsFinder {

    public static void setImmediateDominators(List<BasicBlock> blocks, BasicBlock root) {
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

}
