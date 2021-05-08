package com.compiler.ir;

public class BoolValue extends Value {
    private final boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
