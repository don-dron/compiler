package com.compiler.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicBlock implements Scope {
    private final String name;
    private final FunctionBlock functionBlock;
    private final Set<Variable> variables;
    private final List<Operation> operations;
    private Terminator terminator;

    public BasicBlock(FunctionBlock functionBlock, String name) {
        this.name = name;
        this.functionBlock = functionBlock;
        this.operations = new ArrayList<>();
        this.variables = new HashSet<>();
    }

    @Override
    public Set<Variable> getVariables() {
        return variables;
    }

    public void addVariable(Variable var) {
        if (!variables.add(var)) {
            throw new IllegalStateException("Variable already declared");
        }
    }

    public void addVariables(List<Variable> var) {
        if (!variables.addAll(var)) {
            throw new IllegalStateException("Variable already declared");
        }
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

    public FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public Terminator getTerminator() {
        return terminator;
    }
}
