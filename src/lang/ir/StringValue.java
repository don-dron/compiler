package lang.ir;

public class StringValue implements Value {
    private final String name;
    private final String value;

    public StringValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String toLLVM() {
        return name;
    }

    @Override
    public Type getType() {
        return new LiteralType(value.length());
    }
}