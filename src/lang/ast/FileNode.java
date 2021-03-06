package lang.ast;

import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileNode extends AstNode {
    private final List<ImportNode> importNodes;
    private final List<StatementNode> statementNodes;
    private final String path;

    public FileNode(String path,
                    String pack,
                    List<ImportNode> importNodes,
                    List<StatementNode> statementNodes) {
        this.path = path;
        this.importNodes = importNodes;
        this.statementNodes = statementNodes;
    }

    public String getPath() {
        return path;
    }

    public List<StatementNode> getStatementNodes() {
        return statementNodes;
    }

    public List<ImportNode> getImportNodes() {
        return importNodes;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "FileNode " + path + " :\n" +
                (!importNodes.isEmpty() ? (importNodes.stream()
                        .map(i -> i.astDebug(shift + 1))
                        .collect(Collectors.joining("\n")) + "\n") : "") +
                statementNodes.stream()
                        .map(i -> i.astDebug(shift + 1))
                        .collect(Collectors.joining("\n"))).stripLeading();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return new ArrayList<>(statementNodes);
    }

    @Override
    public String toString() {
        return importNodes.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                statementNodes.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }
}
