package com.compiler.ast.statement;

import com.compiler.ast.AstNode;

import java.util.List;

public class BreakStatementNode extends StatementNode {
    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Break";
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
