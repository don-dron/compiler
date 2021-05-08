package com.compiler.ast.statement;

import com.compiler.ast.AstNode;

import java.util.List;

public class ContinueStatementNode extends StatementNode {
    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Continue";
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
