package lang.semantic;

import lang.ast.FileNode;
import lang.ast.FunctionDefinitionNode;
import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.EmptyStatementNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnalysis {
    private final List<FileNode> fileNodes;
    private final Map<FileNode, List<DeclarationStatementNode>> externDeclarationStatementNodes;
    private final Map<FileNode, List<InterfaceStatementNode>> externIntefaceStatementsNodes;
    private final Map<FileNode, List<ClassStatementNode>> externClassStatementsNodes;
    private final Map<FileNode, List<FunctionDefinitionNode>> externFunctionDefinitionNodes;

    public SemanticAnalysis(List<FileNode> fileNodes) {
        this.fileNodes = fileNodes;
        this.externDeclarationStatementNodes = new HashMap<>();
        this.externIntefaceStatementsNodes = new HashMap<>();
        this.externClassStatementsNodes = new HashMap<>();
        this.externFunctionDefinitionNodes = new HashMap<>();
    }

    public void analyse() {
        for (FileNode fileNode : fileNodes) {
            analyseDefinitions(fileNode);
        }

        System.out.println(Stream.of(externClassStatementsNodes,
                externDeclarationStatementNodes,
                externFunctionDefinitionNodes,
                externIntefaceStatementsNodes)
                .flatMap(mp -> mp.values().stream())
                .flatMap(Collection::stream)
                .map(st -> st.astDebug(0))
                .collect(Collectors.joining("\n")));
    }

    public void analyseDefinitions(FileNode fileNode) {
        fileNode.getStatementNodes().removeIf(s -> s instanceof EmptyStatementNode);

        for (StatementNode statementNode : fileNode.getStatementNodes()) {
            if (statementNode instanceof ClassStatementNode) {
                externClassStatementsNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add((ClassStatementNode) statementNode);
            } else if (statementNode instanceof InterfaceStatementNode) {
                externIntefaceStatementsNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add((InterfaceStatementNode) statementNode);
            } else if (statementNode instanceof FunctionDefinitionNode) {
                externFunctionDefinitionNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add((FunctionDefinitionNode) statementNode);
            } else if (statementNode instanceof DeclarationStatementNode) {
                externDeclarationStatementNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add((DeclarationStatementNode) statementNode);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }
}
