package com.compiler.ir;

import com.compiler.ir.drive.operation.Operation;
import com.compiler.ir.drive.terminator.Terminator;
import com.compiler.ir.drive.value.Variable;
import com.compiler.ir.optimization.PhiFunction;

import java.util.*;

public class BasicBlock {
    private final String name;
    private final Scope scope;
    private final Set<Variable> defines;
    private final List<Operation> operations;
    private final List<Variable> ssaDefines;
    private Terminator terminator;
    private boolean dummy;
    private boolean dead;

    private boolean marked;
    private BasicBlock dominator;
    private final List<BasicBlock> dominants;
    private final List<BasicBlock> input;
    private final List<BasicBlock> output;
    private final List<BasicBlock> dominanceFrontier;
    private final Map<Variable, PhiFunction> phiFunctions;
    private final Map<Variable, String> ssaNames;

    public BasicBlock(Scope scope, String name) {
        this.name = name;
        this.scope = scope;
        this.operations = new ArrayList<>();
        this.defines = new HashSet<>();

        this.dominator = null;
        this.dominants = new ArrayList<>();
        this.input = new ArrayList<>();
        this.output = new ArrayList<>();
        this.ssaDefines = new ArrayList<>();
        this.phiFunctions = new HashMap<>();
        this.ssaNames = new HashMap<>();
        this.dominanceFrontier = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "BasicBlock: " + name;
    }

    public Map<Variable, String> getSsaNames() {
        return ssaNames;
    }

    public List<Variable> getSsaDefines() {
        return ssaDefines;
    }

    public Map<Variable, PhiFunction> getPhiFunctions() {
        return phiFunctions;
    }

    public boolean addVariable(Variable variable) {
        return ssaDefines.add(variable);
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isDead() {
        return dead;
    }

    public List<BasicBlock> getDominanceFrontier() {
        return dominanceFrontier;
    }

    public void addDominanceFrontier(BasicBlock block) {
        dominanceFrontier.add(block);
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

    public void addDominant(BasicBlock block) {
        dominants.add(block);
    }

    public void addDominants(List<BasicBlock> dominants) {
        this.dominants.addAll(dominants);
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
