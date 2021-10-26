package lang.ir;

public class IntValue implements Value {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String toLLVM() {
        return String.valueOf(value);
    }

    @Override
    public Type getType() {
        return Type.INT_32;
    }
}
