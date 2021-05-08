package com.compiler.ir;

public enum BinOpType {
    ADD("add"),
    SUB("sub"),
    DIV("div"),
    GE("ge"),
    LE("le"),
    GT("gt"),
    LT("lt"),
    NE("ne"),
    EQ("eq"),
    AND(""),
    OR(""),
    MUL("mul");

    BinOpType(String code) {
        this.code = code;
    }

    public String toCode() {
        return code;
    }

    private String code;
}
