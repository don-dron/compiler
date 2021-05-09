package com.compiler.lexer;

public class Message {
    private final String content;
    private final Position position;

    public Message(String content, Position position) {
        this.content = content;
        this.position = position;
    }

    public String getContent() {
        return content;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "ERROR " + content + " : " + position;
    }
}
