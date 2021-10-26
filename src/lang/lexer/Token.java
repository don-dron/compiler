package lang.lexer;

import lang.Position;

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
         * this
         */
        THIS("this", "this"),

        /**
         * colon
         */
        COLON("colon", "\\:"),

        /**
         * space
         */
        SPACE("space", " "),

        /**
         * float constant
         */
        FLOAT_CONSTANT("fconst", "[0-9]+\\.[0-9]*"),

        /**
         * char constant
         */
        CHAR_CONSTANT("cconst", "[0-9]+c"),

        /**
         * int constant
         */
        INT_CONSTANT("iconst", "[0-9]+"),

        SYMBOL_CONSTANT("sconst", "'(.|[^'])+'"),

        STRING_CONSTANT("strconst","\"(.|[^\"])*?\""),


        /**
         * interface
         */
        INTERFACE("interface","interface"),

        /**
         * import
         */
        IMPORT("import","import"),

        /**
         * class
         */
        CLASS("class","class"),

        /**
         * void
         */
        VOID("void","void"),

        /**
         * arrow
         */
        ARROW("arrow","->"),

        /**
         * inc_add
         */
        INC_ADD("incadd","\\+\\+"),

        /**
         * inc_mul
         */
        INC_MUL("incmul","\\*\\*"),

        /**
         * dec
         */
        DEC("dec","\\-\\-"),

        /**
         * point
         */
        POINT("point","\\."),

        /**
         * int
         */
        INT("int","int"),

        /**
         * char
         */
        CHAR("char","char"),

        /**
         * float
         */
        FLOAT("float","float"),

        /**
         * for
         */
        FOR("for","for"),


        /**
         * While
         */
        WHILE("while","while"),

        /**
         * if
         */
        IF("if","if"),

        /**
         * elif
         */
        ELIF("elif","elif"),

        /**
         * else
         */
        ELSE("else","else"),

        /**
         * null
         */
        NULL("null","null"),

        /**
         * true
         */
        TRUE("true","true"),

        /**
         * false
         */
        FALSE("false","false"),

        /**
         * continue
         */
        CONTINUE("continue","continue"),

        /**
         * new
         */
        NEW("new","new"),

        /**
         * break
         */
        BREAK("break","break"),

        /**
         * return
         */
        RETURN("return","return"),

        /**
         * identifier
         */
        IDENTIFIER("identifier","[a-zA-Z_][_a-zA-Z0-9]*"),

        /**
         * ==
         */
        EQUAL("equal","=="),

        /**
         * !=
         */
        NOT_EQUAL("notequal","!="),

        /**
         * <=
         */
        LE("le","<="),

        /**
         * >=
         */
        GE("ge",">="),

        /**
         * <
         */
        LT("lt","<"),

        /**
         * >
         */
        GT("gt",">"),

        /**
         * =
         */
        DEFINE("define","="),

        /**
         * [
         */
        LB_PAREN("lbparen","\\["),

        /**
         * ]
         */
        RB_PAREN("rbparen","\\]"),

        /**
         * (
         */
        L_PAREN("lparen","\\("),

        /**
         * )
         */
        R_PAREN("rparen","\\)"),

        /**
         * {
         */
        LF_PAREN("lfparen","\\{"),

        /**
         * }
         */
        RF_PAREN("rfparen","\\}"),

        /**
         * *
         */
        ASTERISK("asterisk","\\*"),

        /**
         * ?
         */
        QUESTION("question","\\?"),

        /**
         * !
         */
        NOT("not","\\!"),

        /**
         * +
         */
        PLUS("plus","\\+"),

        /**
         * -
         */
        MINUS("minus","\\-"),

        /**
         * %
         */
        PERCENT("percent","\\%"),

        /**
         * ,
         */
        COMMA("comma","\\,"),

        /**
         * //
         */
        COMMENT("comment","//.*\n"),

        /**
         * '%s'
         */
        LITERAL("literal","'.*?'"),

        /**
         * /
         */
        SLASH("slash","\\/"),

        /**
         * ||
         */
        OR("or","\\|\\|"),

        /**
         * &&
         */
        AND("and","\\&\\&"),

        /**
         * EOF
         */
        EOF("eof","\\Z");


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
