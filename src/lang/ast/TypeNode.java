package lang.ast;

public abstract class TypeNode extends AstNode {
    public enum Type {
        BOOL(1), INT(4), FLOAT(4),  REFERENCE(16), VOID(32),DOUBLE(8),
        LONG(8), SHORT(2), CHAR(1);

        int size;
        Type(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }
}