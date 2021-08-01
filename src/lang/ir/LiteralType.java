package lang.ir;

public class LiteralType extends Type {

    private final int size;

    public LiteralType(int size) {
        this.size = size;
    }

    @Override
    public Type getType() {
        return this;
    }

    @Override
    public String toLLVM() {
        return "[" + size + " x i8" + "]*";
    }

    @Override
    public String toString() {
        return "[" + size + " x i8" + "]*";
    }

    @Override
    public int getSize() {
        return size;
    }
}
