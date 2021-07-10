package lang.ir;

public class Type implements Value {
    public static final Type VOID = new Type();
    public static final Type INT_1 = new Type();
    public static final Type INT_8 = new Type();
    public static final Type INT_16 = new Type();
    public static final Type INT_32 = new Type();
    public static final Type INT_64 = new Type();

    public String toLLVM() {
        if (this == VOID) {
            return "";
        } else if (this == INT_1 || this == INT_8) {
            return "i1";
        } else if (this == INT_16) {
            return "i16";
        } else if (this == INT_32) {
            return "i32";
        } else if (this == INT_64) {
            return "i64";
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return toLLVM();
    }

    @Override
    public Type getType() {
        return this;
    }
}
