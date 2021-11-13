package lang.ir;

import java.util.List;

public class StructType extends Type {
    private final String name;
    private final List<VariableValue> values;
    private final long structId;

    public StructType(String name, List<VariableValue> values, long structId) {
        this.values = values;
        this.name = name;
        this.structId = structId;
    }

    @Override
    public String toLLVM() {
        return "%struct." + name;
    }

    public long getStructId() {
        return structId;
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

    public void setValues(List<VariableValue> values) {
        values.clear();
        values.addAll(values);
    }
}
