package lang.ir;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private final List<Command> commands;

    public BasicBlock() {
        commands = new ArrayList<>();
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }
}
