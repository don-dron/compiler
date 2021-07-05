package lang.ast;

import lang.ast.statement.FunctionDefinitionNode;

import java.util.List;

public class Program {
    private final List<FunctionDefinitionNode> functions;
    private final List<FileNode> fileNodes;

    public Program(List<FunctionDefinitionNode> functions,
                   List<FileNode> fileNodes) {
        this.functions = functions;
        this.fileNodes = fileNodes;
    }

    public List<FunctionDefinitionNode> getFunctions() {
        return functions;
    }

    public List<FileNode> getFileNodes() {
        return fileNodes;
    }
}
