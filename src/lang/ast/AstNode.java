package lang.ast;

import java.util.List;

public abstract class AstNode {
    public static final String SHIFT = "....";

    public boolean visit;

    public String astDebug() {
        return astDebug(0);
    }

    public abstract String astDebug(int shift);

    public abstract List<AstNode> getChildren();
}