package com.compiler.ir;

public abstract class Operation {
    public abstract Value getResult();
    public abstract boolean hasSsaForm();
    public abstract Operation getSsa();
}
