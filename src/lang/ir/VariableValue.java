package lang.ir;

public class VariableValue implements Value {
    private final String name;
    private final Type type;
    private final boolean global;

    public VariableValue(String name, Type type) {
        this(name, type, false);
    }

    public VariableValue(String name, Type type, boolean global) {
        this.name = name;
        this.type = type;
        this.global = global;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isGlobal() {
        return global;
    }

    @Override
    public String toString() {
        return "(" + name + "," + type.toString() + ")";
    }

    @Override
    public String toLLVM() {
        return (isGlobal() ? "@" : "%") + name;
    }
}
