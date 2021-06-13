package lang.ast;

import java.util.List;
import java.util.stream.Collectors;

public class ParametersNode extends AstNode {
    private final List<ParameterNode> parameters;

    public ParametersNode(List<ParameterNode> parameters) {
        this.parameters = parameters;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Parameters:\n" +
                parameters
                        .stream()
                        .map(entry -> entry.astDebug(shift + 1))
                        .collect(Collectors.joining("\n")))
                .stripTrailing();
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }
}