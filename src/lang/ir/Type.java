package lang.ir;

public enum Type implements Value {
    VOID, INT_1, INT_8, INT_16, INT_32, INT_64, POINTER_INT_32, POINTER_INT_64, POINTER_INT_16, POINTER_INT_8, POINTER_INT_1;

    public String toLLVM() {
        switch (this) {
            case VOID:
                return "";
            case INT_1:
            case INT_8:
                return "i1";
            case INT_16:
                return "i16";
            case INT_32:
                return "i32";
            case INT_64:
                return "i64";
            case POINTER_INT_1:
            case POINTER_INT_8:
                return "i1*";
            case POINTER_INT_16:
                return "i16*";
            case POINTER_INT_32:
                return "i32*";
            case POINTER_INT_64:
                return "i64*";
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Type getType() {
        return this;
    }
}
