package lang.ir.translate;

import lang.ir.Type;
import lang.ir.Value;

public class LocalVariableValue implements Value {
    private final Type type;
    private final int index;

    public LocalVariableValue(int i, Type type) {
        this.index = i;
        this.type= type;
    }

    @Override
    public String toLLVM() {
        return "%" + index;
    }

    @Override
    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
