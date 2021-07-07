package lang.ir;


import java.util.List;
import java.util.stream.Collectors;

public class Command implements Value {
    private final Value result;
    private final Operation operation;
    private final List<Value> parameters;

    public Command(Value result, Operation operation, List<Value> parameters) {
        this.result = result;
        this.operation = operation;
        this.parameters = parameters;
    }

    public List<Value> getParameters() {
        return parameters;
    }

    public Operation getOperation() {
        return operation;
    }

    public Value getResult() {
        return result;
    }

    @Override
    public String toString() {
        return result.toString() + " " + operation.toString() + " " +
                parameters.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
    }
}
