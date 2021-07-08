package lang.ir;

public class LongValue implements Value {
    private final long value;

    public LongValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
