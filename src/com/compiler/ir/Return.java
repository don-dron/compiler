package com.compiler.ir;

public class Return extends Terminator {
    private final Value value;

    public Return(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if(value instanceof VariableValue) {
            return "ret " + value.getType().toCode() + " %" + ((VariableValue)value).getVariable().getName();
        }
        return "ret " + value.getType().toCode() + " " + value.toCode();
    }
}
