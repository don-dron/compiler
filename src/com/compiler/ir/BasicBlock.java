package com.compiler.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicBlock {
    private final String name;
    private final Scope scope;
    private final Set<Variable> defines;
    private final List<Operation> operations;
    private Terminator terminator;
    private boolean dummy;

    public BasicBlock(Scope scope, String name) {
        this.name = name;
        this.scope = scope;
        this.operations = new ArrayList<>();
        this.defines = new HashSet<>();
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void addDefine(Variable var) {
        if (!defines.add(var)) {
            throw new IllegalStateException("Variable already declared");
        }
    }

    public boolean isDummy() {
        return dummy;
    }

    public void addDefines(List<Variable> var) {
        if (!defines.addAll(var)) {
            throw new IllegalStateException("Variable already declared");
        }
    }

    public Scope getScope() {
        return scope;
    }

    public Set<Variable> getDefines() {
        return defines;
    }

    public void appendOperation(Operation operation) {
        operations.add(operation);
    }

    public void setTerminator(Terminator terminator) {
        this.terminator = terminator;
    }

    public String getName() {
        return name;
    }

    public Terminator getTerminator() {
        return terminator;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    public void setDummy() {
        this.dummy = true;
    }
}
