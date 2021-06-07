package com.compiler.ir.drive.value;

import com.compiler.ir.drive.Type;

public class BoolValue extends Value {
    private final boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.BOOL;
    }

    @Override
    public String toCode() {
        return String.valueOf(value);
    }
}
