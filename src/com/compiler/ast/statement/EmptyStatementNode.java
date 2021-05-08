package com.compiler.ast.statement;

import com.compiler.ast.AstNode;
import com.compiler.ast.statement.StatementNode;

import java.util.List;

public class EmptyStatementNode extends StatementNode {

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Empty";
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
