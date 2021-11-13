package lang.ir;

import java.util.List;

public class Module {
    private final List<Function> functions;
    private final List<StructType> classes;
    private final List<StringValue> literals;
    private final List<VariableValue> globalVars;

    public Module(
            List<StructType> classes,
            List<Function> functions,
            List<StringValue> literals,
            List<VariableValue> globalVars) {
        this.functions = functions;
        this.classes = classes;
        this.literals = literals;
        this.globalVars = globalVars;
    }

    public List<StructType> getClasses() {
        return classes;
    }

    public List<StringValue> getLiterals() {
        return literals;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<VariableValue> getGlobalVars() {
        return globalVars;
    }
}
