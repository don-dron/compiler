package lang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        return typeNode.toString() + "[]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayTypeNode that = (ArrayTypeNode) o;
        return Objects.equals(typeNode, that.typeNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeNode);
    }
}