package lang.ast;

import lang.ast.statement.FunctionDefinitionNode;

import java.util.List;

public class Program {
    private final List<FunctionDefinitionNode> functions;
    private final List<FileNode> fileNodes;
    private final FunctionDefinitionNode mainFunction;

    public Program(FunctionDefinitionNode mainFunction, List<FunctionDefinitionNode> functions,
                   List<FileNode> fileNodes) {
        this.mainFunction = mainFunction;
        this.functions = functions;
        this.fileNodes = fileNodes;
    }

    public FunctionDefinitionNode getMainFunction() {
        return mainFunction;
    }

    public List<FunctionDefinitionNode> getFunctions() {
        return functions;
    }

    public List<FileNode> getFileNodes() {
        return fileNodes;
    }
}
