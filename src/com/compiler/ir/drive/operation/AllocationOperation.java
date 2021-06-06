package com.compiler.ir.drive.operation;

import com.compiler.ir.drive.value.Value;
import com.compiler.ir.drive.value.Variable;

public class AllocationOperation extends Operation {
    private final Variable variable;
    private AllocationOperation ssaForm;

    public AllocationOperation(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public Operation getSsa() {
        return ssaForm;
    }

    public void setSsaForm(AllocationOperation ssaForm) {
        this.ssaForm = ssaForm;
    }

    @Override
    public boolean hasSsaForm() {
        return ssaForm != null;
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
