package lang.ast.statement;

import lang.ast.AstNode;

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
        return SHIFT.repeat(shift) + "CompoundStatement:\n" +
                statements.stream().map(s -> s.astDebug(shift + 1)).collect(Collectors.joining("\n"));
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return statements.stream().map(c -> (AstNode) c).collect(Collectors.toList());
    }
}
