package lang.ir;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionType extends Type {

    private final Type resultType;
    private final List<Type> parameterTypes;

    public FunctionType(Type resultType,
                        List<Type> parameterTypes) {
        this.resultType = resultType;
        this.parameterTypes = parameterTypes;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public Type getResultType() {
        return resultType;
    }

    @Override
    public String toLLVM() {
        return ((resultType == null || resultType == VOID) ? "void" : resultType.toLLVM()) + " " +
                "(" + parameterTypes.stream()
                .map(p -> p.toLLVM())
                .collect(Collectors.joining(",")) + ")";
    }
}
