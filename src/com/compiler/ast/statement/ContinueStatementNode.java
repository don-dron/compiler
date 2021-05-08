package com.compiler.ast.statement;

public class ContinueStatementNode extends StatementNode {
    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Continue";
    }
}
