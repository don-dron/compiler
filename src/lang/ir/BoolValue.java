package lang.ir;

public class BoolValue implements Value {
    private final boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String toLLVM() {
        return String.valueOf(value ? 1 : 0);
    }

    @Override
    public Type getType() {
        return Type.INT_8;
    }
}
