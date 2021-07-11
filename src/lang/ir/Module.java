package lang.ir;

import java.util.List;

public class Module {
    private final List<Function> functions;
    private final List<StructType> classes;

    public Module(
            List<StructType> classes,
            List<Function> functions) {
        this.functions = functions;
        this.classes = classes;
    }

    public List<StructType> getClasses() {
        return classes;
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
