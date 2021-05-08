package com.compiler.ast;

import com.compiler.ir.Type;

import java.util.List;

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

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
