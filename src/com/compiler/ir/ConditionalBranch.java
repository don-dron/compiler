package com.compiler.ir;

public class ConditionalBranch extends Terminator {
    private final Value value;
    private BasicBlock first;
    private BasicBlock second;

    public ConditionalBranch(Value value, BasicBlock first, BasicBlock second) {
        this.value = value;
        this.first = first;
        this.second = second;
    }

    public void setFirst(BasicBlock first) {
        this.first = first;
    }

    public void setSecond(BasicBlock second) {
        this.second = second;
    }

    public Value getValue() {
        return value;
    }

    public BasicBlock getFirst() {
        return first;
    }

    public BasicBlock getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "br " + value.getType().toCode() + " " + value.toCode() + ", label %" + first.getName() + ", label %" + second.getName();
    }
}

