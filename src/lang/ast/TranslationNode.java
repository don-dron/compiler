package lang.ast;

import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TranslationNode extends AstNode {
    private final List<StatementNode> statements;

    public TranslationNode( List<StatementNode> statements) {
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "TranslationNode:\n" + statements.stream()
                .map(statementNode -> statementNode.astDebug(shift + 1))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return new ArrayList<>(statements);
    }
}
