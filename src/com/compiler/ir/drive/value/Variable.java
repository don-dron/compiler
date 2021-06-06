package com.compiler.ir.drive.value;

import com.compiler.ir.BasicBlock;
import com.compiler.ir.Scope;
import com.compiler.ir.drive.Type;

public class Variable {
    private final BasicBlock definingBlock;
    private final String name;
    private final Type type;
    private final Scope scope;
    private final boolean local;

    public Variable(String name, Type type, Scope scope, BasicBlock definingBlock, boolean local) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.definingBlock = definingBlock;
        this.local = local;
    }

    public boolean isLocal() {
        return local;
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
