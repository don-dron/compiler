package com.compiler.ast.statement;

import com.compiler.ast.statement.StatementNode;

public class EmptyStatementNode extends StatementNode {

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Empty";
    }
}
