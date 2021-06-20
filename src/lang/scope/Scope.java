package lang.scope;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;

import java.util.ArrayList;
import java.util.List;

public class Scope {
    private static int index = 0;

    private final Scope parentScope;
    private final List<AstNode> nodes;
    private final List<AstNode> declarations;
    private final int number = index++;

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
        declarations.add(node);
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
}
