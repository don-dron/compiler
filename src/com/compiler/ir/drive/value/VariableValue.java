package com.compiler.ir.drive.value;

import com.compiler.ir.drive.Type;

public class VariableValue extends Value {
    private final Variable variable;

    public VariableValue(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return variable.getName();
    }

    @Override
    public Type getType() {
        return variable.getType();
    }

    @Override
    public String toCode() {
        return (variable.isLocal() ? "" : "*") + "%" + variable.getName();
    }
}
