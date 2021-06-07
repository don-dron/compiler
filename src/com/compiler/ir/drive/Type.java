package com.compiler.ir.drive;

public enum Type {
    VOID("void"),
    INT("i32"),
    BOOL("i1"),
    FLOAT("f32");

    Type(String code) {
        this.code = code;
    }

    public String toCode() {
        return code;
    }

    private String code;
}
