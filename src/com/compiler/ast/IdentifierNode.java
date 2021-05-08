package com.compiler.ast;

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
}
