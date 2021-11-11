package lang.ir;

public class PointerType extends Type {
    private final Type type;

    public PointerType(Type type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toLLVM() {
        return type.toLLVM() + "*";
    }

    @Override
    public String toString() {
        return type.toLLVM() + "*";
    }

    @Override
    public int getSize() {
        return 8;
    }
}
