package lang.ast.statement;

import lang.ast.AstNode;

import java.util.List;

public class EmptyStatementNode extends StatementNode {

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "EmptyStatement";
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return "";
    }
}
