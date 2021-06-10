package lang.ast;

import java.util.Collections;
import java.util.List;

public class FunctionNode extends TypeNode {
    private final TypeNode typeNode;
    private final ParameterNode parameterNode;

    public FunctionNode(ParameterNode parameterNode,
                        TypeNode typeNode) {
        this.typeNode = typeNode;
        this.parameterNode = parameterNode;
    }

    public ParameterNode getParameterNode() {
        return parameterNode;
    }

    public TypeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift) + "Function:\n" +
                typeNode.astDebug(shift + 1) + "\n" +
                parameterNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return Collections.emptyList();
    }
}