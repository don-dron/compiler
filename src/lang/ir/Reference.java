package lang.ir;

public class Reference implements Value {
    private final Value pointerValue;
    private final Value offsetValue;
    private final Type type;

    public Reference(Type type, Value pointerValue, Value value) {
        this.type = type;
        this.pointerValue = pointerValue;
        this.offsetValue = value;
    }

    public Value getPointerValue() {
        return pointerValue;
    }

    public Value getOffsetValue() {
        return offsetValue;
    }

    @Override
    public String toString() {
        return pointerValue.toString() + "[" + offsetValue + "]";
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toLLVM() {
        return "";
    }
}
