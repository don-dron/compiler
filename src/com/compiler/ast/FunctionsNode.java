package com.compiler.ast;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionsNode extends AstNode {
    private final List<FunctionNode> functionNodes;

    public FunctionsNode(List<FunctionNode> functionNodes) {
        this.functionNodes = functionNodes;
    }

    public List<FunctionNode> getFunctionNodes() {
        return functionNodes;
    }

    @Override
    public String astDebug(int shift) {
        StringBuilder s = new StringBuilder("Functions:\n");

        for (FunctionNode functionNode : functionNodes) {
            s.append(functionNode.astDebug(shift));
        }

        return s.toString();
    }

    @Override
    public List<AstNode> getChildren() {
        return functionNodes.stream().map(t -> (AstNode) t).collect(Collectors.toList());
    }
}
