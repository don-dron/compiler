package lang.ir;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private final List<BasicBlock> blocks;

    public Function() {
        blocks = new ArrayList<>();
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }
}
