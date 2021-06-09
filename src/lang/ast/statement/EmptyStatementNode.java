package lang.ast.statement;

import lang.ast.AstNode;

import java.util.List;

public class EmptyStatementNode extends StatementNode {

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Empty";
    }

    @Override
    public List<AstNode> getChildren() {
        return List.of();
    }
}
