package com.compiler.ast.statement;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundStatementNode extends StatementNode {
    private final List<StatementNode> statements;

    public CompoundStatementNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Compound:\n" +
                statements.stream().map(s -> s.astDebug(shift + 1)).collect(Collectors.joining("\n"));
    }
}
