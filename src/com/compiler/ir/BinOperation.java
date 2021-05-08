package com.compiler.ir;

public class BinOperation extends Operation {
    private final BinOpType type;
    private final Value first;
    private final Value second;
    private final Value target;

    public BinOperation(BinOpType type, Value first, Value second, Value target) {
        this.type = type;
        this.first = first;
        this.second = second;
        this.target = target;
    }

    public Value getFirst() {
        return first;
    }

    public Value getSecond() {
        return second;
    }

    @Override
    public Value getResult() {
        return target;
    }

    public Value getTarget() {
        return target;
    }

    public String toString() {
        return target.toCode() + " = " + type.toCode() + " " + getResult().getType().toCode() +
                " " + first.toCode() + ", " + second.toCode();
    }
}
