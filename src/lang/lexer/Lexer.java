package lang.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    private static final String PATTERN = Arrays
            .stream(Token.TokenType.values())
            .map(Token.TokenType::getRegexp)
            .collect(Collectors.joining("|"));

    private final Reader reader;
    private final List<Message> messages;
    private final Matcher matcher;
    private int column;
    private int line;
    private int offset;
    private int lastColumn;
    private int lastLine;
    private int lastOffset;
    private int currentSpace;
    private Token lastToken;

    public Lexer(Reader reader) {
        this.reader = reader;
        column = 1;
        line = 1;
        offset = 0;
        lastColumn = 1;
        lastLine = 1;
        lastOffset = 0;
        currentSpace = 0;
        messages = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        char current = (char) -1;
        while ((current = (char) nextSymbol()) != (char) -1) {
            if(current != '\r') {
                builder.append(current);
            }
        }

        Pattern pattern = Pattern.compile(PATTERN);
        matcher = pattern.matcher(builder.toString());
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Token peekToken() {
        return lastToken;
    }

    public Token nextToken() {
        if (matcher.find()) {
            Token.TokenType tokenType = Arrays.stream(Token.TokenType.values())
                    .filter(t -> matcher.group(t.getGroupName()) != null)
                    .findFirst()
                    .orElse(Token.TokenType.EOF);

            if (tokenType == Token.TokenType.SPACE) {
                currentSpace++;

                if(currentSpace == 4) {
                    currentSpace = 0;
                    lastColumn = column;
                    lastLine = line;
                    lastOffset = offset;
                    lastToken = new Token("\t",
                            Token.TokenType.TAB,
                            new Position(lastColumn, lastLine, lastOffset),
                            new Position(column, line, offset));
                    return lastToken;
                }

                lastColumn = column;
                lastLine = line;
                lastOffset = offset;
                lastToken = nextToken();
                return lastToken;
            } else {
                currentSpace = 0;
            }

            if (tokenType == Token.TokenType.COMMENT) {
                String content = matcher.group(tokenType.getGroupName());
                updatePosition(content);
                lastColumn = column;
                lastLine = line;
                lastOffset = offset;
                return nextToken();
            }

            String content = matcher.group(tokenType.getGroupName());
            updatePosition(content);

            Token token = new Token(content,
                    tokenType,
                    new Position(lastColumn, lastLine, lastOffset),
                    new Position(column, line, offset));
            lastColumn = column;
            lastLine = line;
            lastOffset = offset;
            lastToken = token;
            return token;
        } else {
            return new Token("",
                    Token.TokenType.EOF,
                    new Position(lastColumn, lastLine, lastOffset),
                    new Position(lastColumn, lastLine, lastOffset));
        }
    }

    private void updatePosition(String content) {
        offset += content.length();

        if (content.contains("\n")) {
            column = 1;
            line++;
        } else {
            column += content.length();
        }
    }

    private int nextSymbol() {
        try {
            return reader.read();
        } catch (IOException ioException) {
            throw new IllegalArgumentException(ioException);
        }
    }
}
