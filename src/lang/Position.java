package lang;

public class Position {
    private final int column;
    private final int line;
    private final int position;

    public Position(int column, int line, int position) {
        this.column = column;
        this.line = line;
        this.position = position;
    }

    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "(" + column +
                ", " + line +
                ")";
    }
}
