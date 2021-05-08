package com.compiler.ir;

public class ConditionalBranch extends Terminator {
    private final Operation operation;
    private final BasicBlock first;
    private final BasicBlock second;

    public ConditionalBranch(Operation operation, BasicBlock first, BasicBlock second) {
        this.operation = operation;
        this.first = first;
        this.second = second;
    }

    public Operation getOperation() {
        return operation;
    }

    public BasicBlock getFirst() {
        return first;
    }

    public BasicBlock getSecond() {
        return second;
    }
}

