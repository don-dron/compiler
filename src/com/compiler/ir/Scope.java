package com.compiler.ir;

import java.util.*;
import java.util.stream.Collectors;

import static com.compiler.ast.AstNode.SHIFT;

public class Scope {
    private static int count = 0;
    private final Scope parentScope;
    private final Set<Variable> variables;
    private final List<Scope> children;
    private final Map<String, String> scopeRenaming;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
        this.variables = new HashSet<>();
        this.children = new ArrayList<>();
        this.scopeRenaming = new HashMap<>();
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    public void addScope(Scope scope) {
        children.add(scope);
    }

    public String getNewName(String oldName) {
        String name = scopeRenaming.get(oldName);

        if(name == null) {
            if (parentScope != null) {
                name = parentScope.getNewName(oldName);
            }
        }

        if(name == null) {
            throw new IllegalArgumentException("Not declared variable " + oldName);
        }

        return name;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    @Override
    public String toString() {
        return treeDebug(0);
    }

    public String treeDebug(int count) {
        return (SHIFT.repeat(count) + "Variables:\n" + SHIFT.repeat(count + 1) +
                variables.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n" + SHIFT.repeat(count + 1))) + "\n"
                + children.stream()
                .map(c -> c.treeDebug(count + 1))
                .collect(Collectors.joining("\n")))
                .stripTrailing();
    }

    public boolean isVariableExist(String name) {
        return variables.stream().anyMatch(v -> v.getName().equals(name))
                || parentScope != null && parentScope.isVariableExist(name);
    }

    public Variable addVariable(String name, Type type, BasicBlock currentBlock) {
        String newName = name + "$" + count++;
        Variable variable = new Variable(newName, type, this, currentBlock);
        scopeRenaming.put(name, newName);
        variables.add(variable);
        return variable;
    }
}
