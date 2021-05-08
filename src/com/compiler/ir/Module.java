package com.compiler.ir;

import java.util.List;

public class Module {
    private final List<FunctionBlock> functionBlocks;

    public Module(List<FunctionBlock> functionBlocks) {
        this.functionBlocks = functionBlocks;
    }

    public List<FunctionBlock> getFunctionBlocks() {
        return functionBlocks;
    }
}
