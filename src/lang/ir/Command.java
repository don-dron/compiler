package lang.ir;


import java.util.List;

public class Command {
    private final Variable result;
    private final Operation operation;
    private final List<Variable> parameters;

    public Command(Variable result, Operation operation, List<Variable> parameters) {
        this.result = result;
        this.operation = operation;
        this.parameters = parameters;
    }

    public List<Variable> getParameters() {
        return parameters;
    }

    public Operation getOperation() {
        return operation;
    }

    public Variable getResult() {
        return result;
    }
}
