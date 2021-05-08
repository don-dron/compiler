package com.compiler.ir;

public class StoreOperation extends Operation {
    private final Value source;
    private final Value target;

    public StoreOperation(Value source, Value target) {
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
        return "store " + source.toString() + "  " + target.toString() ;
    }
}
