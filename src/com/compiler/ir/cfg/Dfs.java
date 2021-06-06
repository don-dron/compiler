package com.compiler.ir.cfg;

import com.compiler.ir.BasicBlock;

import java.util.function.Consumer;

public class Dfs {
    public static void dfs(BasicBlock block, Consumer<BasicBlock> consumer) {
        block.setMarked(true);

        if (consumer != null) {
            consumer.accept(block);
        }

        for (BasicBlock b : block.getOutput()) {
            if (!b.isMarked()) {
                dfs(b, consumer);
            }
        }
    }
}
