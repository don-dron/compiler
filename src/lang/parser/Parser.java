package lang.parser;

import lang.ast.BasicTypeNode;
import lang.ast.ExpressionListNode;
import lang.ast.FunctionDefinitionNode;
import lang.ast.FunctionNode;
import lang.ast.IdentifierNode;
import lang.ast.ParameterNode;
import lang.ast.TranslationNode;
import lang.ast.TypeNode;
import lang.ast.expression.ConditionalExpressionNode;
import lang.ast.expression.ExpressionNode;
import lang.ast.expression.VariableExpressionNode;
import lang.ast.expression.binary.AdditiveExpressionNode;
import lang.ast.expression.binary.AssigmentExpressionNode;
import lang.ast.expression.binary.EqualityExpressionNode;
import lang.ast.expression.binary.LogicalAndExpressionNode;
import lang.ast.expression.binary.LogicalOrExpressionNode;
import lang.ast.expression.binary.MultiplicativeExpressionNode;
import lang.ast.expression.binary.RelationalExpressionNode;
import lang.ast.expression.consts.BoolConstantExpressionNode;
import lang.ast.expression.consts.FloatConstantExpressionNode;
import lang.ast.expression.consts.IntConstantExpressionNode;
import lang.ast.expression.unary.postfix.ArrayAccessExpressionNode;
import lang.ast.expression.unary.postfix.FieldAccessExpressionNode;
import lang.ast.expression.unary.postfix.FunctionCallExpressionNode;
import lang.ast.expression.unary.postfix.PostfixDecrementSubtractionExpressionNode;
import lang.ast.expression.unary.postfix.PostfixIncrementAdditiveExpressionNode;
import lang.ast.expression.unary.postfix.PostfixIncrementMultiplicativeExpressionNode;
import lang.ast.expression.unary.prefix.CastExpressionNode;
import lang.ast.expression.unary.prefix.PrefixDecrementSubtractionExpressionNode;
import lang.ast.expression.unary.prefix.PrefixIncrementAdditiveExpressionNode;
import lang.ast.expression.unary.prefix.PrefixIncrementMultiplicativeExpressionNode;
import lang.ast.statement.BreakStatementNode;
import lang.ast.statement.ClassStatementNode;
import lang.ast.statement.CompoundStatementNode;
import lang.ast.statement.ContinueStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.ElifStatementNode;
import lang.ast.statement.ElseStatementNode;
import lang.ast.statement.EmptyStatementNode;
import lang.ast.statement.ExpressionStatementNode;
import lang.ast.statement.IfStatementNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.ReturnStatementNode;
import lang.ast.statement.StatementNode;
import lang.ast.statement.WhileStatementNode;
import lang.lexer.Lexer;
import lang.lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Parser {
    private final Lexer lexer;
    private final Stack<Token> stack = new Stack<>();

    public Parser(Lexer lexer, String moduleName) {
        this.lexer = lexer;
        next();
    }

    private Token next() {
        if (stack.isEmpty()) {
            Token token = lexer.nextToken();
            System.out.println(token.getTokenType());
            return token;
        } else {
            return stack.pop();
        }
    }

    private Token peek() {
        if (stack.isEmpty()) {
            return lexer.peekToken();
        } else {
            return stack.peek();
        }
    }

    private void ret(Token token) {
        stack.push(token);
    }

    // TRANSLATION ::= (STATEMENT | IMPORT)
    public TranslationNode parse() {
        List<StatementNode> statementNodes = new ArrayList<>();

        while (true) {
            int count = currentTab;
            Stack<Token> tokens = new Stack<>();

            if (peek().getTokenType() == Token.TokenType.NEWLINE) {
                next();
                statementNodes.add(new EmptyStatementNode());
            } else {
                while (count > 0) {
                    Token token = peek();

                    if (token.getTokenType() != Token.TokenType.TAB) {
                        break;
                    }
                    tokens.push(token);
                    token = next();
                    count--;
                }

                if (count > 0) {
                    while (!tokens.isEmpty()) {
                        ret(tokens.pop());
                    }
                    break;
                } else {
                    tokens.clear();
                }

                if (peek().getTokenType() == Token.TokenType.EOF) {
                    break;
                }

                statementNodes.add(parseGlobalStatement());
            }
        }

        return new TranslationNode(statementNodes);
    }

    // ST ::= (STATEMENT | IMPORT)
    public StatementNode parseGlobalStatement() {
        Token first = peek();
        if (first.getTokenType() == Token.TokenType.NEWLINE) {
            next();
            return new EmptyStatementNode();
        } else if (first.getTokenType() == Token.TokenType.CLASS) {
            return parseClassStatement();
        } else if (first.getTokenType() == Token.TokenType.INTERFACE) {
            return parseInterfaceStatement();
        } else if (first.getTokenType() == Token.TokenType.INT
                || first.getTokenType() == Token.TokenType.FLOAT
                || first.getTokenType() == Token.TokenType.L_PAREN) {
            return parseDeclarationStatement();
        } else {
            throw new IllegalArgumentException("Error");
        }
    }

    // ST ::= (STATEMENT | IMPORT)
    public StatementNode parseStatement() {
        Token first = peek();

        if (first.getTokenType() == Token.TokenType.NEWLINE) {
            next();
            return new EmptyStatementNode();
        } else if (first.getTokenType() == Token.TokenType.IF) {
            return parseIfStatement();
        } else if (first.getTokenType() == Token.TokenType.ELSE) {
            return parseElseStatement();
        } else if (first.getTokenType() == Token.TokenType.ELIF) {
            return parseElifStatement();
        } else if (first.getTokenType() == Token.TokenType.TAB) {
            return parseCompoundStatement();
        } else if (first.getTokenType() == Token.TokenType.WHILE) {
            return parseWhileStatement();
        } else if (first.getTokenType() == Token.TokenType.RETURN) {
            return parseReturnStatement();
        } else if (first.getTokenType() == Token.TokenType.BREAK) {
            return parseBreakStatement();
        } else if (first.getTokenType() == Token.TokenType.CLASS) {
            return parseClassStatement();
        } else if (first.getTokenType() == Token.TokenType.INTERFACE) {
            return parseInterfaceStatement();
        } else if (first.getTokenType() == Token.TokenType.CONTINUE) {
            return parseContinueStatement();
        } else if (first.getTokenType() == Token.TokenType.INT
                || first.getTokenType() == Token.TokenType.FLOAT
                || first.getTokenType() == Token.TokenType.L_PAREN) {
            return parseDeclarationStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    int currentTab = 0;

    // COMPOUND_STATEMENT ::= LF_PAREN STATEMENT* RF_PAREN
    private StatementNode parseCompoundStatement() {
        List<StatementNode> statementNodes = new ArrayList<>();
        while (true) {
            int count = currentTab;
            Stack<Token> tokens = new Stack<>();

            if (peek().getTokenType() == Token.TokenType.NEWLINE) {
                next();
                statementNodes.add(new EmptyStatementNode());
            } else {
                while (count > 0) {
                    Token token = peek();

                    if (token.getTokenType() != Token.TokenType.TAB) {
                        break;
                    }
                    tokens.push(token);
                    token = next();
                    count--;
                }

                if (count > 0) {
                    while (!tokens.isEmpty()) {
                        ret(tokens.pop());
                    }
                    break;
                } else {
                    tokens.clear();
                }

                if (peek().getTokenType() == Token.TokenType.EOF) {
                    break;
                }

                statementNodes.add(parseStatement());
            }
        }

        return new CompoundStatementNode(statementNodes);
    }

    // EXPRESSION_STATEMENT ::= CONDITIONAL_EXPRESSION SEMICOLON | ASSIGMENT_EXPRESSION SEMICOLON
    private StatementNode parseExpressionStatement() {
        Token ident = peek();

        if (ident.getTokenType() == Token.TokenType.IDENTIFIER) {
            if (next().getTokenType() == Token.TokenType.DEFINE) {
                ret(ident);
                ExpressionNode expressionNode = parseAssigmentExpression();
                next();
                return new ExpressionStatementNode(expressionNode);
            } else {
                ret(ident);
            }
        }
        ExpressionNode expressionNode = parseConditionalExpression();

        next();
        return new ExpressionStatementNode(expressionNode);
    }

    private ExpressionNode parseAssigmentExpression() {
        IdentifierNode identifierNode = parseIdentifier();
        Token def = peek();
        next();
        ExpressionNode expressionNode = parseConditionalExpression();

        return new AssigmentExpressionNode(identifierNode, expressionNode);
    }

    private StatementNode parseInterfaceStatement() {
        next();
        IdentifierNode identifierNode = parseIdentifier();
        next();
        currentTab++;
        TranslationNode translationNode = parse();
        currentTab--;
        return new InterfaceStatementNode(identifierNode, translationNode);
    }

    private StatementNode parseClassStatement() {
        next();
        IdentifierNode identifierNode = parseIdentifier();
        next();
        currentTab++;
        TranslationNode translationNode = parse();
        currentTab--;
        return new ClassStatementNode(identifierNode, translationNode);
    }

    private StatementNode parseDeclarationStatement() {
        TypeNode typeNode = parseType();
        IdentifierNode identifierNode = parseIdentifier();

        ExpressionNode expressionNode = null;
        if (typeNode instanceof FunctionNode) {
            if (peek().getTokenType() == Token.TokenType.ARROW) {
                Token token = next();
                if (token.getTokenType() == Token.TokenType.NEWLINE) {
                    next();
                    currentTab++;
                    StatementNode statement = parseStatement();
                    currentTab--;
                    return new FunctionDefinitionNode((FunctionNode) typeNode, identifierNode, statement);
                } else {
                    StatementNode statement = parseStatement();
                    return new FunctionDefinitionNode((FunctionNode) typeNode, identifierNode, statement);
                }
            } else {
                next();
                return new FunctionDefinitionNode((FunctionNode) typeNode, identifierNode, null);
            }
        } else if (peek().getTokenType() == Token.TokenType.DEFINE) {
            next();
            expressionNode = parseConditionalExpression();
        }

        Token semicolon = next();

        return new DeclarationStatementNode(typeNode, identifierNode, expressionNode);
    }

    private StatementNode parseIfStatement() {
        Token ifToken = peek();
        Token lParen = next();

        ExpressionNode expressionNode = parseConditionalExpression();

        Token rParen = peek();
        rParen = next();

        currentTab++;
        StatementNode thenNode = parseStatement();
        currentTab--;

        return new IfStatementNode(expressionNode, thenNode);
    }

    private StatementNode parseElifStatement() {
        Token ifToken = peek();
        Token lParen = next();

        ExpressionNode expressionNode = parseConditionalExpression();

        Token rParen = peek();
        rParen = next();

        currentTab++;
        StatementNode thenNode = parseStatement();
        currentTab--;

        return new ElifStatementNode(expressionNode, thenNode);
    }

    private StatementNode parseElseStatement() {
        next();
        Token rParen = next();
        currentTab++;
        StatementNode elseNode = parseStatement();
        currentTab--;
        return new ElseStatementNode(elseNode);
    }

    private StatementNode parseWhileStatement() {
        ExpressionNode predicate = null;
        StatementNode body = null;
        Token forToken = peek();
        Token lParen = next();

        predicate = parseConditionalExpression();

        Token rParen = peek();
        next();

        currentTab++;
        body = parseStatement();
        currentTab--;
        return new WhileStatementNode(predicate, body);
    }

    private StatementNode parseContinueStatement() {
        Token continueToken = peek();
        next();
        Token semicolon = peek();
        next();
        return new ContinueStatementNode();
    }

    private StatementNode parseReturnStatement() {
        Token returnToken = peek();
        next();
        Token semicolon = peek();
        ExpressionNode expressionNode = null;

        if (semicolon.getTokenType() != Token.TokenType.NEWLINE) {
            expressionNode = parseConditionalExpression();
        }

        next();

        return new ReturnStatementNode(expressionNode);
    }

    private StatementNode parseBreakStatement() {
        Token breakToken = peek();
        next();
        Token semicolon = peek();
        next();
        return new BreakStatementNode();
    }

    private ParameterNode parseParameterList() {
        Map<IdentifierNode, TypeNode> map = new HashMap<>();

        while (true) {
            if (peek().getTokenType() == Token.TokenType.R_PAREN) {
                break;
            }
            TypeNode typeNode = parseType();

            if (typeNode == null) {
                break;
            }

            IdentifierNode identifierNode = parseIdentifier();

            if (identifierNode == null) {
                throw new IllegalStateException("Need identifier");
            }

            map.put(identifierNode, typeNode);

            if (peek().getTokenType() != Token.TokenType.COMMA) {
                break;
            } else {
                next();
            }
        }

        return new ParameterNode(map);
    }

    // TYPE ::= INT | FLOAT | VOID
    private TypeNode parseType() {
        Token type = peek();

        if (type.getTokenType() == Token.TokenType.EOF) {
            return null;
        }

        if (type.getTokenType() == Token.TokenType.L_PAREN) {
            next();
            ParameterNode parameterNode = parseParameterList();
            next();
            TypeNode typeNode = parseType();

            return new FunctionNode(parameterNode, typeNode);
        }

        if (type.getTokenType() != Token.TokenType.VOID &&
                type.getTokenType() != Token.TokenType.INT &&
                type.getTokenType() != Token.TokenType.FLOAT) {
            throwExpected(List.of(Token.TokenType.VOID, Token.TokenType.INT, Token.TokenType.FLOAT), type);
        }
        next();

        TypeNode.Type t = null;

        switch (type.getTokenType()) {
            case VOID:
                t = TypeNode.Type.VOID;
                break;
            case INT:
                t = TypeNode.Type.INT;
                break;
            case FLOAT:
            default:
                t = TypeNode.Type.FLOAT;
        }

        return new BasicTypeNode(t);
    }

    private IdentifierNode parseIdentifier() {
        Token identifier = peek();

        if (identifier.getTokenType() != Token.TokenType.IDENTIFIER) {
            return null;
        }
        next();
        return new IdentifierNode(identifier.getContent());
    }

    // CONDITIONAL_EXPRESSION ::= LOGICAL_OR_EXPRESSION
    //                            | LOGICAL_OR_EXPRESSION ? CONDITIONAL_EXPRESSION : CONDITIONAL_EXPRESSION
    private ExpressionNode parseConditionalExpression() {
        ExpressionNode first = parseLogicalOrExpression();

        Token token = peek();

        if (token.getTokenType() == Token.TokenType.QUESTION) {
            token = next();
            ExpressionNode thenNode = parseConditionalExpression();
            ExpressionNode elseNode = parseConditionalExpression();
            return new ConditionalExpressionNode(first, thenNode, elseNode);
        }

        return first;
    }

    // LOGICAL_OR_EXPRESSION ::= LOGICAL_AND_EXPRESSION | LOGICAL_OR_EXPRESSION OR LOGICAL_AND_EXPRESSION
    private ExpressionNode parseLogicalOrExpression() {
        ExpressionNode first = parseLogicalAndExpression();
        LogicalOrExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.OR) {
                next();
                if (current == null) {
                    current = new LogicalOrExpressionNode(first, parseLogicalAndExpression());
                } else {
                    current = new LogicalOrExpressionNode(current, parseLogicalAndExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    // LOGICAL_AND_EXPRESSION ::= EQUALITY_EXPRESSION | LOGICAL_AND_EXPRESSION AND EQUALITY_EXPRESSION
    private ExpressionNode parseLogicalAndExpression() {
        ExpressionNode first = parseEqualityExpression();
        LogicalAndExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.AND) {
                next();
                if (current == null) {
                    current = new LogicalAndExpressionNode(first, parseEqualityExpression());
                } else {
                    current = new LogicalAndExpressionNode(current, parseEqualityExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    // EQUALITY_EXPRESSION ::= RELATIONAL_EXPRESSION | EQUALITY_EXPRESSION == RELATIONAL_EXPRESSION | EQUALITY_EXPRESSION != RELATIONAL_EXPRESSION
    private ExpressionNode parseEqualityExpression() {
        ExpressionNode first = parseRelationalExpression();
        EqualityExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.EQUAL || token.getTokenType() == Token.TokenType.NOT_EQUAL) {
                next();

                EqualityExpressionNode.EqualityType type = null;

                switch (token.getTokenType()) {
                    case EQUAL:
                        type = EqualityExpressionNode.EqualityType.EQ;
                        break;
                    case NOT_EQUAL:
                    default:
                        type = EqualityExpressionNode.EqualityType.NE;
                }

                if (current == null) {
                    current = new EqualityExpressionNode(type, first, parseRelationalExpression());
                } else {
                    current = new EqualityExpressionNode(type, current, parseRelationalExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    // RELATIONAL_EXPRESSION ::= ADDITIVE_EXPRESSION
    // | RELATIONAL_EXPRESSION <= ADDITIVE_EXPRESSION
    // | RELATIONAL_EXPRESSION >= ADDITIVE_EXPRESSION
    // | RELATIONAL_EXPRESSION < ADDITIVE_EXPRESSION
    // | RELATIONAL_EXPRESSION > ADDITIVE_EXPRESSION
    private ExpressionNode parseRelationalExpression() {
        ExpressionNode first = parseAdditiveExpression();
        RelationalExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.GE || token.getTokenType() == Token.TokenType.GT
                    || token.getTokenType() == Token.TokenType.LE || token.getTokenType() == Token.TokenType.LT) {
                next();

                RelationalExpressionNode.RelationalType type = null;

                switch (token.getTokenType()) {
                    case GE:
                        type = RelationalExpressionNode.RelationalType.GE;
                        break;
                    case LE:
                        type = RelationalExpressionNode.RelationalType.LE;
                        break;
                    case LT:
                        type = RelationalExpressionNode.RelationalType.LT;
                        break;
                    case GT:
                    default:
                        type = RelationalExpressionNode.RelationalType.GT;
                        break;
                }

                if (current == null) {
                    current = new RelationalExpressionNode(type, first, parseAdditiveExpression());
                } else {
                    current = new RelationalExpressionNode(type, current, parseAdditiveExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    // ADDITIVE_EXPRESSION ::= MULTIPLICATIVE_EXPRESSION | ADDITIVE_EXPRESSION + MULTIPLICATIVE_EXPRESSION | ADDITIVE_EXPRESSION - MULTIPLICATIVE_EXPRESSION
    private ExpressionNode parseAdditiveExpression() {
        ExpressionNode first = parseMultiplicativeExpression();
        AdditiveExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.PLUS || token.getTokenType() == Token.TokenType.MINUS) {
                next();

                AdditiveExpressionNode.AdditiveType type = null;

                switch (token.getTokenType()) {
                    case PLUS:
                        type = AdditiveExpressionNode.AdditiveType.ADD;
                        break;
                    case MINUS:
                    default:
                        type = AdditiveExpressionNode.AdditiveType.SUB;
                }

                if (current == null) {
                    current = new AdditiveExpressionNode(type, first, parseMultiplicativeExpression());
                } else {
                    current = new AdditiveExpressionNode(type, current, parseMultiplicativeExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    // MULTIPLICATIVE_EXPRESSION ::= PRIMARY_EXPRESSION | MULTIPLICATIVE_EXPRESSION * PRIMARY_EXPRESSION | MULTIPLICATIVE_EXPRESSION / PRIMARY_EXPRESSION
    private ExpressionNode parseMultiplicativeExpression() {
        ExpressionNode first = parseCastExpression();
        MultiplicativeExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.ASTERISK || token.getTokenType() == Token.TokenType.SLASH
                    || token.getTokenType() == Token.TokenType.PERCENT) {
                next();

                MultiplicativeExpressionNode.MultiplicativeType type = null;

                switch (token.getTokenType()) {
                    case ASTERISK:
                        type = MultiplicativeExpressionNode.MultiplicativeType.MUL;
                        break;
                    case SLASH:
                        type = MultiplicativeExpressionNode.MultiplicativeType.DIV;
                        break;
                    case PERCENT:
                    default:
                        type = MultiplicativeExpressionNode.MultiplicativeType.MOD;
                }

                if (current == null) {
                    current = new MultiplicativeExpressionNode(type, first, parseCastExpression());
                } else {
                    current = new MultiplicativeExpressionNode(type, current, parseCastExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    private ExpressionListNode parseExpressionListNode() {
        List<ExpressionNode> list = new ArrayList<>();

        while (true) {
            if (peek().getTokenType() == Token.TokenType.R_PAREN) {
                break;
            }
            ExpressionNode expressionNode = parseConditionalExpression();

            if (expressionNode == null) {
                throw new IllegalStateException("Need expression");
            }

            list.add(expressionNode);

            if (peek().getTokenType() != Token.TokenType.COMMA) {
                break;
            } else {
                next();
            }
        }

        return new ExpressionListNode(list);
    }

    private ExpressionNode parseCastExpression() {
        if (peek().getTokenType() == Token.TokenType.L_PAREN) {
            next();
            TypeNode typeNode = parseType();
            next();
            ExpressionNode expressionNode = parseCastExpression();
            return new CastExpressionNode(typeNode, expressionNode);
        } else {
            return parseUnaryExpression();
        }
    }

    private ExpressionNode parseUnaryExpression() {
        Token first = peek();
        if (first.getTokenType() == Token.TokenType.NEWLINE) {
            Token second = next();
            return parseUnaryExpression();
        }
        ExpressionNode expressionNode = null;

        if (first.getTokenType() == Token.TokenType.INC_ADD) {
            next();
            expressionNode = new PrefixIncrementAdditiveExpressionNode(parseUnaryExpression());
        } else if (first.getTokenType() == Token.TokenType.INC_MUL) {
            next();
            expressionNode = new PrefixIncrementMultiplicativeExpressionNode(parseUnaryExpression());
        } else if (first.getTokenType() == Token.TokenType.DEC) {
            next();
            expressionNode = new PrefixDecrementSubtractionExpressionNode(parseUnaryExpression());
        } else {
            expressionNode = parsePostExpression();
        }
        return expressionNode;
    }

    private ExpressionNode parsePostExpression() {
        ExpressionNode expressionNode = parsePrimaryExpression();

        while (true) {
            if (peek().getTokenType() == Token.TokenType.INC_ADD) {
                next();
                expressionNode = new PostfixIncrementAdditiveExpressionNode(expressionNode);
            } else if (peek().getTokenType() == Token.TokenType.INC_MUL) {
                next();
                expressionNode = new PostfixIncrementMultiplicativeExpressionNode(expressionNode);
            } else if (peek().getTokenType() == Token.TokenType.DEC) {
                next();
                expressionNode = new PostfixDecrementSubtractionExpressionNode(expressionNode);
            } else if (peek().getTokenType() == Token.TokenType.POINT) {
                next();
                ExpressionNode nextNode = new VariableExpressionNode(parseIdentifier());
                expressionNode = new FieldAccessExpressionNode(expressionNode, nextNode);
            } else if (peek().getTokenType() == Token.TokenType.L_PAREN) {
                next();
                if (peek().getTokenType() != Token.TokenType.R_PAREN) {
                    ExpressionListNode expressionListNode = parseExpressionListNode();
                    next();
                    expressionNode = new FunctionCallExpressionNode(expressionNode, expressionListNode);
                } else {
                    next();
                    expressionNode = new FunctionCallExpressionNode(expressionNode, new ExpressionListNode(List.of()));
                }
            } else if (peek().getTokenType() == Token.TokenType.LB_PAREN) {
                next();
                ExpressionNode argument = parseConditionalExpression();
                next();
                expressionNode = new ArrayAccessExpressionNode(expressionNode, argument);
            } else {
                break;
            }
        }

        return expressionNode;
    }

    // PRIMARY_EXPRESSION ::= IDENTIFIER | CONSTANT | L_PAREN CONDITIONAL_EXPRESSION R_PAREN
    private ExpressionNode parsePrimaryExpression() {
        Token first = peek();

        if (first.getTokenType() == Token.TokenType.IDENTIFIER) {
            next();
            return new VariableExpressionNode(new IdentifierNode(first.getContent()));
        } else if (first.getTokenType() == Token.TokenType.FLOAT_CONSTANT) {
            next();
            return new FloatConstantExpressionNode(Float.parseFloat(first.getContent()));
        } else if (first.getTokenType() == Token.TokenType.INT_CONSTANT) {
            next();
            return new IntConstantExpressionNode(Integer.parseInt(first.getContent()));
        } else if (first.getTokenType() == Token.TokenType.TRUE) {
            next();
            return new BoolConstantExpressionNode(true);
        } else if (first.getTokenType() == Token.TokenType.FALSE) {
            next();
            return new BoolConstantExpressionNode(false);
        } else if (first.getTokenType() == Token.TokenType.L_PAREN) {
            next();
            ExpressionNode expressionNode = parseConditionalExpression();
            next();
            return expressionNode;
        }

        throw new IllegalStateException("Unknown primary expression " + first);
    }

    private void need(Token.TokenType tokenType, Token current) {
        throwExpected(List.of(tokenType), current);
    }

    private void throwExpected(List<Token.TokenType> expected, Token result) {
        if (!expected.contains(result.getTokenType())) {
            throw new IllegalStateException("Expected " + expected.toString()
                    + " but current " + result);
        }
    }
}
