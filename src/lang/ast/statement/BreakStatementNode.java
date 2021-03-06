package lang.ast.statement;

import lang.ast.AstNode;

import java.util.List;

public class BreakStatementNode extends StatementNode {
    private AstNode cycle;

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "BreakStatement";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return "break";
    }

    public void setCycle(AstNode cycle) {
        this.cycle = cycle;
    }

    public AstNode getCycle() {
        return cycle;
    }
}
