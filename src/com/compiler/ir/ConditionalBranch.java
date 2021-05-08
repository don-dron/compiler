package com.compiler.ir;

public class ConditionalBranch extends Terminator {
    private final Variable variable;
    private final BasicBlock first;
    private final BasicBlock second;

    public ConditionalBranch(Variable variable, BasicBlock first, BasicBlock second) {
        this.variable = variable;
        this.first = first;
        this.second = second;
    }

    public Variable getVariable() {
        return variable;
    }

    public BasicBlock getFirst() {
        return first;
    }

    public BasicBlock getSecond() {
        return second;
    }
}

