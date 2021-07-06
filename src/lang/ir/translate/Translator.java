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
import lang.ast.statement.ElifStatementNode;
import lang.ast.statement.ElseStatementNode;
import lang.ast.statement.ExpressionStatementNode;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ast.statement.IfElseStatementNode;
import lang.ast.statement.IfStatementNode;
import lang.ast.statement.ReturnStatementNode;
import lang.ast.statement.StatementNode;
import lang.ast.statement.WhileStatementNode;
import lang.ir.BasicBlock;
import lang.ir.Branch;
import lang.ir.Command;
import lang.ir.ConditionalBranch;
import lang.ir.Function;
import lang.ir.Module;

import java.util.ArrayList;
import java.util.List;
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
        Function function = new Function();

        BasicBlock header = function.appendBlock("header");

        BasicBlock returnBlock = function.appendBlock("return");

        BasicBlock entry = function.appendBlock("entry");
        header.setTerminator(new Branch(entry));

        translateStatement(function, functionDefinitionNode.getStatementNode());

        function.getCurrentBlock().setTerminator(new Branch(returnBlock));

        System.out.println(graphVizDebug(function));
        return function;
    }

    public static String graphVizDebug(Function functionBlock) {
        StringBuilder s = new StringBuilder("digraph G {\n");

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            String body = basicBlock.getName();

            s.append("\t\"")
                    .append(basicBlock.getName())
                    .append("\"")
                    .append(" ")
                    .append("[")
                    .append("style=filled, shape=box, label=\"")
                    .append(body)
                    .append("\"];\n");
        }

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            for (BasicBlock other : basicBlock.getOutput()) {
                s.append("\"")
                        .append(basicBlock.getName())
                        .append("\"")
                        .append(" -> ")
                        .append("\"")
                        .append(other.getName())
                        .append("\";\n");
            }
        }
        s.append("}");

        return s.toString();
    }

    private void createBranch(BasicBlock source,
                              BasicBlock target) {
        source.addOutput(target);
        target.addInput(source);
        source.setTerminator(new Branch(target));
    }

    private void createConditionalBranch(BasicBlock source,
                                         Command command,
                                         BasicBlock left,
                                         BasicBlock right) {
        source.addOutput(left);
        source.addOutput(right);
        left.addInput(source);
        right.addInput(source);
        source.setTerminator(new ConditionalBranch(command, left, right));
    }

    private void translateStatement(Function function, StatementNode node) {
        if (node instanceof BreakStatementNode) {
            translateBreak(function, (BreakStatementNode) node);
        } else if (node instanceof CompoundStatementNode) {
            translateCompound(function, (CompoundStatementNode) node);
        } else if (node instanceof ContinueStatementNode) {
            translateContinue(function, (ContinueStatementNode) node);
        } else if (node instanceof DeclarationStatementNode) {
            translateDeclaration(function, (DeclarationStatementNode) node);
        } else if (node instanceof IfElseStatementNode) {
            translateIfElse(function, (IfElseStatementNode) node);
        } else if (node instanceof IfStatementNode) {
            translateIf(function, (IfStatementNode) node);
        } else if (node instanceof ElifStatementNode) {
            translateElif(function, (ElifStatementNode) node);
        } else if (node instanceof ElseStatementNode) {
            translateElse(function, (ElseStatementNode) node);
        } else if (node instanceof ReturnStatementNode) {
            translateReturn(function, (ReturnStatementNode) node);
        } else if (node instanceof WhileStatementNode) {
            translateWhile(function, (WhileStatementNode) node);
        } else if (node instanceof ExpressionStatementNode) {
            translateExpressionStatement(function, (ExpressionStatementNode) node);
        } else {
            throw new IllegalArgumentException("Undefined statement");
        }
    }

    private Command translateExpression(Function function, ExpressionNode expressionNode) {
        if (expressionNode instanceof AssigmentExpressionNode) {
            return translateAssigmentExpression(function, (AssigmentExpressionNode) expressionNode);
        } else if (expressionNode instanceof ConditionalExpressionNode) {
            return translateConditionalExpression(function, (ConditionalExpressionNode) expressionNode);
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            return translateAdditionalExpression(function, (AdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            return translateMultiplicativeExpression(function, (MultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            return translateLogicalAndExpression(function, (LogicalAndExpressionNode) expressionNode);
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            return translateLogicalOrExpression(function, (LogicalOrExpressionNode) expressionNode);
        } else if (expressionNode instanceof RelationalExpressionNode) {
            return translateRelationalExpression(function, (RelationalExpressionNode) expressionNode);
        } else if (expressionNode instanceof EqualityExpressionNode) {
            return translateEqualityExpression(function, (EqualityExpressionNode) expressionNode);
        } else if (expressionNode instanceof VariableExpressionNode) {
            return translateVariableExpression(function, (VariableExpressionNode) expressionNode);
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            return translateBoolConstantExpression(function, (BoolConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            return translateIntConstantExpression(function, (IntConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            return translateFloatConstantExpression(function, (FloatConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof NullConstantExpressionNode) {
            return translateNullConstantExpression(function, (NullConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof ArrayConstructorExpressionNode) {
            return translateArrayConstructorExpression(function, (ArrayConstructorExpressionNode) expressionNode);
        } else if (expressionNode instanceof FunctionCallExpressionNode) {
            return translateFunctionCallExpression(function, (FunctionCallExpressionNode) expressionNode);
        } else if (expressionNode instanceof ArrayAccessExpressionNode) {
            return translateArrayAccessExpression(function, (ArrayAccessExpressionNode) expressionNode);
        } else if (expressionNode instanceof FieldAccessExpressionNode) {
            return translateFieldAccessExpression(function, (FieldAccessExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixDecrementSubtractionExpressionNode) {
            return translatePostfixDecrementExpression(function, (PostfixDecrementSubtractionExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixIncrementAdditiveExpressionNode) {
            return translatePostfixIncrementExpression(function, (PostfixIncrementAdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof PostfixIncrementMultiplicativeExpressionNode) {
            return translatePostfixMultiplicative(function, (PostfixIncrementMultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixDecrementSubtractionExpressionNode) {
            return translatePrefixDecrement(function, (PrefixDecrementSubtractionExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixIncrementAdditiveExpressionNode) {
            return translatePrefixIncrement(function, (PrefixIncrementAdditiveExpressionNode) expressionNode);
        } else if (expressionNode instanceof PrefixIncrementMultiplicativeExpressionNode) {
            return translatePrefixMultiplicative(function, (PrefixIncrementMultiplicativeExpressionNode) expressionNode);
        } else if (expressionNode instanceof CastExpressionNode) {
            return translateCastExpression(function, (CastExpressionNode) expressionNode);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private void translateBreak(Function function, BreakStatementNode node) {
        BasicBlock last = function.getCurrentBlock();

        BasicBlock breakBlock = function.appendBlock("break");
        createBranch(last, breakBlock);

        function.appendBlock("dummy");
    }

    private void translateCompound(Function function, CompoundStatementNode node) {
        node.getStatements().forEach(n -> translateStatement(function, n));
    }

    private void translateContinue(Function function, ContinueStatementNode node) {
        BasicBlock last = function.getCurrentBlock();

        BasicBlock continueBlock = function.appendBlock("continue");
        createBranch(last, continueBlock);

        function.appendBlock("dummy");
    }

    private void translateIfElse(Function function, IfElseStatementNode node) {
        List<BasicBlock> mergeBranches = new ArrayList<>();

        BasicBlock last = function.getCurrentBlock();

        BasicBlock condition = function.appendBlock("if_condition");
        createBranch(last, condition);

        Command ifCommand = translateExpression(function, node.getIfStatementNode().getConditionNode());
        last = function.getCurrentBlock();

        BasicBlock then = function.appendBlock("if_then");
        translateStatement(function, node.getIfStatementNode().getThenNode());

        BasicBlock elseBlock = null;
        BasicBlock finalMerge = function.appendBlock("merge");
        BasicBlock merge = function.appendBlock("merge");

        createConditionalBranch(last, ifCommand, then, merge);

        mergeBranches.add(then);

        for (ElifStatementNode elifStatementNode : node.getElifStatementNodes()) {
            Command elifCommand = translateExpression(function, elifStatementNode.getConditionNode());
            last = function.getCurrentBlock();

            BasicBlock elifThen = function.appendBlock("elif_then");
            translateStatement(function, elifStatementNode.getElseNode());

            merge = function.appendBlock("merge");

            createConditionalBranch(last, elifCommand, elifThen, merge);
            mergeBranches.add(elifThen);
        }

        if (node.getElseStatementNode() != null) {
            elseBlock = function.appendBlock("else_then");
            translateStatement(function, node.getElseStatementNode().getElseNode());
            createBranch(merge, elseBlock);
            mergeBranches.add(elseBlock);
        }

        mergeBranches.forEach(b -> createBranch(b, finalMerge));
    }

    private void translateIf(Function function, IfStatementNode node) {
        throw new IllegalArgumentException("");
    }

    private void translateElif(Function function, ElifStatementNode node) {
        throw new IllegalArgumentException("");
    }

    private void translateElse(Function function, ElseStatementNode node) {
        throw new IllegalArgumentException("");
    }

    private void translateExpressionStatement(Function function, ExpressionStatementNode node) {

    }

    private void translateDeclaration(Function function, DeclarationStatementNode node) {

    }

    private void translateReturn(Function function, ReturnStatementNode node) {

    }

    private void translateWhile(Function function, WhileStatementNode node) {
        BasicBlock last = function.getCurrentBlock();

        BasicBlock condition = function.appendBlock("while_condition");
        createBranch(last, condition);

        Command command = translateExpression(function, node.getConditionNode());

        BasicBlock body = function.appendBlock("while_body");
        body.setTerminator(new Branch(condition));

        BasicBlock merge = function.appendBlock("while_merge");
        condition.setTerminator(new ConditionalBranch(command, body, merge));
    }

    private Command translateCastExpression(Function function, CastExpressionNode expressionNode) {
        return null;
    }

    private Command translatePrefixMultiplicative(Function function, PrefixIncrementMultiplicativeExpressionNode expressionNode) {
        return null;
    }

    private Command translatePrefixIncrement(Function function, PrefixIncrementAdditiveExpressionNode expressionNode) {
        return null;
    }

    private Command translatePrefixDecrement(Function function, PrefixDecrementSubtractionExpressionNode expressionNode) {
        return null;
    }

    private Command translatePostfixMultiplicative(Function function, PostfixIncrementMultiplicativeExpressionNode expressionNode) {
        return null;
    }

    private Command translatePostfixIncrementExpression(Function function, PostfixIncrementAdditiveExpressionNode expressionNode) {
        return null;
    }

    private Command translatePostfixDecrementExpression(Function function, PostfixDecrementSubtractionExpressionNode expressionNode) {
        return null;
    }

    private Command translateFieldAccessExpression(Function function, FieldAccessExpressionNode expressionNode) {
        return null;
    }

    private Command translateArrayAccessExpression(Function function, ArrayAccessExpressionNode expressionNode) {
        return null;
    }

    private Command translateFunctionCallExpression(Function function, FunctionCallExpressionNode expressionNode) {
        return null;
    }

    private Command translateArrayConstructorExpression(Function function, ArrayConstructorExpressionNode expressionNode) {
        return null;
    }

    private Command translateNullConstantExpression(Function function, NullConstantExpressionNode expressionNode) {
        return null;
    }

    private Command translateFloatConstantExpression(Function function, FloatConstantExpressionNode expressionNode) {
        return null;
    }

    private Command translateIntConstantExpression(Function function, IntConstantExpressionNode expressionNode) {
        return null;
    }

    private Command translateBoolConstantExpression(Function function, BoolConstantExpressionNode expressionNode) {
        return null;
    }

    private Command translateVariableExpression(Function function, VariableExpressionNode expressionNode) {
        return null;
    }

    private Command translateEqualityExpression(Function function, EqualityExpressionNode expressionNode) {
        return null;
    }

    private Command translateRelationalExpression(Function function, RelationalExpressionNode expressionNode) {
        return null;
    }

    private Command translateLogicalOrExpression(Function function, LogicalOrExpressionNode expressionNode) {
        return null;
    }

    private Command translateLogicalAndExpression(Function function, LogicalAndExpressionNode expressionNode) {
        return null;
    }

    private Command translateMultiplicativeExpression(Function function, MultiplicativeExpressionNode expressionNode) {
        return null;
    }

    private Command translateAdditionalExpression(Function function, AdditiveExpressionNode expressionNode) {
        return null;
    }

    private Command translateConditionalExpression(Function function, ConditionalExpressionNode expressionNode) {
        return null;
    }

    private Command translateAssigmentExpression(Function function, AssigmentExpressionNode expressionNode) {
        return null;
    }
}
