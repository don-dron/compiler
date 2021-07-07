package lang.ir.translate;

import lang.ast.Program;
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
import lang.ir.BoolValue;
import lang.ir.Branch;
import lang.ir.Command;
import lang.ir.ConditionalBranch;
import lang.ir.FloatValue;
import lang.ir.Function;
import lang.ir.IntValue;
import lang.ir.Module;
import lang.ir.Operation;
import lang.ir.Return;
import lang.ir.Type;
import lang.ir.Value;
import lang.ir.VariableValue;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static lang.ir.Operation.ADD;
import static lang.ir.Operation.AND;
import static lang.ir.Operation.DIV;
import static lang.ir.Operation.EQ;
import static lang.ir.Operation.GE;
import static lang.ir.Operation.GT;
import static lang.ir.Operation.LE;
import static lang.ir.Operation.LT;
import static lang.ir.Operation.MOD;
import static lang.ir.Operation.MUL;
import static lang.ir.Operation.NE;
import static lang.ir.Operation.OR;
import static lang.ir.Operation.STORE;
import static lang.ir.Operation.SUB;

public class Translator {
    private final Program program;
    private int TEMP_VARIABLE_COUNT = 0;

    private final Map<String, Value> variables;
    private final Map<WhileStatementNode, BasicBlock> whileToConditionBlock;
    private final Map<WhileStatementNode, BasicBlock> whileToMergeBlock;

    public Translator(Program program) {
        this.program = program;
        variables = new HashMap<>();
        whileToConditionBlock = new HashMap<>();
        whileToMergeBlock = new HashMap<>();
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
        function.setReturnBlock(returnBlock);
        returnBlock.setTerminator(new Return());

        BasicBlock entry = function.appendBlock("entry");
        createBranch(header, entry);

        translateStatement(function, functionDefinitionNode.getStatementNode());

        function.getCurrentBlock().setTerminator(new Branch(returnBlock));

        System.out.println(graphVizDebug(function));
        return function;
    }

    public static String graphVizDebug(Function functionBlock) {
        StringBuilder s = new StringBuilder("digraph G {\n");

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            String body = basicBlock.getName() + ":\n" +
                    basicBlock.getCommands()
                            .stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining("\n")) + "\n" +
                    basicBlock.getTerminator().toString();

            s.append("\t\"")
                    .append(basicBlock.getName())
                    .append("\"")
                    .append(" ")
                    .append("[")
                    .append("style=filled, shape=box, label=\"")
                    .append(body)
                    .append("\"")
                    .append("];\n");
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
                                         Value value,
                                         BasicBlock left,
                                         BasicBlock right) {
        source.addOutput(left);
        source.addOutput(right);
        left.addInput(source);
        right.addInput(source);
        source.setTerminator(new ConditionalBranch(value, left, right));
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

    private Value translateExpression(Function function, ExpressionNode expressionNode) {
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

        createBranch(breakBlock, whileToMergeBlock.get((WhileStatementNode) node.getCycle()));

        function.appendBlock("dummy");
    }

    private void translateCompound(Function function, CompoundStatementNode node) {
        node.getStatements().forEach(n -> translateStatement(function, n));
    }

    private void translateContinue(Function function, ContinueStatementNode node) {
        BasicBlock last = function.getCurrentBlock();

        BasicBlock continueBlock = function.appendBlock("continue");
        createBranch(last, continueBlock);

        createBranch(continueBlock, whileToConditionBlock.get((WhileStatementNode) node.getCycle()));

        function.appendBlock("dummy");
    }

    private void translateIfElse(Function function, IfElseStatementNode node) {
        List<BasicBlock> endsBlocks = new ArrayList<>();
        BasicBlock last = function.getCurrentBlock();

        BasicBlock condition = function.appendBlock("if_condition");
        createBranch(last, condition);

        Value ifCommand = translateExpression(function, node.getIfStatementNode().getConditionNode());
        BasicBlock endCondition = function.getCurrentBlock();

        BasicBlock then = function.appendBlock("if_then");
        translateStatement(function, node.getIfStatementNode().getThenNode());
        endsBlocks.add(function.getCurrentBlock());

        BasicBlock merge = function.appendBlock("if_else");
        createConditionalBranch(endCondition, ifCommand, then, merge);

        if(node.getElifStatementNodes().isEmpty() && node.getElseStatementNode() == null) {
            endsBlocks.add(function.getCurrentBlock());
        }

        for (ElifStatementNode elifStatementNode : node.getElifStatementNodes()) {
            Value elifCommand = translateExpression(function, elifStatementNode.getConditionNode());
            last = function.getCurrentBlock();

            BasicBlock elifThen = function.appendBlock("elif_then");
            translateStatement(function, elifStatementNode.getElseNode());
            endsBlocks.add(function.getCurrentBlock());

            merge = function.appendBlock("elif_else");

            createConditionalBranch(last, elifCommand, elifThen, merge);
        }

        if (node.getElseStatementNode() != null) {
            last = function.getCurrentBlock();
            BasicBlock elseBlock = function.appendBlock("else_then");
            createBranch(last, elseBlock);
            translateStatement(function, node.getElseStatementNode().getElseNode());
            endsBlocks.add(function.getCurrentBlock());
        }

        BasicBlock finalMerge = function.appendBlock("merge");

        endsBlocks.forEach(b -> createBranch(b, finalMerge));
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
        translateExpression(function, node.getExpressionNode());
    }

    private void translateDeclaration(Function function, DeclarationStatementNode node) {
        Value variableValue = new VariableValue(node.getIdentifierNode().getName(), matchType(node.getTypeNode()));
        variables.put(node.getIdentifierNode().getName(), variableValue);

        if (node.getExpressionNode() != null) {
            Value value = translateExpression(function, node.getExpressionNode());
            BasicBlock current = function.getCurrentBlock();
            current.addCommand(new Command(variableValue, STORE, List.of(value)));
        }
    }

    private Type matchType(TypeNode typeNode) {
        return Type.INT_4;
    }

    private void translateReturn(Function function, ReturnStatementNode node) {
        BasicBlock last = function.getCurrentBlock();
        Value value = null;

        if (node.getExpressionNode() != null) {
            value = translateExpression(function, node.getExpressionNode());
            last = function.getCurrentBlock();
        }

        BasicBlock returnBlock = function.appendBlock("local_return");

        if (value != null) {

        }

        createBranch(last, returnBlock);
        createBranch(returnBlock, function.getReturnBlock());

        function.appendBlock("dummy");
    }

    private void translateWhile(Function function, WhileStatementNode node) {
        BasicBlock last = function.getCurrentBlock();

        BasicBlock condition = function.appendBlock("while_condition");
        createBranch(last, condition);

        whileToConditionBlock.put(node, condition);

        Value value = translateExpression(function, node.getConditionNode());
        last = function.getCurrentBlock();

        BasicBlock merge = function.appendBlock("merge");
        whileToMergeBlock.put(node, merge);

        BasicBlock body = function.appendBlock("while_body");
        translateStatement(function, node.getBodyNode());
        createBranch(function.getCurrentBlock(), condition);

        BasicBlock whileMerge = function.appendBlock("while_merge");
        createBranch(merge, whileMerge);

        createConditionalBranch(last, value, body, merge);
    }

    private Value translateCastExpression(Function function, CastExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePrefixMultiplicative(Function function, PrefixIncrementMultiplicativeExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePrefixIncrement(Function function, PrefixIncrementAdditiveExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePrefixDecrement(Function function, PrefixDecrementSubtractionExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePostfixMultiplicative(Function function, PostfixIncrementMultiplicativeExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePostfixIncrementExpression(Function function, PostfixIncrementAdditiveExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translatePostfixDecrementExpression(Function function, PostfixDecrementSubtractionExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateFieldAccessExpression(Function function, FieldAccessExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateArrayAccessExpression(Function function, ArrayAccessExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateFunctionCallExpression(Function function, FunctionCallExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateArrayConstructorExpression(Function function, ArrayConstructorExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateNullConstantExpression(Function function, NullConstantExpressionNode expressionNode) {

        BasicBlock current = function.getCurrentBlock();
        return null;
    }

    private Value translateFloatConstantExpression(Function function, FloatConstantExpressionNode expressionNode) {
        return new FloatValue(expressionNode.getValue());
    }

    private Value translateIntConstantExpression(Function function, IntConstantExpressionNode expressionNode) {
        return new IntValue(expressionNode.getValue());
    }

    private Value translateBoolConstantExpression(Function function, BoolConstantExpressionNode expressionNode) {
        return new BoolValue(expressionNode.getValue());
    }

    private Value translateVariableExpression(Function function, VariableExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();

        Command command = new Command(createTempVariable(matchType(
                expressionNode.getResultType())),
                STORE,
                List.of(variables.get(expressionNode.getIdentifierNode().getName())));

        current.addCommand(command);

        return command.getResult();
    }

    private Value translateEqualityExpression(Function function, EqualityExpressionNode expressionNode) {
        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                expressionNode.getType() == EqualityExpressionNode.EqualityType.EQ ? EQ : NE,
                matchType(expressionNode.getResultType()));
    }

    private Value translateRelationalExpression(Function function, RelationalExpressionNode expressionNode) {
        Operation operation = null;
        switch (expressionNode.getType()) {
            case GE:
                operation = GE;
                break;
            case GT:
                operation = GT;
                break;
            case LE:
                operation = LE;
                break;
            case LT:
            default:
                operation = LT;
        }

        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                operation,
                matchType(expressionNode.getResultType()));
    }

    private Value translateLogicalOrExpression(Function function, LogicalOrExpressionNode expressionNode) {
        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                OR,
                matchType(expressionNode.getResultType()));
    }

    private Value translateLogicalAndExpression(Function function, LogicalAndExpressionNode expressionNode) {
        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                AND,
                matchType(expressionNode.getResultType()));
    }

    private Value translateMultiplicativeExpression(Function function, MultiplicativeExpressionNode expressionNode) {
        Operation operation = expressionNode.getType() == MultiplicativeExpressionNode.MultiplicativeType.MUL
                ? MUL
                : expressionNode.getType() == MultiplicativeExpressionNode.MultiplicativeType.DIV
                ? DIV
                : MOD;

        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                operation,
                matchType(expressionNode.getResultType()));
    }

    private Value translateAdditionalExpression(Function function, AdditiveExpressionNode expressionNode) {
        Operation operation = expressionNode.getType() == AdditiveExpressionNode.AdditiveType.ADD ? ADD : SUB;

        return translateBinaryOperation(
                function,
                expressionNode.getLeft(),
                expressionNode.getRight(),
                operation,
                matchType(expressionNode.getResultType()));
    }

    private Value translateBinaryOperation(Function function,
                                           ExpressionNode left,
                                           ExpressionNode right,
                                           Operation operation,
                                           Type resultType) {
        Value leftValue = translateExpression(function, left);
        Value rightValue = translateExpression(function, right);

        Command command = new Command(createTempVariable(resultType), operation, List.of(leftValue, rightValue));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        return command.getResult();
    }

    private Value translateConditionalExpression(Function function, ConditionalExpressionNode expressionNode) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock condition = function.appendBlock("conditional");
        BasicBlock thenBlock = null;
        BasicBlock elseBlock = null;
        BasicBlock mergeBlock = null;

        createBranch(last, condition);

        Value resultValue = createTempVariable(matchType(expressionNode.getResultType()));
        Value conditionValue = translateExpression(function, expressionNode.getConditionNode());
        BasicBlock endCondition = function.getCurrentBlock();

        thenBlock = function.appendBlock("conditional_then");
        Value firstArg = translateExpression(function, expressionNode.getThenNode());
        BasicBlock endThen = function.getCurrentBlock();
        Command firstStore = new Command(resultValue, STORE, List.of(firstArg));
        endThen.addCommand(firstStore);

        elseBlock = function.appendBlock("conditional_else");
        Value secondArg = translateExpression(function, expressionNode.getElseNode());
        BasicBlock endElse = function.getCurrentBlock();
        Command secondStore = new Command(resultValue, STORE, List.of(secondArg));
        endElse.addCommand(secondStore);

        mergeBlock = function.appendBlock("conditional_result");

        createBranch(endThen, mergeBlock);
        createBranch(endElse, mergeBlock);
        createConditionalBranch(endCondition, conditionValue, thenBlock, elseBlock);

        return resultValue;
    }

    private Value translateAssigmentExpression(Function function, AssigmentExpressionNode expressionNode) {
        Value left = translateExpression(function, expressionNode.getLeft());
        Value right = translateExpression(function, expressionNode.getRight());

        Command command = new Command(left, STORE, List.of(right));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        return command.getResult();
    }

    private VariableValue createTempVariable(Type type) {
        return new VariableValue(
                "$$_" + TEMP_VARIABLE_COUNT++,
                type);
    }
}
