package lang.ir;

public class GlobalVariableValue extends VariableValue {
    private final Value value;

    public GlobalVariableValue(String name, Type type, Value value) {
        super(name, type, true);
        this.value = value;
    }

    public Value getValue() {
        return value;
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
        return "@" + name;
    }
}
