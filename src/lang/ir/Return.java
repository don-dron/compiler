package lang.ir;

public class Return implements Terminator {
    private final Value value;

    public Return(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "return" + (value == null ? "" : (" " + value.toString()));
    }

    @Override
    public String toLLVM() {
        return "ret " + (value == null ? "" : (value.getType().toLLVM() + " " + value.toLLVM()));
    }
}
