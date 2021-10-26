package lang.ast.statement;

import lang.ast.AstNode;

import java.util.List;

public class ContinueStatementNode extends StatementNode {
    private AstNode cycle;

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "ContinueStatement";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return "continue";
    }

    public AstNode getCycle() {
        return cycle;
    }

    public void setCycle(AstNode cycle) {
        this.cycle = cycle;
    }
}
