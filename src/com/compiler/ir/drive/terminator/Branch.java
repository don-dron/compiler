package com.compiler.ir.drive.terminator;

import com.compiler.ir.BasicBlock;

public class Branch extends Terminator {
    private BasicBlock target;

    public Branch(BasicBlock target) {
        this.target = target;
    }

    public void setTarget(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "br label %" + target.getName();
    }
}
