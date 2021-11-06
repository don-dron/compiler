package lang.ir;

import java.util.List;

public class StructType extends Type {
    private final String name;
    private final List<VariableValue> values;

    public StructType(String name, List<VariableValue> values) {
        this.values = values;
        this.name = name;
    }

    @Override
    public String toLLVM() {
        return "%struct." + name;
    }

    @Override
    public String toString() {
        return toLLVM();
    }

    public String getName() {
        return name;
    }

    public List<VariableValue> getTypes() {
        return values;
    }

    @Override
    public int getSize() {
        return values.stream()
                .map(v -> v.getType().getSize())
                .reduce(Integer::sum)
                .orElseThrow();
    }
}
