package lang.ir.translate;

import lang.ast.Program;
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
import lang.ast.statement.CompoundStatementNode;
import lang.ast.statement.ContinueStatementNode;
import lang.ast.statement.DeclarationStatementNode;
import lang.ast.statement.ExpressionStatementNode;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ast.statement.IfElseStatementNode;
import lang.ast.statement.ReturnStatementNode;
import lang.ast.statement.StatementNode;
import lang.ast.statement.WhileStatementNode;
import lang.ir.Function;
import lang.ir.Module;
import lang.semantic.Scope;

import java.util.stream.Collectors;

public class Translator {
    private final Program program;

    public Translator(Program program) {
        this.program = program;
    }

    public Module translate() {
        return new Module(program.getFunctions()
                .stream()
                .map(this::translateFunction)
                .collect(Collectors.toList()));
    }

    private Function translateFunction(FunctionDefinitionNode functionDefinitionNode) {

        return new Function();
    }

    private void translateStatement(StatementNode node) {
        if (node instanceof BreakStatementNode) {
            translateBreak((BreakStatementNode) node);
        } else if (node instanceof CompoundStatementNode) {
            translateCompound((CompoundStatementNode) node);
        } else if (node instanceof ContinueStatementNode) {
            translateContinue((ContinueStatementNode) node);
        } else if (node instanceof DeclarationStatementNode) {
            translateDeclaration((DeclarationStatementNode) node);
        } else if (node instanceof IfElseStatementNode) {
            translateIfElse((IfElseStatementNode) node);
        } else if (node instanceof ReturnStatementNode) {
            translateReturn((ReturnStatementNode) node);
        } else if (node instanceof WhileStatementNode) {
            translateWhile((WhileStatementNode) node);
        } else if (node instanceof ExpressionStatementNode) {
            translateExpressionStatement((ExpressionStatementNode) node);
        } else {
            throw new IllegalArgumentException("Undefined statement");
        }
    }

    private void translateBreak(BreakStatementNode node) {
    }

    private void translateCompound(CompoundStatementNode node) {

    }

    private void translateContinue(ContinueStatementNode node) {

    }

    private void translateIfElse(IfElseStatementNode node) {

    }

    private void translateExpressionStatement(ExpressionStatementNode node) {

    }

    private void translateDeclaration(DeclarationStatementNode node) {

    }

    private void translateReturn(ReturnStatementNode node) {

    }

    private void translateWhile(WhileStatementNode node) {

    }

    private void translateExpression(ExpressionNode expressionNode, Scope parentScope) {
        if (expressionNode instanceof AssigmentExpressionNode) {
            translateAssigmentExpression(expressionNode);
        } else if (expressionNode instanceof ConditionalExpressionNode) {
            translateConditionalExpression(expressionNode);
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            translateAdditionalExpression(expressionNode);
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            translateMultiplicativeExpression(expressionNode);
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            translateLogicalAndExpression((LogicalAndExpressionNode) expressionNode);
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            translateLogicalOrExpression((LogicalOrExpressionNode) expressionNode);
        } else if (expressionNode instanceof RelationalExpressionNode) {
            translateRelationalExpression((RelationalExpressionNode) expressionNode);
        } else if (expressionNode instanceof EqualityExpressionNode) {
            translateEqualityExpression((EqualityExpressionNode) expressionNode);
        } else if (expressionNode instanceof VariableExpressionNode) {
            translateVariableExpression((VariableExpressionNode) expressionNode);
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            translateBoolConstantExpression((BoolConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            translateIntConstantExpression((IntConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            translateFloatConstantExpression((FloatConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof NullConstantExpressionNode) {
            translateNullConstantExpression((NullConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof ArrayConstructorExpressionNode) {
            translateArrayConstructorExpression((ArrayConstructorExpressionNode) expressionNode);
        } else if (expressionNode instanceof FunctionCallExpressionNode) {
            translateFunctionCallExpression((FunctionCallExpressionNode) expressionNode);
        } else if (expressionNode instanceof ArrayAccessExpressionNode) {
            translateArrayAccessExpression(expressionNode);
        } else if (expressionNode instanceof FieldAccessExpressionNode) {
            translateFieldAccessExpression((FieldAccessExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixDecrementSubtractionExpressionNode) {
            translatePostfixDecrementExpression((PostfixDecrementSubtractionExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixIncrementAdditiveExpressionNode) {
            translatePostfixIncrementExpression((PostfixIncrementAdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixIncrementMultiplicativeExpressionNode) {
            translatePostfixMultiplicative((PostfixIncrementMultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixDecrementSubtractionExpressionNode) {
            translatePrefixDecrement((PrefixDecrementSubtractionExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixIncrementAdditiveExpressionNode) {
            translatePrefixIncrement((PrefixIncrementAdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixIncrementMultiplicativeExpressionNode) {
            translatePrefixMultiplicative((PrefixIncrementMultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof CastExpressionNode) {
            translateCastExpression((CastExpressionNode) expressionNode);
        }
    }

    private void translateCastExpression(CastExpressionNode expressionNode) {
    }

    private void translatePrefixMultiplicative(PrefixIncrementMultiplicativeExpressionNode expressionNode) {
    }

    private void translatePrefixIncrement(PrefixIncrementAdditiveExpressionNode expressionNode) {
    }

    private void translatePrefixDecrement(PrefixDecrementSubtractionExpressionNode expressionNode) {
    }

    private void translatePostfixMultiplicative(PostfixIncrementMultiplicativeExpressionNode expressionNode) {
    }

    private void translatePostfixIncrementExpression(PostfixIncrementAdditiveExpressionNode expressionNode) {
    }

    private void translatePostfixDecrementExpression(PostfixDecrementSubtractionExpressionNode expressionNode) {
    }

    private void translateFieldAccessExpression(FieldAccessExpressionNode expressionNode) {
    }

    private void translateArrayAccessExpression(ExpressionNode expressionNode) {
    }

    private void translateFunctionCallExpression(FunctionCallExpressionNode expressionNode) {
    }

    private void translateArrayConstructorExpression(ArrayConstructorExpressionNode expressionNode) {
    }

    private void translateNullConstantExpression(NullConstantExpressionNode expressionNode) {
    }

    private void translateFloatConstantExpression(FloatConstantExpressionNode expressionNode) {
    }

    private void translateIntConstantExpression(IntConstantExpressionNode expressionNode) {
    }

    private void translateBoolConstantExpression(BoolConstantExpressionNode expressionNode) {
    }

    private void translateVariableExpression(VariableExpressionNode expressionNode) {
    }

    private void translateEqualityExpression(EqualityExpressionNode expressionNode) {
    }

    private void translateRelationalExpression(RelationalExpressionNode expressionNode) {
    }

    private void translateLogicalOrExpression(LogicalOrExpressionNode expressionNode) {
    }

    private void translateLogicalAndExpression(LogicalAndExpressionNode expressionNode) {
    }

    private void translateMultiplicativeExpression(ExpressionNode expressionNode) {
    }

    private void translateAdditionalExpression(ExpressionNode expressionNode) {
    }

    private void translateConditionalExpression(ExpressionNode expressionNode) {
    }

    private void translateAssigmentExpression(ExpressionNode expressionNode) {
    }
}
