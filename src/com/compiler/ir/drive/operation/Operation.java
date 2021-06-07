package com.compiler.ir.drive.operation;

import com.compiler.ir.drive.value.Value;

public abstract class Operation {
    public abstract Value getResult();
    public abstract boolean hasSsaForm();
    public abstract Operation getSsa();
}
