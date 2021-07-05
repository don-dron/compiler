package lang.ir;

import java.util.List;

public class Module {
    private final List<Function> functions;

    public Module(List<Function> functions) {
        this.functions = functions;
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
