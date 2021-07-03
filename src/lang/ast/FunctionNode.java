package lang.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FunctionNode extends TypeNode {
    private final TypeNode typeNode;
    private final ParametersNode parametersNode;

    public FunctionNode(ParametersNode parametersNode,
                        TypeNode typeNode) {
        this.typeNode = typeNode;
        this.parametersNode = parametersNode;
    }

    public ParametersNode getParametersNode() {
        return parametersNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public String astDebug(int shift) {
        return SHIFT.repeat(shift) + "Function:\n" +
                (typeNode != null ? (typeNode.astDebug(shift + 1) + "\n") : "") +
                parametersNode.astDebug(shift + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionNode that = (FunctionNode) o;
        return Objects.equals(typeNode, that.typeNode) &&
                Objects.equals(parametersNode, that.parametersNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeNode, parametersNode);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "(" + parametersNode.toString() + ") " + typeNode.toString();
    }
}