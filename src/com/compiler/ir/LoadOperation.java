package com.compiler.ir;

public class LoadOperation extends Operation {
    private final Value source;
    private final Value target;
    private LoadOperation ssaVariant;

    public LoadOperation(Value source, Value target) {
        this.source = source;
        this.target = target;
    }

    public Value getSource() {
        return source;
    }

    @Override
    public boolean hasSsaForm() {
        return ssaVariant != null;
    }

    @Override
    public Operation getSsa() {
        return ssaVariant;
    }

    public void setSsaVariant(LoadOperation ssaVariant) {
        this.ssaVariant = ssaVariant;
    }

    public Value getTarget() {
        return target;
    }

    @Override
    public Value getResult() {
        return target;
    }

    public String toString() {
        return target.toCode() + " = load " + target.getType().toCode() + ", "
                + source.getType().toCode() + " " + source.toCode();
    }
}
