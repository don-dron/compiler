package com.compiler.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionBlock {
    private static int count = 0;
    private final String functionName;
    private final List<BasicBlock> blocks;
    private final Set<Variable> defines;
    private final Scope scope;
    private Type returnType;
    private BasicBlock currentBlock;
    private BasicBlock returnBlock;

    public FunctionBlock(String name, Type returnType, Scope scope) {
        this.functionName = name;
        this.scope = scope;
        this.returnType = returnType;
        this.defines = new HashSet<>();
        this.blocks = new ArrayList<>();
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void addDefine(Variable var) {
        if (!defines.add(var)) {
            throw new IllegalStateException("Variable already declared");
        }
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

    public BasicBlock appendBlock(String name) {
        BasicBlock basicBlock = new BasicBlock(scope, name + "_" + count++);
        blocks.add(basicBlock);
        currentBlock = basicBlock;
        return basicBlock;
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

    public BasicBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setReturnBlock(BasicBlock returnBlock) {
        this.returnBlock = returnBlock;
    }

    public BasicBlock getReturnBlock() {
        return returnBlock;
    }
}
