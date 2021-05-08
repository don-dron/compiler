package com.compiler.ir;

import com.compiler.ast.ParameterNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionBlock implements Scope {
    private final String functionName;
    private final List<BasicBlock> blocks;
    private final Set<Variable> variables;
    private Type returnType;

    public FunctionBlock(String name, Type returnType) {
        this.functionName = name;
        this.returnType = returnType;
        this.variables = new HashSet<>();
        this.blocks = new ArrayList<>();
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
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

    public void appendBlock(BasicBlock basicBlock) {
        blocks.add(basicBlock);
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Type getReturnType() {
        return returnType;
    }
}
