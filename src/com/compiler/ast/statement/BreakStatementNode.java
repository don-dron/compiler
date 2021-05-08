package com.compiler.ast.statement;

import com.compiler.ast.AstNode;

import java.util.List;
import java.util.stream.Collectors;

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
