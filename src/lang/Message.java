package lang;

public class Message {
    private final String content;
    private final Position start;
    private final Position end;

    public Message(String content, Position start, Position end) {
        this.content = content;
        this.start = start;
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "ERROR " + content + " : " + start;
    }
}
