package com.compiler.ir.drive.operation;

import com.compiler.ir.drive.value.Value;

public class StoreOperation extends Operation {
    private final Value source;
    private final Value target;
    private StoreOperation ssaVariant;

    public StoreOperation(Value source, Value target) {
        this.source = source;
        this.target = target;
    }

    public Value getSource() {
        return source;
    }

    public void setSsaVariant(StoreOperation ssaVariant) {
        this.ssaVariant = ssaVariant;
    }

    @Override
    public boolean hasSsaForm() {
        return ssaVariant != null;
    }

    @Override
    public Operation getSsa() {
        return ssaVariant;
    }

    public Value getTarget() {
        return target;
    }

    @Override
    public Value getResult() {
        return target;
    }

    public String toString() {
        return "store " + source.getType().toCode() + " " + source.toCode() +
                ", " + target.getType().toCode() + " " + target.toCode();
    }
}
