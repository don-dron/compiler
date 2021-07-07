package lang.ir;

public enum  Operation {

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
    NE("<- ne")

    ;

    private final String print;

    Operation(String  print) {
        this.print = print;
    }

    @Override
    public String toString() {
        return print;
    }
}
