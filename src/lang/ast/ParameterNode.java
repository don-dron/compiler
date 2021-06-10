package lang.ast;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterNode extends AstNode {
    private final List<Pair<IdentifierNode, TypeNode>> parameters;

    public ParameterNode(List<Pair<IdentifierNode, TypeNode>> parameters) {
        this.parameters = parameters;
    }

    public List<Pair<IdentifierNode, TypeNode>> getParameters() {
        return parameters;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Parameters:\n" +
                parameters
                        .stream()
                        .map(entry -> SHIFT.repeat(shift + 1)
                                + entry.getKey().getName() +
                                " -> \n" + entry.getValue().astDebug(shift + 1))
                        .collect(Collectors.joining("\n")))
                .stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}