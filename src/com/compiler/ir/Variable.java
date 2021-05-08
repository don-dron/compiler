package com.compiler.ir;

public class Variable {
    private String name;
    private Type type;
    private Scope scope;

    public Variable(String name, Type type, Scope scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
