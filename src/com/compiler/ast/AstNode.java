package com.compiler.ast;

public abstract class AstNode {
    public static final String SHIFT = "....";

    public String astDebug() {
        return astDebug(0);
    }

    public abstract String astDebug(int shift);
}
