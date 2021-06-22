package lang.scope;

import lang.ast.AstNode;
import lang.ast.ParameterNode;
import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scope {
    private static int VARIABLE_COUNT = 0;
    private static int SCOPE_COUNT = 0;

    private final Scope parentScope;
    private final List<AstNode> nodes;
    private final List<AstNode> declarations;
    private final int number = SCOPE_COUNT++;

    private AstNode owner;

    public Scope(Scope scope) {
        this.parentScope = scope;
        this.declarations = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public void setOwner(AstNode owner) {
        this.owner = owner;
    }

    public AstNode getOwner() {
        return owner;
    }

    public void addDeclaration(AstNode node) {
        if (node instanceof ParameterNode) {
            ParameterNode parameterNode = (ParameterNode)node;
            parameterNode.getIdentifierNode().setName(nextName(parameterNode.getIdentifierNode().getName()));
        } else if (node instanceof DeclarationStatementNode) {
            DeclarationStatementNode declarationStatementNode = (DeclarationStatementNode)node;
            declarationStatementNode.getIdentifierNode()
                    .setName(nextName(declarationStatementNode.getIdentifierNode().getName()));
        } else if (node instanceof FunctionDefinitionNode) {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode)node;
            functionDefinitionNode.getIdentifierNode()
                    .setName(nextName(functionDefinitionNode.getIdentifierNode().getName()));
        } else if (node instanceof ClassStatementNode) {
            ClassStatementNode classStatementNode = (ClassStatementNode)node;
            classStatementNode.getIdentifierNode().setName(nextName(classStatementNode.getIdentifierNode().getName()));
        } else if (node instanceof InterfaceStatementNode) {
            InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode)node;
            interfaceStatementNode.getIdentifierNode()
                    .setName(nextName(interfaceStatementNode.getIdentifierNode().getName()));
        } else {
            throw new IllegalArgumentException("((((");
        }

        declarations.add(node);
    }

    public void addWeakDeclaration(StatementNode statementNode) {
        declarations.add(statementNode);
    }

    private String nextName(String name) {
        String nextName = "$" + VARIABLE_COUNT + "_" + name;
        VARIABLE_COUNT++;
        return nextName;
    }

    public List<AstNode> getDeclarations() {
        return declarations;
    }

    public int getNumber() {
        return number;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public List<AstNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "number:" + number + (parentScope != null ? (" parent: " + parentScope.getNumber()) : "");
    }

    public AstNode findDefinitionByVariable(String currentName) {
        if (currentName == null) {
            throw new IllegalArgumentException("");
        }

        Pattern pattern = Pattern.compile("\\$\\d+_" + currentName);

        AstNode node = this.getDeclarations()
                .stream()
                .filter(n -> {
                    if (n instanceof DeclarationStatementNode) {
                        Matcher matcher = pattern.matcher(((DeclarationStatementNode) n).getIdentifierNode().getName());
                        return matcher.find();
                    } else if (n instanceof ParameterNode) {
                        Matcher matcher = pattern.matcher(((ParameterNode) n).getIdentifierNode().getName());
                        return matcher.find();
                    } else if (n instanceof InterfaceStatementNode) {
                        Matcher matcher = pattern.matcher(((InterfaceStatementNode) n).getIdentifierNode().getName());
                        return matcher.find();
                    } else if (n instanceof ClassStatementNode) {
                        Matcher matcher = pattern.matcher(((ClassStatementNode) n).getIdentifierNode().getName());
                        return matcher.find();
                    } else if (n instanceof FunctionDefinitionNode) {
                        Matcher matcher = pattern.matcher(((FunctionDefinitionNode) n).getIdentifierNode().getName());
                        return matcher.find();
                    }

                    return false;
                })
                .findFirst()
                .orElse(null);

        if (node == null && parentScope != null) {
            return parentScope.findDefinitionByVariable(currentName);
        }

        return node;
    }
}
