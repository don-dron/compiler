package com.compiler.ir;

public class VariableValue extends Value {
    private final Variable variable;

    public VariableValue(Variable variable) {
        this.variable =variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return variable.getName();
    }
}
