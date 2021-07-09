package lang.ir;

public enum Type {
    VOID, INT_8, INT_16, INT_32, INT_64, POINTER;

    public String toLLVM() {
        switch (this) {
            case VOID:
                return "";
            case INT_8:
                return "i1";
            case INT_16:
                return "i16";
            case INT_32:
                return "i32";
            case INT_64:
                return "i64";
            default:
                throw new IllegalArgumentException();
        }
    }
}
