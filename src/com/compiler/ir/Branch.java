package com.compiler.ir;

public class Branch extends Terminator{
    private final BasicBlock target;

    public Branch(BasicBlock target) {
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
