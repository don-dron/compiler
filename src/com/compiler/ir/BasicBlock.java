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
    private boolean dead;

    private boolean marked;
    private BasicBlock dominator;
    private final List<BasicBlock> dominants;
    private final List<BasicBlock> input;
    private final List<BasicBlock> output;

    public BasicBlock(Scope scope, String name) {
        this.name = name;
        this.scope = scope;
        this.operations = new ArrayList<>();
        this.defines = new HashSet<>();

        this.dominator = null;
        this.dominants = new ArrayList<>();
        this.input = new ArrayList<>();
        this.output = new ArrayList<>();
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isDead() {
        return dead;
    }

    public void addInput(BasicBlock basicBlock) {
        input.add(basicBlock);
    }

    public void addOutput(BasicBlock basicBlock) {
        output.add(basicBlock);
    }

    public void setDominator(BasicBlock dominator) {
        this.dominator = dominator;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isMarked() {
        return marked;
    }

    public List<BasicBlock> getDominants() {
        return dominants;
    }

    public BasicBlock getDominator() {
        return dominator;
    }

    public List<BasicBlock> getInput() {
        return input;
    }

    public List<BasicBlock> getOutput() {
        return output;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void unmark() {
        marked = false;
    }

    public void mark() {
        marked = true;
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
