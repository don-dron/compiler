package lang.ast;

import lang.scope.Scope;

import java.util.List;

public abstract class AstNode {
    public static final String SHIFT = "....";

    private Scope scope;

    public String astDebug() {
        return astDebug(0);
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public abstract String astDebug(int shift);

    public abstract List<? extends AstNode> getChildren();
}