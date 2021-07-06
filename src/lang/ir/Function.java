package lang.ir;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private final List<BasicBlock> blocks;
    private BasicBlock currentBlock;

    public Function() {
        blocks = new ArrayList<>();
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
}
