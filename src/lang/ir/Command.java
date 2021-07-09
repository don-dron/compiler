package lang.ir;


import java.util.List;
import java.util.stream.Collectors;

import static lang.ir.Operation.ALLOC;
import static lang.ir.Operation.LOAD;
import static lang.ir.Operation.STORE;

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

    @Override
    public Type getType() {
        return result.getType();
    }

    @Override
    public String toLLVM() {
        if (operation == Operation.ADD) {
            return result.toLLVM() + " = add " + result.getType().toLLVM() + " " +
                    parameters
                            .stream()
                            .map(Value::toLLVM)
                            .collect(Collectors.joining(","));
        } else if (operation == LOAD) {
            return result.toLLVM() + " = load " + result.getType().toLLVM() + "* " +
                    parameters.get(0).toLLVM();
        } else if (operation == STORE) {
            return "store " + parameters.get(0).getType().toLLVM() + " " +
                    parameters.get(0).toLLVM() + ", " + result.getType().toLLVM() + "* " + result.toLLVM();
        } else if(operation == ALLOC) {
            return result.toLLVM() + " = alloca " + result.getType().toLLVM();
        }
        return "";
    }
}
