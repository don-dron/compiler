package com.compiler.ir;

public class LoadOperation extends Operation {
    private final Value source;
    private final Value target;

    public LoadOperation(Value source, Value target) {
        this.source = source;
        this.target = target;
    }

    public Value getSource() {
        return source;
    }

    public Value getTarget() {
        return target;
    }

    @Override
    public Value getResult() {
        return target;
    }

    public String toString(){
        return target.toString() + " = load " + source.toString();
    }
}
