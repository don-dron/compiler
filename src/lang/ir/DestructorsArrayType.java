package lang.ir;

import java.util.List;
import java.util.stream.Collectors;

public class DestructorsArrayType extends Type {

    private final Type type;
    private final int size;
    private final List<Function> values;

    public DestructorsArrayType(Type type, int size, List<Function> values) {
        this.type = type;
        this.size = size;
        this.values = values;
    }

    public List<Function> getValues() {
        return values;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String toLLVM() {
        return "[" + size + " x void (i32*)*] ";
    }

    @Override
    public Type getType() {
        return type;
    }
}
