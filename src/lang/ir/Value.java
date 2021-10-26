package lang.ir;

public interface Value {
    public String toLLVM();

    public Type getType();
}
