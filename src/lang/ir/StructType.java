package lang.ir;

import java.util.List;

public class StructType {
    private final String name;
    private final List<VariableValue> values;

    public StructType(String name, List<VariableValue> values) {
        this.values = values;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<VariableValue> getTypes() {
        return values;
    }
}
