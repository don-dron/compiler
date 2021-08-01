package lang.ir;

public class CharValue implements Value {
    private final char value;

    public CharValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf((int)value);
    }

    @Override
    public String toLLVM() {
        return String.valueOf((int)value);
    }

    @Override
    public Type getType() {
        return Type.INT_8;
    }
}
