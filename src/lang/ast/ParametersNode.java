package lang.ast;

import java.util.List;
import java.util.Objects;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParametersNode that = (ParametersNode) o;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of();
    }

    @Override
    public String toString() {
        return parameters.stream().map(Objects::toString).collect(Collectors.joining(","));
    }
}