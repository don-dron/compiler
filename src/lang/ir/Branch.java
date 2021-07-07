package lang.ir;

public class Branch implements Terminator {
    private final BasicBlock target;

    public Branch(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "branch " + target.getName();
    }
}
