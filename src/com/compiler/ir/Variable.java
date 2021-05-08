package com.compiler.ir;

import java.util.Objects;

public class Variable {
    private final BasicBlock definingBlock;
    private final String name;
    private final Type type;
    private final Scope scope;

    public Variable(String name, Type type, Scope scope, BasicBlock definingBlock) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.definingBlock = definingBlock;
    }

    public Scope getScope() {
        return scope;
    }

    public BasicBlock getDefiningBlock() {
        return definingBlock;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " : " + type;
    }
}
