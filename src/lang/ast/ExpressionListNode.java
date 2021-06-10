package lang.ast;

import lang.ast.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionListNode extends AstNode {
    private final List<ExpressionNode> list;

    public ExpressionListNode(List<ExpressionNode> list) {
        this.list = list;
    }

    public List<ExpressionNode> getList() {
        return list;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "ExpressionList:\n" +
                list.stream()
                        .map(entry -> entry.astDebug(shift + 1))
                        .collect(Collectors.joining("\n")))
                .stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return new ArrayList<>(list);
    }
}