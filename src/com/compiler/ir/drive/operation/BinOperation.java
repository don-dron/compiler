package com.compiler.ir.drive.operation;

import com.compiler.ir.drive.value.Value;

public class BinOperation extends Operation {
    private final BinOpType type;
    private final Value first;
    private final Value second;
    private final Value target;
    private BinOperation ssaVariant;

    public BinOperation(BinOpType type, Value first, Value second, Value target) {
        this.type = type;
        this.first = first;
        this.second = second;
        this.target = target;
    }

    @Override
    public boolean hasSsaForm() {
        return ssaVariant != null;
    }

    @Override
    public Operation getSsa() {
        return ssaVariant;
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
        return target.toCode() + " = " + type.toCode() + " " + first.getType().toCode() +
                " " + first.toCode() + ", " + second.toCode();
    }
}
