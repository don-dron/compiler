package lang.parser;

import lang.ast.ArrayTypeNode;
import lang.ast.BasicTypeNode;
import lang.ast.ExpressionListNode;
import lang.ast.FileNode;
import lang.ast.FunctionNode;
import lang.ast.IdentifierNode;
import lang.ast.ImportNode;
import lang.ast.ObjectTypeNode;
import lang.ast.ParameterNode;
import lang.ast.ParametersNode;
import lang.ast.TranslationNode;
import lang.ast.TypeNode;
import lang.ast.expression.ArrayConstructorExpressionNode;
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
import lang.ast.expression.consts.NullConstantExpressionNode;
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
import lang.ast.statement.ConstructorDefinitionNode;
import lang.ast.statement.ContinueStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.ElifStatementNode;
import lang.ast.statement.ElseStatementNode;
import lang.ast.statement.EmptyStatementNode;
import lang.ast.statement.ExpressionStatementNode;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ast.statement.IfStatementNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.ReturnStatementNode;
import lang.ast.statement.StatementNode;
import lang.ast.statement.WhileStatementNode;
import lang.lexer.Lexer;
import lang.lexer.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import static lang.lexer.Token.TokenType.*;

public class Parser {
    private final Lexer lexer;
    private final Queue<Token> stack = new ArrayDeque<>();
    private final Stack<Token> tokens;
    private final String path;
    private final String pack;
    private Token lastToken = null;

    public Parser(Lexer lexer, String path, String pack) {
        this.lexer = lexer;
        this.tokens = new Stack<>();
        this.path = path;
        this.pack = pack;

        while (true) {
            Token token = lexer.nextToken();

            tokens.add(token);
            if (token.getTokenType() == Token.TokenType.EOF) {
                break;
            }
        }

        List<Token> rev = new ArrayList<>(tokens);
        Collections.reverse(rev);
        tokens.addAll(rev);
    }

    private Token next() {
        lastToken = tokens.pop();
        return lastToken;
    }

    private Token peek() {
        return tokens.peek();
    }

    private void ret(Token token) {
        tokens.push(token);
    }

    public FileNode parse() {
        List<ImportNode> importNodes = new ArrayList<>();
        List<StatementNode> statementNodes = new ArrayList<>();

        while (true) {
            if (peek().getTokenType() == EOF) {
                break;
            }

            if (peek().getTokenType() == IMPORT) {
                importNodes.add(parseImportNode());
            } else {
                statementNodes.add(parseGlobalStatement());
            }
        }

        return new FileNode(path, pack, importNodes, statementNodes);
    }

    private ImportNode parseImportNode() {
        next();

        ExpressionNode expressionNode = parseExpression();

        next();

        return new ImportNode(expressionNode);
    }

    // TRANSLATION ::= (STATEMENT | IMPORT)
    private TranslationNode parseTranslationNode() {
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
            return parseGlobalStatement();
        } else if (first.getTokenType() == Token.TokenType.CLASS) {
            return parseClassStatement();
        } else if (first.getTokenType() == Token.TokenType.INTERFACE) {
            return parseInterfaceStatement();
        } else if (first.getTokenType() == Token.TokenType.INT
                || first.getTokenType() == Token.TokenType.FLOAT
                || first.getTokenType() == Token.TokenType.L_PAREN) {
            return parseDeclarationStatement();
        } else {
            if (first.getTokenType() == Token.TokenType.IDENTIFIER) {
                next();
                Token token = peek();
                if (token.getTokenType() == Token.TokenType.IDENTIFIER) {
                    ret(first);
                    return parseDeclarationStatement();
                }
                ret(first);
            }
            throw new IllegalArgumentException();
        }
    }

    // ST ::= (STATEMENT | IMPORT)
    public StatementNode parseStatement() {
        Token first = peek();

        if (first.getTokenType() == Token.TokenType.NEWLINE) {
            next();
            return parseStatement();
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
            if (first.getTokenType() == Token.TokenType.IDENTIFIER) {
                next();
                Token token = peek();
                if (token.getTokenType() == Token.TokenType.IDENTIFIER) {
                    ret(first);
                    return parseDeclarationStatement();
                }
                ret(first);
            }
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
        ExpressionNode expressionNode = parseExpression();
        next();
        return new ExpressionStatementNode(expressionNode);
    }

    private ExpressionNode parseExpression() {
        ExpressionNode first = parseConditionalExpression();
        AssigmentExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.DEFINE) {
                next();
                if (current == null) {
                    current = new AssigmentExpressionNode(first, parseConditionalExpression());
                } else {
                    current = new AssigmentExpressionNode(current, parseConditionalExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
    }

    private StatementNode parseInterfaceStatement() {
        next();
        IdentifierNode identifierNode = parseIdentifier();

        List<IdentifierNode> identifierNodes = new ArrayList<>();
        if (peek().getTokenType() == COLON) {
            next();

            IdentifierNode node = parseIdentifier();
            identifierNodes.add(node);
            while (true) {
                if (peek().getTokenType() == COMMA) {
                    next();
                    node = parseIdentifier();
                    identifierNodes.add(node);
                } else {
                    break;
                }
            }
        }

        need(Token.TokenType.NEWLINE, peek());
        next();
        currentTab++;
        TranslationNode translationNode = parseTranslationNode();
        currentTab--;
        return new InterfaceStatementNode(identifierNode, identifierNodes, translationNode);
    }

    private StatementNode parseClassStatement() {
        next();
        IdentifierNode identifierNode = parseIdentifier();

        List<IdentifierNode> identifierNodes = new ArrayList<>();
        if (peek().getTokenType() == COLON) {
            next();

            IdentifierNode node = parseIdentifier();
            identifierNodes.add(node);
            while (true) {
                if (peek().getTokenType() == COMMA) {
                    next();
                    node = parseIdentifier();
                    identifierNodes.add(node);
                } else {
                    break;
                }
            }
        }

        need(Token.TokenType.NEWLINE, peek());
        next();
        currentTab++;
        TranslationNode translationNode = parseTranslationNode();
        currentTab--;
        return new ClassStatementNode(identifierNode, identifierNodes, translationNode);
    }

    private StatementNode parseDeclarationStatement() {
        TypeNode typeNode = parseType();

        if (typeNode instanceof FunctionNode && peek().getTokenType() == Token.TokenType.ARROW) {
            Token token = next();
            next();
            currentTab++;
            StatementNode statement = parseStatement();
            currentTab--;
            return new ConstructorDefinitionNode((FunctionNode) typeNode, statement);
        }

        IdentifierNode identifierNode = parseIdentifier();

        ExpressionNode expressionNode = null;
        if (typeNode instanceof FunctionNode) {
            if (peek().getTokenType() == Token.TokenType.ARROW) {
                next();
                Token token = peek();
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
            } else if (peek().getTokenType() == Token.TokenType.DEFINE) {
                next();
                expressionNode = parseExpression();
            } else {
                next();
                return new FunctionDefinitionNode((FunctionNode) typeNode, identifierNode, null);
            }
        } else if (peek().getTokenType() == Token.TokenType.DEFINE) {
            next();
            expressionNode = parseExpression();
        }

//        need(Token.TokenType.NEWLINE, peek());

        next();

        return new DeclarationStatementNode(typeNode, identifierNode, expressionNode);
    }

    private StatementNode parseIfStatement() {
        need(Token.TokenType.IF, peek());
        next();

        ExpressionNode expressionNode = parseConditionalExpression();

        need(Token.TokenType.NEWLINE, peek());
        next();

        currentTab++;
        StatementNode thenNode = parseStatement();
        currentTab--;

        return new IfStatementNode(expressionNode, thenNode);
    }

    private StatementNode parseElifStatement() {
        need(Token.TokenType.ELIF, peek());
        next();

        ExpressionNode expressionNode = parseConditionalExpression();

        need(Token.TokenType.NEWLINE, peek());
        next();

        currentTab++;
        StatementNode thenNode = parseStatement();
        currentTab--;

        return new ElifStatementNode(expressionNode, thenNode);
    }

    private StatementNode parseElseStatement() {
        need(Token.TokenType.ELSE, peek());
        next();

        need(Token.TokenType.NEWLINE, peek());
        next();

        currentTab++;
        StatementNode elseNode = parseStatement();
        currentTab--;
        return new ElseStatementNode(elseNode);
    }

    private StatementNode parseWhileStatement() {
        need(Token.TokenType.WHILE, peek());
        next();

        ExpressionNode predicate = parseConditionalExpression();

        need(Token.TokenType.NEWLINE, peek());
        next();

        currentTab++;
        StatementNode body = parseCompoundStatement();
        currentTab--;
        return new WhileStatementNode(predicate, body);
    }

    private StatementNode parseContinueStatement() {
        need(Token.TokenType.CONTINUE, peek());
        next();
        need(Token.TokenType.NEWLINE, peek());
        next();
        return new ContinueStatementNode();
    }

    private StatementNode parseReturnStatement() {
        need(Token.TokenType.RETURN, peek());
        next();

        ExpressionNode expressionNode = null;

        if (peek().getTokenType() != Token.TokenType.NEWLINE) {
            expressionNode = parseConditionalExpression();
        }

        need(Token.TokenType.NEWLINE, peek());
        next();

        return new ReturnStatementNode(expressionNode);
    }

    private StatementNode parseBreakStatement() {
        need(Token.TokenType.BREAK, peek());
        next();

        need(Token.TokenType.NEWLINE, peek());
        next();
        return new BreakStatementNode();
    }

    private ParametersNode parseParameterList() {
        List<ParameterNode> parameters = new ArrayList<>();

        while (true) {
            if (peek().getTokenType() == Token.TokenType.R_PAREN) {
                break;
            }
            TypeNode typeNode = parseType();

            if (typeNode == null) {
                break;
            }

            need(Token.TokenType.IDENTIFIER, peek());
            IdentifierNode identifierNode = parseIdentifier();

            parameters.add(new ParameterNode(typeNode, identifierNode));

            if (peek().getTokenType() != Token.TokenType.COMMA) {
                break;
            } else {
                next();
            }
        }

        return new ParametersNode(parameters);
    }

    // TYPE ::= INT | FLOAT | VOID
    private TypeNode parseType() {
        Token type = peek();

        if (type.getTokenType() == Token.TokenType.EOF) {
            return null;
        }

        TypeNode typeNode = null;
        if (type.getTokenType() == Token.TokenType.L_PAREN) {
            need(Token.TokenType.L_PAREN, peek());
            next();

            ParametersNode parametersNode = parseParameterList();

            need(Token.TokenType.R_PAREN, peek());
            next();

            if (peek().getTokenType() != Token.TokenType.ARROW) {
                typeNode = parseType();
            }
            typeNode = new FunctionNode(parametersNode, typeNode);
        } else if (type.getTokenType() == Token.TokenType.IDENTIFIER) {
            typeNode = new ObjectTypeNode(parseIdentifier());
        } else {
            typeNode = parseBasicType(type);
            if (typeNode == null) {
                return null;
            }
        }

        while (true) {
            if (peek().getTokenType() == Token.TokenType.LB_PAREN) {
                need(Token.TokenType.LB_PAREN, peek());
                next();

                need(Token.TokenType.RB_PAREN, peek());
                next();
                typeNode = new ArrayTypeNode(typeNode);
            } else {
                break;
            }
        }

        return typeNode;
    }

    private TypeNode parseBasicType(Token type) {
        TypeNode typeNode;
        next();

        switch (type.getTokenType()) {
            case VOID:
                typeNode = new BasicTypeNode(TypeNode.Type.VOID, type);
                break;
            case INT:
                typeNode = new BasicTypeNode(TypeNode.Type.INT, type);
                break;
            case FLOAT:
                typeNode = new BasicTypeNode(TypeNode.Type.FLOAT, type);
                break;
            default:
                typeNode = null;
        }

        return typeNode;
    }

    private IdentifierNode parseIdentifier() {
        Token identifier = peek();

        if (identifier.getTokenType() != Token.TokenType.IDENTIFIER) {
            return null;
        }
        next();
        return new IdentifierNode(identifier.getContent(), identifier);
    }

    // CONDITIONAL_EXPRESSION ::= LOGICAL_OR_EXPRESSION
    //                            | LOGICAL_OR_EXPRESSION ? CONDITIONAL_EXPRESSION : CONDITIONAL_EXPRESSION
    private ExpressionNode parseConditionalExpression() {
        ExpressionNode first = parseLogicalOrExpression();

        Token token = peek();

        if (token.getTokenType() == Token.TokenType.QUESTION) {
            next();
            ExpressionNode thenNode = parseConditionalExpression();
            next();
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
        if (peek().getTokenType() == Token.TokenType.LT) {
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
                VariableExpressionNode right = new VariableExpressionNode(parseIdentifier());
                expressionNode = new FieldAccessExpressionNode(expressionNode, right);
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
            IdentifierNode identifierNode = parseIdentifier();
            return new VariableExpressionNode(identifierNode);
        } else if (first.getTokenType() == Token.TokenType.FLOAT_CONSTANT) {
            next();
            return new FloatConstantExpressionNode(Float.parseFloat(first.getContent()));
        } else if (first.getTokenType() == Token.TokenType.INT_CONSTANT) {
            next();
            return new IntConstantExpressionNode(Integer.parseInt(first.getContent()));
        } else if (first.getTokenType() == Token.TokenType.NULL) {
            next();
            return new NullConstantExpressionNode();
        } else if (first.getTokenType() == Token.TokenType.TRUE) {
            next();
            return new BoolConstantExpressionNode(true, first);
        } else if (first.getTokenType() == Token.TokenType.FALSE) {
            next();
            return new BoolConstantExpressionNode(false, first);
        } else if (first.getTokenType() == Token.TokenType.L_PAREN) {
            next();
            ExpressionNode expressionNode = parseConditionalExpression();
            next();
            return expressionNode;
        } else {
            IdentifierNode identifierNode = null;
            Token token = peek();
            TypeNode typeNode = null;

            if (token.getTokenType() == NEW) {
                next();
                token = peek();
                if (token.getTokenType() == IDENTIFIER
                        || token.getTokenType() == INT
                        || token.getTokenType() == FLOAT) {
                    if (token.getTokenType() == IDENTIFIER) {
                        identifierNode = new IdentifierNode(token.getContent(), token);
                        typeNode = new ObjectTypeNode(identifierNode);
                        next();
                    } else {
                        typeNode = parseBasicType(token);
                    }
                } else {
                    throw new IllegalArgumentException("");
                }

                if (peek().getTokenType() == Token.TokenType.LB_PAREN) {
                    List<ExpressionNode> expressionNodes = new ArrayList<>();
                    while (true) {
                        if (peek().getTokenType() == Token.TokenType.LB_PAREN) {
                            next();
                            ExpressionNode expressionNode = parseExpression();
                            next();

                            expressionNodes.add(expressionNode);
                        } else {
                            break;
                        }
                    }

                    return new ArrayConstructorExpressionNode(typeNode, expressionNodes);
                } else if (peek().getTokenType() == L_PAREN) {

                }
            }
        }
        throw new IllegalArgumentException("");

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
