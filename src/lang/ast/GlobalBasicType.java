package lang.ast;

import lang.Position;

import static lang.ast.TypeNode.Type.*;

public class GlobalBasicType extends BasicTypeNode {
    public static final BasicTypeNode VOID_TYPE = new GlobalBasicType(VOID);
    public static final BasicTypeNode BOOL_TYPE = new GlobalBasicType(BOOL);
    public static final BasicTypeNode LONG_TYPE = new GlobalBasicType(LONG);
    public static final BasicTypeNode INT_TYPE = new GlobalBasicType(INT);
    public static final BasicTypeNode FLOAT_TYPE = new GlobalBasicType(FLOAT);
    public static final BasicTypeNode REF_TYPE = new GlobalBasicType(REFERENCE);

    public GlobalBasicType(Type type) {
        super(type, null);
    }

    @Override
    public Position getStart() {
        throw new IllegalStateException("");
    }

    @Override
    public Position getEnd() {
        throw new IllegalStateException("");
    }

    @Override
    public String getFormattedText() {
        throw new IllegalStateException("");
    }
}
