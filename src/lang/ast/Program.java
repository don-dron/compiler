package lang.ast;

import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.ConstructorDefinitionNode;
import lang.ast.statement.FunctionDefinitionNode;

import java.util.List;

public class Program {
    private final List<FunctionDefinitionNode> functions;
    private final List<FileNode> fileNodes;
    private final FunctionDefinitionNode mainFunction;
    private final List<ClassStatementNode> classes;
    private final List<ConstructorDefinitionNode> constructors;

    public Program(FunctionDefinitionNode mainFunction,
                   List<ClassStatementNode> classes,
                   List<ConstructorDefinitionNode> constructors,
                   List<FunctionDefinitionNode> functions,
                   List<FileNode> fileNodes) {
        this.mainFunction = mainFunction;
        this.constructors = constructors;
        this.classes = classes;
        this.functions = functions;
        this.fileNodes = fileNodes;
    }
    
    public List<ConstructorDefinitionNode> getConstructors() {
        return constructors;
    }

    public List<ClassStatementNode> getClasses() {
        return classes;
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
