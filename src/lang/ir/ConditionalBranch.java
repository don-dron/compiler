package lang.ir;

public class ConditionalBranch implements Terminator {
    private final Value value;
    private BasicBlock left;
    private BasicBlock right;

    public ConditionalBranch(Value value, BasicBlock left, BasicBlock right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public BasicBlock getLeft() {
        return left;
    }

    public BasicBlock getRight() {
        return right;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "branch " + value.toString() + " " + left.getName() + "," + right.getName();
    }

    @Override
    public String toLLVM() {
        return "br " + value.getType().toLLVM() + " " + value.toLLVM() +
                ", label %" + left.getName() + ",label %" + right.getName();
    }

    public void setLeft(BasicBlock target) {
        left = target;
    }

    public void setRight(BasicBlock target) {
        right = target;
    }
}
