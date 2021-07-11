package lang.ir;


import java.util.List;
import java.util.stream.Collectors;

import static lang.ir.Operation.*;

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
        return (result == null ? "" : result.toString()) + " " + operation.toString() + " " +
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
        } else if (operation == SUB) {
            return result.toLLVM() + " = sub " + result.getType().toLLVM() + " " +
                    parameters
                            .stream()
                            .map(Value::toLLVM)
                            .collect(Collectors.joining(","));
        } else if (operation == MUL) {
            return result.toLLVM() + " = mul " + result.getType().toLLVM() + " " +
                    parameters
                            .stream()
                            .map(Value::toLLVM)
                            .collect(Collectors.joining(","));
        } else if (operation == AND) {
            return result.toLLVM() + " = and " + result.getType().toLLVM() + " " +
                    parameters
                            .stream()
                            .map(Value::toLLVM)
                            .collect(Collectors.joining(","));
        } else if (operation == LOAD) {
            return result.toLLVM() + " = load " + result.getType().toLLVM() + "," +
                    parameters.get(0).getType().toLLVM() + "* " +
                    parameters.get(0).toLLVM();
        } else if (operation == STORE) {
            return "store " + parameters.get(0).getType().toLLVM() + " " +
                    parameters.get(0).toLLVM() + ", " + result.getType().toLLVM() + "* " + result.toLLVM();
        } else if (operation == STORE_TO_POINTER) {
            return "store " + parameters.get(0).getType().toLLVM() + " " +
                    parameters.get(0).toLLVM() + ", " + result.getType().toLLVM() + " " + result.toLLVM();
        } else if (operation == ALLOC) {
            return result.toLLVM() + " = alloca " + result.getType().toLLVM();
        } else if (operation == GT
                || operation == GE
                || operation == LT
                || operation == LE
                || operation == EQ
                || operation == NE) {
            return result.toLLVM() + " = icmp " + operation.toLLVM() + " " +
                    parameters.get(0).getType().toLLVM() + " " + parameters.get(0).toLLVM() + " , " +
                    " " + parameters.get(1).toLLVM();
        } else if (operation == ARRAY_ALLOCATION) {
            return result.toLLVM() + " = call " + result.getType().toLLVM()
                    + " @malloc(" + parameters.get(1).getType().toLLVM() + " "
                    + parameters.get(1).toLLVM() + ")";
        } else if (operation == ARRAY_REFERENCE) {
            return result.toLLVM() + " = getelementptr inbounds " + result.getType().toLLVM() + " , "
                    + parameters.get(0).getType().toLLVM() + " " + parameters.get(0).toLLVM() + " , " +
                    parameters.get(1).getType().toLLVM() + " " + parameters.get(1).toLLVM();
        } else if (operation == ARRAY_ACCESS) {
            return result.toLLVM() + " = getelementptr inbounds " +
                    ((PointerType) parameters.get(0).getType()).getType().toLLVM() + " , "
                    + parameters.get(0).getType().toLLVM() + " " + parameters.get(0).toLLVM() + " , " +
                    parameters.get(1).getType().toLLVM() + " " + parameters.get(1).toLLVM();
        } else if (operation == CALL) {
            return (result == null ? "" : (result.toLLVM() + " = ")) + "call " +
                    (result == null ? "void" : result.getType().toLLVM())
                    + " @" + parameters.get(0).toLLVM() + "(" +
                    parameters.stream().skip(1).map(p -> p.getType().toLLVM() + " "
                            + p.toLLVM()).collect(Collectors.joining(",")) + ")";
        } else if (operation == CAST) {
            return result.toLLVM() + " = bitcast " + parameters.get(0).getType().toLLVM() + " " +
                    parameters.get(0).toLLVM() + " to " + result.getType().toLLVM();
        }
        return "";
    }
}
