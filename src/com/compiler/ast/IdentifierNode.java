package com.compiler.ast;

import java.util.List;

public class IdentifierNode  extends AstNode {
    private final String name;
    
    public IdentifierNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Identifier: " + name;
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
