package lang.ir;

public class VariableValue implements Value {
    private final String name;
    private final Type type;

    public VariableValue(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "(" + name + "," + type.toString() + ")";
    }

    @Override
    public String toLLVM() {
        return "%" + name;
    }
}
