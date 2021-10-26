package lang.ir;

public class NullValue implements Value {
    private final Type type;

    public NullValue(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public String toLLVM() {
        return "null";
    }

    @Override
    public Type getType() {
        return type;
    }
}
