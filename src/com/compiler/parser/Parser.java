package com.compiler.parser;

import com.compiler.ast.*;
import com.compiler.ast.expression.*;
import com.compiler.ast.statement.*;
import com.compiler.core.Type;
import com.compiler.lexer.Lexer;
import com.compiler.lexer.Token;

import java.util.*;

public class Parser {
    private final Lexer lexer;
    private Token current;
    private Stack<Token> stack = new Stack<>();
    private Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private Token next() {
        if (stack.isEmpty()) {
            peekToken = null;
            return lexer.nextToken();
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

    // FUNCTIONS ::= FUNCTION +
    public FunctionsNode parse() {
        List<FunctionNode> nodes = new ArrayList<>();
        next();
        while (peek().getTokenType() != Token.TokenType.EOF) {
            nodes.add(parseFunction());
        }
        return new FunctionsNode(nodes);
    }

    // FUNCTION ::= TYPE IDENTIFIER L_PAREN PARAMETER_LIST R_PAREN COMPOUND_STATEMENT
    private FunctionNode parseFunction() {
        TypeNode type = parseType();
        IdentifierNode identifierNode = parseIdentifier();

        Token lParen = peek();

        need(Token.TokenType.L_PAREN, lParen);

        if (lParen.getTokenType() != Token.TokenType.L_PAREN) {
            throwExpected(List.of(Token.TokenType.L_PAREN), lParen);
        }
        next();
        ParameterNode parameterNode = parseParameterList();

        Token rParen = peek();
        need(Token.TokenType.R_PAREN, rParen);

        if (rParen.getTokenType() != Token.TokenType.R_PAREN) {
            throwExpected(List.of(Token.TokenType.R_PAREN), rParen);
        }

        next();

        StatementNode statementNode = parseCompoundStatement();

        return new FunctionNode(type, identifierNode, parameterNode, statementNode);
    }

    // TYPE ::= INT | FLOAT | VOID
    private TypeNode parseType() {
        Token type = peek();

        if (type.getTokenType() == Token.TokenType.EOF) {
            return null;
        }

        if (type.getTokenType() != Token.TokenType.VOID &&
                type.getTokenType() != Token.TokenType.INT &&
                type.getTokenType() != Token.TokenType.FLOAT) {
            throwExpected(List.of(Token.TokenType.VOID, Token.TokenType.INT, Token.TokenType.FLOAT), type);
        }
        next();

        Type t = null;

        switch (type.getTokenType()) {
            case VOID:
                t = Type.VOID;
                break;
            case INT:
                t = Type.INT;
                break;
            case FLOAT:
            default:
                t = Type.FLOAT;
        }

        return new TypeNode(t);
    }

    private IdentifierNode parseIdentifier() {
        Token identifier = peek();

        if (identifier.getTokenType() != Token.TokenType.IDENTIFIER) {
            return null;
        }
        next();
        return new IdentifierNode(identifier.getContent());
    }

    // PARAMETER_LIST ::= (TYPE IDENTIFIER)+
    private ParameterNode parseParameterList() {
        Map<IdentifierNode, TypeNode> map = new HashMap<>();

        while (true) {
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

    // STATEMENT ::= COMPOUND_STATEMENT | EXPRESSION_STATEMENT | IF_STATEMENT | ITERATION_STATEMENT | JUMP_STATEMENT | DECLARATION_STATEMENT
    private StatementNode parseStatement() {
        Token first = peek();

        if (first.getTokenType() == Token.TokenType.SEMICOLON) {
            next();
            return new EmptyStatementNode();
        } else if (first.getTokenType() == Token.TokenType.IF) {
            return parseIfStatement();
        } else if (first.getTokenType() == Token.TokenType.LF_PAREN) {
            return parseCompoundStatement();
        } else if (first.getTokenType() == Token.TokenType.FOR) {
            return parseIterationStatement();
        } else if (first.getTokenType() == Token.TokenType.RETURN) {
            return parseReturnStatement();
        } else if (first.getTokenType() == Token.TokenType.BREAK) {
            return parseBreakStatement();
        } else if (first.getTokenType() == Token.TokenType.CONTINUE) {
            return parseContinueStatement();
        } else if (first.getTokenType() == Token.TokenType.INT || first.getTokenType() == Token.TokenType.FLOAT) {
            return parseDeclarationStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    // COMPOUND_STATEMENT ::= LF_PAREN STATEMENT* RF_PAREN
    private StatementNode parseCompoundStatement() {
        List<StatementNode> statementNodes = new ArrayList<>();
        Token lfParen = peek();
        need(Token.TokenType.LF_PAREN, lfParen);
        next();

        while (true) {
            Token token = peek();
            if (token.getTokenType() == Token.TokenType.RF_PAREN) {
                break;
            }

            statementNodes.add(parseStatement());
        }

        Token rfParen = peek();
        need(Token.TokenType.RF_PAREN, rfParen);
        next();
        return new CompoundStatementNode(statementNodes);
    }

    // EXPRESSION_STATEMENT ::= CONDITIONAL_EXPRESSION SEMICOLON | ASSIGMENT_EXPRESSION SEMICOLON
    private StatementNode parseExpressionStatement() {
        Token ident = peek();

        if (ident.getTokenType() == Token.TokenType.IDENTIFIER) {
            if (next().getTokenType() == Token.TokenType.DEFINE) {
                ret(ident);
                ExpressionNode expressionNode = parseAssigmentExpression();
                if (peek().getTokenType() != Token.TokenType.SEMICOLON) {
                    throw new IllegalStateException("Expected ;");
                } else {
                    next();
                }
                return new ExpressionStatementNode(expressionNode);
            } else {
                ret(ident);
            }
        }
        ExpressionNode expressionNode = parseConditionalExpression();

        if (peek().getTokenType() != Token.TokenType.SEMICOLON) {
            throw new IllegalStateException("Expected ;");
        } else {
            next();
        }

        return new ExpressionStatementNode(expressionNode);
    }

    // DECLARATION_STATEMENT ::= TYPE IDENTIFIER ( = CONDITIONAL_EXPRESSION ) ?  SEMICOLON
    private StatementNode parseDeclarationStatement() {
        TypeNode typeNode = parseType();
        IdentifierNode identifierNode = parseIdentifier();
        ExpressionNode expressionNode = null;
        if (peek().getTokenType() == Token.TokenType.DEFINE) {
            next();
            expressionNode = parseConditionalExpression();
        }

        Token semicolon = next();

        return new DeclarationStatementNode(typeNode, identifierNode, expressionNode);
    }

    // IF_STATEMENT ::= IF L_PAREN CONDITIONAL_EXPRESSION R_PAREN STATEMENT (ELSE STATEMENT)*
    private StatementNode parseIfStatement() {
        Token ifToken = peek();
        Token lParen = next();
        need(Token.TokenType.L_PAREN, lParen);
        next();

        ExpressionNode expressionNode = parseConditionalExpression();

        Token rParen = peek();
        need(Token.TokenType.R_PAREN, rParen);
        rParen = next();

        StatementNode thenNode = parseStatement();

        Token elseToken = peek();
        if (elseToken.getTokenType() == Token.TokenType.ELSE) {
            next();
            StatementNode elseNode = parseStatement();
            return new IfStatementNode(expressionNode, thenNode, elseNode);
        }
        return new IfStatementNode(expressionNode, thenNode, null);
    }

    // ITERATION_STATEMENT ::= FOR L_PAREN (DECLARATION_STATEMENT | SEMICOLON) CONDITIONAL_EXPRESSION SEMICOLON ASSIGMENT_EXPRESSION R_PAREN STATEMENT
    private StatementNode parseIterationStatement() {
        StatementNode prev = null;
        ExpressionNode predicate = null;
        ExpressionNode step = null;
        StatementNode body = null;
        Token forToken = peek();
        Token lParen = next();
        need(Token.TokenType.L_PAREN, lParen);
        next();
        Token semicolon = peek();
        if (semicolon.getTokenType() != Token.TokenType.SEMICOLON) {
            prev = parseDeclarationStatement();
        } else {
            next();
        }

        semicolon = peek();

        if (semicolon.getTokenType() != Token.TokenType.SEMICOLON) {
            predicate = parseConditionalExpression();
        }

        Token rParen = next();

        if (rParen.getTokenType() != Token.TokenType.R_PAREN) {
            step = parseAssigmentExpression();
        }

        next();

        body = parseStatement();
        return new ForStatementNode(prev, predicate, step, body);
    }

    // JUMP_STATEMENT ::= BREAK SEMICOLON | RETURN SEMICOLON | RETURN CONDITIONAL_EXPRESSION SEMICOLON
    private StatementNode parseBreakStatement() {
        Token breakToken = peek();
        next();
        Token semicolon = peek();
        next();
        return new BreakStatementNode();
    }

    // JUMP_STATEMENT ::= BREAK SEMICOLON | RETURN SEMICOLON | RETURN CONDITIONAL_EXPRESSION SEMICOLON
    private StatementNode parseReturnStatement() {
        Token returnToken = peek();
        next();
        Token semicolon = peek();
        ExpressionNode expressionNode = null;

        if (semicolon.getTokenType() != Token.TokenType.SEMICOLON) {
            expressionNode = parseConditionalExpression();
        }

        next();

        return new ReturnStatementNode(expressionNode);
    }

    // JUMP_STATEMENT ::= BREAK SEMICOLON | RETURN SEMICOLON | RETURN CONDITIONAL_EXPRESSION SEMICOLON
    private StatementNode parseContinueStatement() {
        Token continueToken = peek();
        next();
        Token semicolon = peek();
        next();
        return new ContinueStatementNode();
    }

    // ASSIGMENT_EXPRESSION ::= IDENTIFIER = CONDITIONAL_EXPRESSION
    private ExpressionNode parseAssigmentExpression() {
        IdentifierNode identifierNode = parseIdentifier();
        Token def = peek();
        next();
        ExpressionNode expressionNode = parseConditionalExpression();

        return new AssigmentExpressionNode(identifierNode, expressionNode);
    }

    // CONDITIONAL_EXPRESSION ::= LOGICAL_OR_EXPRESSION | LOGICAL_OR_EXPRESSION ? CONDITIONAL_EXPRESSION : CONDITIONAL_EXPRESSION
    private ExpressionNode parseConditionalExpression() {
        ExpressionNode first = parseLogicalOrExpression();

        Token token = peek();

        if (token.getTokenType() == Token.TokenType.QUESTION) {
            next();
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
        ExpressionNode first = parsePrimaryExpression();
        MultiplicativeExpressionNode current = null;

        while (true) {
            Token token = peek();

            if (token.getTokenType() == Token.TokenType.ASTERISK || token.getTokenType() == Token.TokenType.SLASH) {
                next();

                MultiplicativeExpressionNode.MultiplicativeType type = null;

                switch (token.getTokenType()) {
                    case ASTERISK:
                        type = MultiplicativeExpressionNode.MultiplicativeType.MUL;
                        break;
                    case SLASH:
                    default:
                        type = MultiplicativeExpressionNode.MultiplicativeType.DIV;
                }

                if (current == null) {
                    current = new MultiplicativeExpressionNode(type, first, parsePrimaryExpression());
                } else {
                    current = new MultiplicativeExpressionNode(type, current, parsePrimaryExpression());
                }
            } else {
                break;
            }
        }

        return current == null ? first : current;
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
        } else if (first.getTokenType() == Token.TokenType.L_PAREN) {
            next();
            ExpressionNode expressionNode = parseConditionalExpression();
            next();
            return expressionNode;
        }

        throw new IllegalStateException("Unknown primary expression");
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
