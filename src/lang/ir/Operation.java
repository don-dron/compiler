package lang.ir;

public enum  Operation {
    ALLOC("alloc"),

    STORE("store"),
    LOAD("load"),

    ADD("<- add"),
    SUB("<- sub"),
    MUL("<- mul"),
    DIV("<- div"),
    MOD("<- mod"),

    AND("<- and"),
    OR("<- or"),

    GT("<- gt"),
    GE("<- ge"),

    LT("<- lt"),
    LE("<- le"),

    EQ("<- eq"),
    NE("<- ne"),

    CALL("<- call"),
    ARRAY_ACCESS("<- array_access"),
    ARRAY_REFERENCE("<- array_left_access"),
    FIELD_ACCESS("<- field_access"),
    ARRAY_ALLOCATION("<- array_allocation"),
    STRUCT_ALLOCATION("<- struct_allocation"),

    CAST("<- cast"),
    TRUNC("<- trunc"),
    SEXT("<- sext"),

    STORE_TO_POINTER("<- store_ptr");

    private final String print;

    Operation(String  print) {
        this.print = print;
    }

    @Override
    public String toString() {
        return print;
    }

    public String toLLVM() {
        switch (this) {
            case ADD:
                return "add";
            case SUB:
                return "sub";
            case MUL:
                return "mul";
            case DIV:
                return "sdiv";
            case GE:
                return "sge";
            case GT:
                return "sgt";
            case LE:
                return "sle";
            case LT:
                return "slt";
            case NE:
                return "ne";
            case EQ:
                return "eq";
            default:
                throw new IllegalArgumentException();
        }
    }
}
