package lang.ast;

import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс описывающий файл программы
 *
 * содержит только объявления, определения и импорты
 */
public class TranslationNode extends AstNode {
    private final String modulePath;
    private final List<StatementNode> statements;

    public TranslationNode(String modulePath, List<StatementNode> statements) {
        this.modulePath = modulePath;
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String astDebug(int shift) {
        return "Module " + modulePath + ":\n" + statements.stream()
                .map(statementNode -> statementNode.astDebug(shift + 1))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public List<AstNode> getChildren() {
        return new ArrayList<>(statements);
    }
}
