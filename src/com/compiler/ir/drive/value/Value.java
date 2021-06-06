package com.compiler.ir.drive.value;

import com.compiler.ir.drive.Type;

public abstract class Value {
    public abstract Type getType();

    public abstract String toCode();
}
