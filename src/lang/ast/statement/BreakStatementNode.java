package lang.ast.statement;

import lang.ast.AstNode;

import java.util.List;

public class BreakStatementNode extends StatementNode {
    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "BreakStatement";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}
