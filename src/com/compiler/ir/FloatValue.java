package com.compiler.ir;

public class FloatValue extends Value {
    private final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public float getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.FLOAT;
    }
}
