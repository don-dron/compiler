package com.compiler.ir;

public class IntValue extends Value {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.INT;
    }

    @Override
    public String toCode() {
        return String.valueOf(value);
    }
}
