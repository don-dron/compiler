package lang.lexer;

public class Token {

    private final String content;
    private final TokenType tokenType;
    private final Position start;
    private final Position end;

    public Token(String content, TokenType tokenType, Position start, Position end) {
        this.content = content;
        this.tokenType = tokenType;
        this.start = start;
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return tokenType + " " + start + " - " + end + " : " + "'" + content + "'";
    }

    public enum TokenType {

        /**
         * newline
         */
        NEWLINE("newline", "\n"),

        /**
         * tab
         */
        TAB("tab", "\t"),

        /**
         * space
         */
        SPACE("space", " "),

        /**
         * float constant
         */
        FLOAT_CONSTANT("fconst", "[0-9]+\\.[0-9]*"),

        /**
         * int constant
         */
        INT_CONSTANT("iconst", "[0-9]+"),

        /**
         * void
         */
        VOID("void", "void"),

        /**
         * arrow
         */
        ARROW("arrow", "->"),

        /**
         * int
         */
        INT("int", "int"),

        /**
         * float
         */
        FLOAT("float", "float"),

        /**
         * for
         */
        FOR("for", "for"),


        /**
         * While
         */
        WHILE("while", "while"),

        /**
         * if
         */
        IF("if", "if"),

        /**
         * elif
         */
        ELIF("elif", "elif"),

        /**
         * else
         */
        ELSE("else", "else"),

        /**
         * true
         */
        TRUE("true", "true"),

        /**
         * false
         */
        FALSE("false", "false"),

        /**
         * continue
         */
        CONTINUE("continue", "continue"),

        /**
         * break
         */
        BREAK("break", "break"),

        /**
         * return
         */
        RETURN("return", "return"),

        /**
         * identifier
         */
        IDENTIFIER("identifier", "[a-zA-Z_][_a-zA-Z0-9]*"),

        /**
         * ==
         */
        EQUAL("equal", "=="),

        /**
         * !=
         */
        NOT_EQUAL("notequal", "!="),

        /**
         * <=
         */
        LE("le", "<="),

        /**
         * >=
         */
        GE("ge", ">="),

        /**
         * <
         */
        LT("lt", "<"),

        /**
         * >
         */
        GT("gt", ">"),

        /**
         * =
         */
        DEFINE("define", "="),

        /**
         * (
         */
        L_PAREN("lparen", "\\("),

        /**
         * )
         */
        R_PAREN("rparen", "\\)"),

        /**
         * {
         */
        LF_PAREN("lfparen", "\\{"),

        /**
         * }
         */
        RF_PAREN("rfparen", "\\}"),

        /**
         * *
         */
        ASTERISK("asterisk", "\\*"),

        /**
         * ?
         */
        QUESTION("question", "\\?"),

        /**
         * !
         */
        NOT("not", "\\!"),

        /**
         * +
         */
        PLUS("plus", "\\+"),

        /**
         * -
         */
        MINUS("minus", "\\-"),

        /**
         * %
         */
        PERCENT("percent", "\\%"),

        /**
         * ,
         */
        COMMA("comma", "\\,"),

        /**
         * //
         */
        COMMENT("comment", "//.*\n"),

        /**
         * '%s'
         */
        LITERAL("literal", "'.*?'"),

        /**
         * /
         */
        SLASH("slash", "\\/"),

        /**
         * ||
         */
        OR("or", "\\|\\|"),

        /**
         * &&
         */
        AND("and", "\\&\\&"),

        /**
         * EOF
         */
        EOF("eof", "\\Z");


        private final String groupName;
        private final String regexp;

        TokenType(String groupName, String regexp) {
            this.regexp = regexp;
            this.groupName = groupName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getRegexp() {
            return String.format("(?<%s>%s)", groupName, regexp);
        }
    }
}