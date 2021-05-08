package com.compiler.ir;

public class AllocationOperation extends Operation {
    private final Variable variable;

    public AllocationOperation(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public Value getResult() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public String toString() {
        return "%" + variable.getName() + " = alloca " + variable.getType().toCode();
    }
}
