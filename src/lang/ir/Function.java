package lang.ir;

import java.util.ArrayList;
import java.util.List;

public class Function implements Value {
    private final List<BasicBlock> blocks;
    private final String name;
    private final boolean systemFunction;
    private BasicBlock currentBlock;
    private BasicBlock returnBlock;
    private Value returnValue;
    private VariableValue thisValue;
    private Type resultType;
    private List<Type> parameterTypes;

    public Function(String name) {
        this(name, false);
    }

    public Function(String name, boolean systemFunction) {
        this.name = name;
        this.systemFunction = systemFunction;
        blocks = new ArrayList<>();
    }

    public boolean isSystemFunction() {
        return systemFunction;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    public BasicBlock appendBlock(String name) {
        BasicBlock basicBlock = BasicBlock.nextBlock(name);
        blocks.add(basicBlock);
        currentBlock = basicBlock;
        return basicBlock;
    }

    public BasicBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setReturnBlock(BasicBlock returnBlock) {
        this.returnBlock = returnBlock;
    }

    public BasicBlock getReturnBlock() {
        return returnBlock;
    }

    public void setReturnValue(Value variableValue) {
        this.returnValue = variableValue;
    }

    public Value getReturnValue() {
        return returnValue;
    }

    public void setResultType(Type resultType) {
        this.resultType = resultType;
    }

    public Type getType() {
        return resultType;
    }

    public void setThisValue(VariableValue thisValue) {
        this.thisValue = thisValue;
    }

    @Override
    public String toLLVM() {
        return name;
    }

    public void setParameterTypes(List<Type> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public VariableValue getThisValue() {
        return thisValue;
    }
}
