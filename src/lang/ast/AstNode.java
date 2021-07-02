package lang.ast;

import lang.Position;
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

    public String getFormattedText() {
        return null;
    }

    public Position getEnd() {
        return null;
    }

    public Position getStart() {
        return null;
    }

    public abstract String astDebug(int shift);

    public abstract List<? extends AstNode> getChildren();
}