package com.compiler.ir;

public abstract class Value {
    public abstract Type getType();

    public abstract String toCode();
}