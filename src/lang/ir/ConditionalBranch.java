package lang.ir;

public class ConditionalBranch  implements Terminator {
    private final Command command;
    private final BasicBlock left;
    private final BasicBlock right;

    public ConditionalBranch(Command command, BasicBlock left, BasicBlock right) {
        this.command = command;
        this.left = left;
        this.right = right;
    }

    public BasicBlock getLeft() {
        return left;
    }

    public BasicBlock getRight() {
        return right;
    }

    public Command getCommand() {
        return command;
    }
}
