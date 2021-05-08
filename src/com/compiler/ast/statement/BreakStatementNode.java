package com.compiler.ast.statement;

public class BreakStatementNode extends StatementNode {
    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Break";
    }
}
