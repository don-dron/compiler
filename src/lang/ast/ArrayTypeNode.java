package lang.ast;

import java.util.Collections;
import java.util.List;

public class ArrayTypeNode extends TypeNode {
    private final TypeNode typeNode;

    public ArrayTypeNode(TypeNode typeNode) {
        this.typeNode = typeNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift) + "Array:\n" +
                typeNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return Collections.emptyList();
    }
}