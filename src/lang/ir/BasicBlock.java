package lang.ir;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private static int BASIC_BLOCK_COUNT = 0;
    private final List<Command> commands;
    private final String name;
    private final List<BasicBlock> input;
    private final List<BasicBlock> output;
    private Terminator terminator;

    public BasicBlock(String name) {
        this.name = name;
        commands = new ArrayList<>();
        input = new ArrayList<>();
        output = new ArrayList<>();
    }

    public void addInput(BasicBlock basicBlock) {
        input.add(basicBlock);
    }

    public void addOutput(BasicBlock basicBlock) {
        output.add(basicBlock);
    }

    public List<BasicBlock> getInput() {
        return input;
    }

    public List<BasicBlock> getOutput() {
        return output;
    }

    public void setTerminator(Terminator terminator) {
        this.terminator = terminator;
    }

    public Terminator getTerminator() {
        return terminator;
    }

    public static BasicBlock nextBlock(String name) {
        return new BasicBlock(name + "_" + BASIC_BLOCK_COUNT++);
    }

    public String getName() {
        return name;
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }
}
