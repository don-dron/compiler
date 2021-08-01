package lang.ir;

import java.util.List;

public class Module {
    private final List<Function> functions;
    private final List<StructType> classes;
    private final List<StringValue> literals;

    public Module(
            List<StructType> classes,
            List<Function> functions,
            List<StringValue> literals) {
        this.functions = functions;
        this.classes = classes;
        this.literals = literals;
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
}
