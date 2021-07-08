package lang.ir;

import java.util.ArrayList;
import java.util.List;

public class Function implements Value {
    private final List<BasicBlock> blocks;
    private final String name;
    private BasicBlock currentBlock;
    private BasicBlock returnBlock;
    private Value returnValue;

    public Function(String name) {
        this.name = name;
        blocks = new ArrayList<>();
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
}
