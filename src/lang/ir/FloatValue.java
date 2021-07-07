package lang.ir;

public class FloatValue implements Value {
    private final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
