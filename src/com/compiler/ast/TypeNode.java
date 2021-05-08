package com.compiler.ast;

import com.compiler.core.Type;

public class TypeNode  extends AstNode {
    private final Type type;

    public TypeNode(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Type: " + type;
    }
}
