package lang.semantic;

import lang.ast.AstNode;
import lang.ast.ParameterNode;
import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scope {
    private static int VARIABLE_COUNT = 0;
    private static int SCOPE_COUNT = 0;

    private final Scope parentScope;
    private final List<Scope> alternativeScopes = new ArrayList<>();
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

    public void addAlternativeScope(Scope scope) {
        alternativeScopes.add(scope);
    }

    public List<Scope> getAlternativeScopes() {
        return alternativeScopes;
    }

    public void addDeclaration(AstNode node) {
        String originalName;
        if (node instanceof ParameterNode) {
            ParameterNode parameterNode = (ParameterNode) node;
            originalName = parameterNode.getIdentifierNode().getName();
            parameterNode.getIdentifierNode()
                    .setName(nextName(parameterNode.getIdentifierNode().getName()));
        } else if (node instanceof DeclarationStatementNode) {
            DeclarationStatementNode declarationStatementNode = (DeclarationStatementNode) node;
            originalName = declarationStatementNode.getIdentifierNode().getName();
            declarationStatementNode.getIdentifierNode()
                    .setName(nextName(declarationStatementNode.getIdentifierNode().getName()));
        } else if (node instanceof FunctionDefinitionNode) {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
            originalName = functionDefinitionNode.getIdentifierNode().getName();
            functionDefinitionNode.getIdentifierNode()
                    .setName(nextName(functionDefinitionNode.getIdentifierNode().getName()));
        } else if (node instanceof ClassStatementNode) {
            ClassStatementNode classStatementNode = (ClassStatementNode) node;
            originalName = classStatementNode.getIdentifierNode().getName();
            classStatementNode.getIdentifierNode().setName(nextName(classStatementNode.getIdentifierNode().getName()));
        } else if (node instanceof InterfaceStatementNode) {
            InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode) node;
            originalName = interfaceStatementNode.getIdentifierNode().getName();
            interfaceStatementNode.getIdentifierNode()
                    .setName(nextName(interfaceStatementNode.getIdentifierNode().getName()));
        } else {
            throw new IllegalArgumentException("((((");
        }

        if (matchDeclaration(originalName) != null) {
            throw new IllegalArgumentException("Already defined " + originalName);
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

        AstNode node = matchDeclaration(currentName);

        if (node == null && parentScope != null) {
            node = parentScope.findDefinitionByVariable(currentName);
        }

        if (node == null) {
            node = matchInAlternativeScopes(currentName);
        }

        return node;
    }

    public AstNode matchInAlternativeScopes(String currentName) {
        return alternativeScopes.stream()
                .map(i -> Optional.ofNullable(i.matchDeclaration(currentName))
                        .orElse(i.matchInAlternativeScopes(currentName)))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public AstNode matchDeclaration(String currentName) {
        Pattern pattern = Pattern.compile("\\$\\d+_" + currentName + "\\Z");

        return declarations
                .stream()
                .filter(n -> {
                    Matcher matcher = null;
                    if (n instanceof DeclarationStatementNode) {
                        matcher = pattern.matcher(((DeclarationStatementNode) n).getIdentifierNode().getName());
                    } else if (n instanceof ParameterNode) {
                        matcher = pattern.matcher(((ParameterNode) n).getIdentifierNode().getName());
                    } else if (n instanceof InterfaceStatementNode) {
                        matcher = pattern.matcher(((InterfaceStatementNode) n).getIdentifierNode().getName());
                    } else if (n instanceof ClassStatementNode) {
                        matcher = pattern.matcher(((ClassStatementNode) n).getIdentifierNode().getName());
                    } else if (n instanceof FunctionDefinitionNode) {
                        matcher = pattern.matcher(((FunctionDefinitionNode) n).getIdentifierNode().getName());
                    } else {
                        return false;
                    }

                    return matcher.find();
                })
                .findFirst()
                .orElse(null);
    }
}
