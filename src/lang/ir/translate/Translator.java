package lang.ir.translate;

import lang.ast.*;
import lang.ast.expression.*;
import lang.ast.expression.binary.AdditiveExpressionNode;
import lang.ast.expression.binary.AssigmentExpressionNode;
import lang.ast.expression.binary.EqualityExpressionNode;
import lang.ast.expression.binary.LogicalAndExpressionNode;
import lang.ast.expression.binary.LogicalOrExpressionNode;
import lang.ast.expression.binary.MultiplicativeExpressionNode;
import lang.ast.expression.binary.RelationalExpressionNode;
import lang.ast.expression.consts.*;
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
import lang.ast.statement.*;
import lang.ir.*;
import lang.ir.Module;
import lang.lexer.Token;
import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lang.ir.Operation.*;
import static lang.ir.Type.*;

public class Translator {
    private final Program program;
    private int LITERAL_COUNT = 0;
    private int THIS_COUNT = 0;
    private int RET_COUNT = 0;
    private int CONSTRUCTOR_COUNT = 0;
    private int TEMP_VARIABLE_COUNT = 0;
    private int ARRAY_SIZE_COUNT = 0;

    private final List<StringValue> literals;
    private final Map<String, StructType> classes;
    private final Map<String, Value> variables;
    private final Map<WhileStatementNode, BasicBlock> whileToConditionBlock;
    private final Map<WhileStatementNode, BasicBlock> whileToMergeBlock;
    private Map<ConstructorDefinitionNode, Function> constructors;
    private final List<Function> predefinedFunctions;

    public Translator(Program program) {
        this.program = program;
        literals = new ArrayList<>();
        classes = new HashMap<>();
        variables = new HashMap<>();
        whileToConditionBlock = new HashMap<>();
        whileToMergeBlock = new HashMap<>();
        predefinedFunctions = new ArrayList<>();
    }

    private Function getPutStringFunction() {
        Function function = new Function("puts", true);
        function.setParameterTypes(List.of(new PointerType(INT_32)));
        function.setResultType(INT_32);
        return function;
    }

    private Function getPutcharFunction() {
        Function function = new Function("putchar", true);
        function.setParameterTypes(List.of(INT_32));
        function.setResultType(INT_32);
        return function;
    }

    private Function getGetcharFunction() {
        Function function = new Function("getchar", true);
        function.setParameterTypes(List.of());
        function.setResultType(INT_32);
        return function;
    }

    private Function getMallocFunction() {
        Function function = new Function("malloc", true);
        function.setParameterTypes(List.of(INT_32));
        function.setResultType(new PointerType(INT_64));
        return function;
    }

    public Module translate() {
        predefinedFunctions.add(getPutStringFunction());
        predefinedFunctions.add(getPutcharFunction());
        predefinedFunctions.add(getGetcharFunction());
        predefinedFunctions.add(getMallocFunction());

        predefinedFunctions
                .forEach(f -> {
                    variables.put(f.getName(), f);
                });


        program.getClasses()
                .forEach(this::translateClass);

        Map<Function, FunctionDefinitionNode> functionToStatement = program.getFunctions()
                .stream()
                .collect(Collectors.toMap(
                        functionDefinitionNode -> {
                            String name = functionDefinitionNode.getIdentifierNode().getName();

                            if (program.getMainFunction().getIdentifierNode().getName()
                                    .equals(functionDefinitionNode.getIdentifierNode().getName())) {
                                name = "main";
                            }

                            Function function = new Function(name);
                            variables.put(name, function);
                            return function;
                        },
                        java.util.function.Function.identity()));


        constructors = program.getConstructors()
                .stream()
                .collect(Collectors.toMap(java.util.function.Function.identity(),
                        constructorDefinitionNode -> {
                            String name = "$_constructor_" + CONSTRUCTOR_COUNT++;

                            FunctionDefinitionNode functionDefinitionNode =
                                    new FunctionDefinitionNode(
                                            constructorDefinitionNode.getFunctionNode(),
                                            new IdentifierNode(name, null),
                                            constructorDefinitionNode.getStatementNode()
                                    );
                            Function function = new Function(name);
                            variables.put(name, function);
                            functionToStatement.put(function, functionDefinitionNode);

                            return function;
                        }
                ));
        List<Function> functions = functionToStatement
                .entrySet()
                .stream()
                .map(entry -> translateFunction(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        functions.addAll(predefinedFunctions);

        return new Module(
                new ArrayList<>(classes.values()),
                functions,
                literals);
    }

    private void translateClass(ClassStatementNode cl) {
        List<DeclarationStatementNode> fields = cl.getFields();
        List<VariableValue> values = new ArrayList<>();

        for (DeclarationStatementNode field : fields) {
            VariableValue variableValue = new VariableValue(
                    field.getIdentifierNode().getName(),
                    matchType(field.getTypeNode()));
            values.add(variableValue);
        }

        StructType structType = new StructType(
                cl.getIdentifierNode().getName(),
                values);

        classes.put(cl.getIdentifierNode().getName(), structType);
    }

    private Function translateFunction(Function function, FunctionDefinitionNode functionDefinitionNode) {
        BasicBlock header = function.appendBlock("header");

        List<Type> parameterTypes = new ArrayList<>();

        if (functionDefinitionNode.getScope() != null
                && functionDefinitionNode.getScope().getOwner() instanceof ClassStatementNode
                && !constructors.containsValue(function)) {
            ClassStatementNode classStatementNode = (ClassStatementNode)
                    functionDefinitionNode.getScope().getOwner();
            Type type = matchType(new ObjectTypeNode(classStatementNode.getIdentifierNode()));
            parameterTypes.add(type);

            String thisName = "$$_this_value_" + THIS_COUNT++;
            VariableValue thisValue = new VariableValue(
                    thisName,
                    type
            );
            variables.put(thisName, thisValue);

            function.setThisValue(thisValue);
            Command alloc = new Command(
                    thisValue,
                    ALLOC,
                    List.of()
            );

            header.addCommand(alloc);

            Command command = new Command(
                    thisValue,
                    STORE,
                    List.of(createTempVariable(type))
            );

            header.addCommand(command);
        }

        for (ParameterNode n : functionDefinitionNode
                .getFunctionNode()
                .getParametersNode()
                .getParameters()) {
            Type type = matchType(n.getTypeNode());
            parameterTypes.add(type);
            VariableValue variableValue = new VariableValue(
                    n.getIdentifierNode().getName(),
                    type
            );
            variables.put(n.getIdentifierNode().getName(), variableValue);

            Command alloc = new Command(
                    variableValue,
                    ALLOC,
                    List.of()
            );

            header.addCommand(alloc);

            Command command = new Command(
                    variableValue,
                    STORE,
                    List.of(createTempVariable(type))
            );

            header.addCommand(command);
        }
        BasicBlock returnBlock = null;

        if (!functionDefinitionNode.getFunctionNode().getTypeNode().equals(GlobalBasicType.VOID_TYPE)) {
            String name = "$$_ret_value_" + RET_COUNT++;
            Type type = matchType(functionDefinitionNode.getFunctionNode().getTypeNode());

            Value variableValue = new VariableValue(name, type);
            variables.put(name, variableValue);

            Command alloc = new Command(
                    variableValue,
                    ALLOC,
                    List.of()
            );
            header.addCommand(alloc);

            if (constructors.containsValue(function)) {
                String thisName = "$$_this_value_" + THIS_COUNT++;
                Type thisType = matchType(functionDefinitionNode.getFunctionNode().getTypeNode());

                VariableValue thisVariableValue = new VariableValue(thisName, thisType);
                variables.put(thisName, thisVariableValue);

                function.setThisValue(thisVariableValue);

                Command thisAlloc = new Command(
                        thisVariableValue,
                        ALLOC,
                        List.of()
                );
                header.addCommand(thisAlloc);

                PointerType pointerStructType = (PointerType) type;
                PointerType pointerType = new PointerType(INT_64);
                Command command = new Command(
                        createTempVariable(pointerType),
                        STRUCT_ALLOCATION,
                        List.of(pointerType, new IntValue(pointerStructType.getType().getSize()))
                );
                function.getCurrentBlock().addCommand(command);

                Command castCommand = new Command(
                        createTempVariable(type),
                        CAST,
                        List.of(command.getResult())
                );
                function.getCurrentBlock().addCommand(castCommand);

                Command store = new Command(
                        thisVariableValue,
                        STORE,
                        List.of(castCommand.getResult())
                );
                function.getCurrentBlock().addCommand(store);

                Command storeRet = new Command(
                        variableValue,
                        STORE,
                        List.of(castCommand.getResult())
                );
                function.getCurrentBlock().addCommand(storeRet);
            }

            returnBlock = function.appendBlock("return");

            Command loadReturn = new Command(
                    createTempVariable(type),
                    LOAD,
                    List.of(variableValue)
            );

            function.setReturnValue(variableValue);

            returnBlock.addCommand(loadReturn);
            returnBlock.setTerminator(new Return(loadReturn.getResult()));
        } else {
            returnBlock = function.appendBlock("return");
            returnBlock.setTerminator(new Return(null));
        }

        function.setParameterTypes(parameterTypes);
        function.setResultType(matchType(functionDefinitionNode.getFunctionNode().getTypeNode()));
        function.setReturnBlock(returnBlock);

        BasicBlock entry = function.appendBlock("entry");
        createBranch(header, entry);

        translateStatement(function, functionDefinitionNode.getStatementNode());

        function.getCurrentBlock().setTerminator(new Branch(returnBlock));

        TEMP_VARIABLE_COUNT = 0;
        return function;
    }

    public static String graphVizDebug(Function functionBlock) {
        StringBuilder s = new StringBuilder();
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
        } else if (node instanceof FunctionDefinitionNode) {
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
        } else if (expressionNode instanceof CharConstantExpressionNode) {
            return translateCharConstantExpression(function, (CharConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof StringConstantExpressionNode) {
            return translateStringConstantExpressionNode(function, (StringConstantExpressionNode) expressionNode);
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
        } else if (expressionNode instanceof ObjectConstructorExpressionNode) {
            return translateObjectConstructorExpression(function, (ObjectConstructorExpressionNode) expressionNode);
        } else if (expressionNode instanceof ThisExpressionNode) {
            return translateThisExpression(function, (ThisExpressionNode) expressionNode);
        } else {
            throw new IllegalArgumentException("");
        }
    }


    private Value translateThisExpression(Function function, ThisExpressionNode expressionNode) {

        VariableValue variableValue = function.getThisValue();
        Command load = new Command(
                createTempVariable(variableValue.getType()),
                LOAD,
                List.of(variableValue)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(load);
        return load.getResult();
    }

    private Value translateObjectConstructorExpression(Function function,
                                                       ObjectConstructorExpressionNode expressionNode) {
        Value functionValue = constructors.get(expressionNode.getConstructorDefinitionNode());
        List<Value> values =
                expressionNode
                        .getParameters()
                        .stream()
                        .map(exp -> translateExpression(function, exp))
                        .collect(Collectors.toList());

        Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        return command.getResult();
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

        if (node.getElifStatementNodes().isEmpty() && node.getElseStatementNode() == null) {
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
        if (node.getExpressionNode() instanceof FunctionCallExpressionNode) {
            FunctionCallExpressionNode expressionNode = (FunctionCallExpressionNode) node.getExpressionNode();
            if (expressionNode.getFunction() instanceof FieldAccessExpressionNode) {
                Value leftValue = translateExpression(
                        function,
                        ((FieldAccessExpressionNode) expressionNode.getFunction()).getLeft());

                Value functionValue =
                        translateVariableExpression(
                                function, ((FieldAccessExpressionNode) expressionNode.getFunction()).getRight());
                List<Value> values =
                        expressionNode
                                .getParameters()
                                .getList()
                                .stream()
                                .map(exp -> translateExpression(function, exp))
                                .collect(Collectors.toList());
                values.add(0, leftValue);

                if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                    Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                            Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                    BasicBlock current = function.getCurrentBlock();
                    current.addCommand(command);
                } else {
                    Command command = new Command(null, CALL,
                            Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                    BasicBlock current = function.getCurrentBlock();
                    current.addCommand(command);
                }
            } else {
                Value functionValue = translateExpression(function, expressionNode.getFunction());
                List<Value> values =
                        expressionNode
                                .getParameters()
                                .getList()
                                .stream()
                                .map(exp -> translateExpression(function, exp))
                                .collect(Collectors.toList());

                if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                    Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                            Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                    BasicBlock current = function.getCurrentBlock();
                    current.addCommand(command);
                } else {
                    Command command = new Command(
                            functionValue.getType() == null || functionValue.getType().equals(VOID)
                                    ? null
                                    : createTempVariable(functionValue.getType()), CALL,
                            Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                    BasicBlock current = function.getCurrentBlock();
                    current.addCommand(command);
                }
            }
        } else {
            translateExpression(function, node.getExpressionNode());
        }
    }

    private void translateDeclaration(Function function, DeclarationStatementNode node) {
        Value variableValue = new VariableValue(node.getIdentifierNode().getName(), matchType(node.getTypeNode()));
        variables.put(node.getIdentifierNode().getName(), variableValue);

        Command alloc = new Command(
                variableValue,
                ALLOC,
                List.of()
        );

        function.getCurrentBlock().addCommand(alloc);

        if (node.getExpressionNode() != null) {
            Value value = translateExpression(function, node.getExpressionNode());
            BasicBlock current = function.getCurrentBlock();
            current.addCommand(new Command(variableValue, STORE, List.of(value)));
        }
    }

    private Type matchType(TypeNode typeNode) {
        if (typeNode instanceof BasicTypeNode) {
            BasicTypeNode basicTypeNode = (BasicTypeNode) typeNode;

            if (basicTypeNode.getType() == TypeNode.Type.VOID) {
                return VOID;
            } else if (basicTypeNode.getType() == TypeNode.Type.BOOL) {
                return Type.INT_1;
            } else if (basicTypeNode.getType() == TypeNode.Type.CHAR) {
                return Type.INT_8;
            } else if (basicTypeNode.getType() == TypeNode.Type.INT) {
                return Type.INT_32;
            } else if (basicTypeNode.getType() == TypeNode.Type.LONG) {
                return Type.INT_64;
            }

            return Type.INT_32;
        } else if (typeNode instanceof ArrayTypeNode) {
            ArrayTypeNode arrayTypeNode = (ArrayTypeNode) typeNode;
            return new PointerType(matchType(arrayTypeNode.getTypeNode()));
        } else if (typeNode instanceof ObjectTypeNode) {
            if (((ObjectTypeNode) typeNode).getDefinitionNode() instanceof InterfaceStatementNode) {
                return new PointerType(classes.get(
                        ((InterfaceStatementNode) ((ObjectTypeNode) typeNode).getDefinitionNode())
                                .getIdentifierNode().getName()));
            } else {
                String name = ((ObjectTypeNode) typeNode).getIdentifierNode().getName();
                StructType structType = classes.get(name);

                return new PointerType(structType);
            }
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private void translateReturn(Function function, ReturnStatementNode node) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock returnBlock = function.appendBlock("local_return");
        createBranch(last, returnBlock);

        if (node.getExpressionNode() != null) {
            Value value = translateExpression(function, node.getExpressionNode());

            Command storeReturn = new Command(
                    function.getReturnValue(),
                    STORE,
                    List.of(value)
            );
            returnBlock.addCommand(storeReturn);
        }

        createBranch(function.getCurrentBlock(), function.getReturnBlock());

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
        Type type = matchType(expressionNode.getTypeNode());
        Value value = translateExpression(function, expressionNode.getExpressionNode());

        if(value.getType().getSize() < type.getSize()) {
            Command command = new Command(createTempVariable(type), SEXT, List.of(value));
            function.getCurrentBlock().addCommand(command);
            return command.getResult();
        } else if (value.getType().getSize() > type.getSize()) {
            Command command = new Command(createTempVariable(type), TRUNC, List.of(value));
            function.getCurrentBlock().addCommand(command);
            return command.getResult();
        } else {
            Command command = new Command(createTempVariable(type), CAST, List.of(value));
            function.getCurrentBlock().addCommand(command);
            return command.getResult();
        }
    }

    private Value translatePrefixMultiplicative(Function function, PrefixIncrementMultiplicativeExpressionNode
            expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(load);

        Command change = new Command(
                createTempVariable(type),
                ADD,
                List.of(load.getResult(), load.getResult())
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        current.addCommand(load);
        return load.getResult();
    }

    private Value translatePrefixIncrement(Function function, PrefixIncrementAdditiveExpressionNode expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(load);

        Command change = new Command(
                createTempVariable(type),
                ADD,
                List.of(load.getResult(), new IntValue(1))
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        current.addCommand(load);
        return load.getResult();
    }

    private Value translatePrefixDecrement(Function function, PrefixDecrementSubtractionExpressionNode
            expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(load);

        Command change = new Command(
                createTempVariable(type),
                SUB,
                List.of(load.getResult(), new IntValue(1))
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        load = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        current.addCommand(load);
        return load.getResult();
    }

    private Value translatePostfixMultiplicative(Function function, PostfixIncrementMultiplicativeExpressionNode
            expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command loadValue = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(loadValue);

        Command change = new Command(
                createTempVariable(type),
                ADD,
                List.of(loadValue.getResult(), loadValue.getResult())
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        return loadValue.getResult();
    }

    private Value translatePostfixIncrementExpression(Function function, PostfixIncrementAdditiveExpressionNode
            expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command loadValue = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(loadValue);

        Command change = new Command(
                createTempVariable(type),
                ADD,
                List.of(loadValue.getResult(), new IntValue(1))
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        return loadValue.getResult();
    }

    private Value translatePostfixDecrementExpression(Function function, PostfixDecrementSubtractionExpressionNode
            expressionNode) {
        Type type = matchType(expressionNode.getResultType());

        VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode.getExpressionNode();

        Value value = variables.get(variableExpressionNode.getIdentifierNode().getName());

        Command loadValue = new Command(
                createTempVariable(type),
                LOAD,
                List.of(value)
        );

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(loadValue);

        Command change = new Command(
                createTempVariable(type),
                SUB,
                List.of(loadValue.getResult(), new IntValue(1))
        );

        current.addCommand(change);

        Command store = new Command(
                value,
                STORE,
                List.of(change.getResult())
        );

        current.addCommand(store);

        return loadValue.getResult();
    }

    private Value translateFieldAccessExpression(Function function,
                                                 FieldAccessExpressionNode fieldAccessExpressionNode) {
        Value structValue = translateExpression(function, fieldAccessExpressionNode.getLeft());

        PointerType pointerType = (PointerType) structValue.getType();
        StructType structType = (StructType) pointerType.getType();
        int index = 0;
        String name = fieldAccessExpressionNode.getRight().getIdentifierNode().getName();

        for (int i = 0; i < structType.getTypes().size(); i++) {
            if (structType.getTypes().get(i).getName().equals(name)) {
                index = i;
                break;
            }
        }

        Command command = new Command(
                createTempVariable(matchType(fieldAccessExpressionNode.getResultType())),
                FIELD_ACCESS,
                List.of(structValue, new IntValue(index)));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        Command load = new Command(
                createTempVariable(matchType(fieldAccessExpressionNode.getResultType())),
                LOAD,
                List.of(command.getResult())
        );

        function.getCurrentBlock().addCommand(load);
        return load.getResult();
    }

    private Value translateArrayAccessExpression(Function function, ArrayAccessExpressionNode expressionNode) {
        Value arrayValue = translateExpression(function, expressionNode.getArray());
        Value offsetValue = translateExpression(function, expressionNode.getArgument());

        Command command = new Command(
                createTempVariable(matchType(expressionNode.getResultType())),
                ARRAY_ACCESS,
                List.of(arrayValue, offsetValue));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        Command load = new Command(
                createTempVariable(matchType(expressionNode.getResultType())),
                LOAD,
                List.of(command.getResult())
        );

        function.getCurrentBlock().addCommand(load);

        return load.getResult();
    }

    private Value translateFunctionCallExpression(Function function, FunctionCallExpressionNode expressionNode) {
        if (expressionNode.getFunction() instanceof FieldAccessExpressionNode) {
            Value leftValue = translateExpression(
                    function,
                    ((FieldAccessExpressionNode) expressionNode.getFunction()).getLeft());

            Value functionValue =
                    translateVariableExpression(
                            function, ((FieldAccessExpressionNode) expressionNode.getFunction()).getRight());
            List<Value> values =
                    expressionNode
                            .getParameters()
                            .getList()
                            .stream()
                            .map(exp -> translateExpression(function, exp))
                            .collect(Collectors.toList());
            values.add(0, leftValue);
            if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);

                return command.getResult();
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            Value functionValue = translateExpression(function, expressionNode.getFunction());
            List<Value> values =
                    expressionNode
                            .getParameters()
                            .getList()
                            .stream()
                            .map(exp -> translateExpression(function, exp))
                            .collect(Collectors.toList());
            if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);

                return command.getResult();
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private int defineArrayTypeSize(Type type) {
        if (type == INT_1) {
            return 1;
        } else if (type == INT_8) {
            return 1;
        } else if (type == INT_16) {
            return 2;
        } else if (type == INT_32) {
            return 4;
        } else if (type == INT_64) {
            return 8;
        } else if (type instanceof PointerType) {
            return 8;
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private Value addWhileAllocation(Function function,
                                     Value arrayValue,
                                     Type type,
                                     List<VariableValue> arrayAccessVariables,
                                     List<VariableValue> iteratorVariables,
                                     List<VariableValue> sizeVariables) {
        if (type instanceof PointerType
                && !(((PointerType) type).getType() instanceof StructType)) {
            PointerType targetType = (PointerType) type;
            VariableValue sizeVariable = sizeVariables.remove(0);
            VariableValue arrayAccessVariable = arrayAccessVariables.remove(0);
            VariableValue iterator = iteratorVariables.remove(0);

            BasicBlock lastBlock = function.getCurrentBlock();

            Command writeIterator = new Command(
                    iterator,
                    STORE,
                    List.of(new IntValue(0))
            );
            function.getCurrentBlock().addCommand(writeIterator);

            Command loadSize = new Command(
                    createTempVariable(sizeVariable.getType()),
                    LOAD,
                    List.of(sizeVariable)
            );
            function.getCurrentBlock().addCommand(loadSize);

            Command trueSize = new Command(
                    createTempVariable(INT_32),
                    MUL,
                    List.of(new IntValue(defineArrayTypeSize(type.getType())), loadSize.getResult())
            );
            function.getCurrentBlock().addCommand(trueSize);

            PointerType pointerType = new PointerType(INT_64);
            Command command = new Command(
                    createTempVariable(pointerType),
                    ARRAY_ALLOCATION,
                    List.of(pointerType, trueSize.getResult())
            );
            function.getCurrentBlock().addCommand(command);

            Command castCommand = new Command(
                    createTempVariable(targetType),
                    CAST,
                    List.of(command.getResult())
            );
            function.getCurrentBlock().addCommand(castCommand);

            Command arrayStore = new Command(
                    arrayValue,
                    STORE,
                    List.of(castCommand.getResult())
            );
            function.getCurrentBlock().addCommand(arrayStore);

            Command loadAccess = new Command(
                    arrayAccessVariable,
                    STORE,
                    List.of(castCommand.getResult())
            );
            function.getCurrentBlock().addCommand(loadAccess);

            BasicBlock allocCondition = function.appendBlock("alloc_condition");
            createBranch(lastBlock, allocCondition);
            Command readSize = new Command(
                    createTempVariable(INT_32),
                    LOAD,
                    List.of(sizeVariable)
            );
            Command readIterator = new Command(
                    createTempVariable(INT_32),
                    LOAD,
                    List.of(iterator)
            );
            allocCondition.addCommand(readSize);
            allocCondition.addCommand(readIterator);

            Command condition = new Command(
                    createTempVariable(INT_1),
                    LT,
                    List.of(readIterator.getResult(), readSize.getResult())
            );

            allocCondition.addCommand(condition);

            BasicBlock allocBody = function.appendBlock("alloc_body");
            Command accessArray = new Command(
                    createTempVariable(arrayValue.getType()),
                    LOAD,
                    List.of(arrayAccessVariable)
            );
            function.getCurrentBlock().addCommand(accessArray);

            Command accessOffset = new Command(
                    createTempVariable(iterator.getType()),
                    LOAD,
                    List.of(iterator)
            );
            function.getCurrentBlock().addCommand(accessOffset);

            Command arrayAccess = new Command(
                    createTempVariable(targetType.getType()),
                    ARRAY_ACCESS,
                    List.of(accessArray.getResult(), accessOffset.getResult()));
            function.getCurrentBlock().addCommand(arrayAccess);

            addWhileAllocation(function, arrayAccess.getResult(), targetType.getType(),
                    arrayAccessVariables,
                    iteratorVariables,
                    sizeVariables);

            Command loadIterator = new Command(
                    createTempVariable(iterator.getType()),
                    LOAD,
                    List.of(iterator)
            );
            function.getCurrentBlock().addCommand(loadIterator);

            Command increment = new Command(
                    createTempVariable(iterator.getType()),
                    ADD,
                    List.of(loadIterator.getResult(), new IntValue(1))
            );
            function.getCurrentBlock().addCommand(increment);

            Command store = new Command(
                    iterator,
                    STORE,
                    List.of(increment.getResult())
            );
            function.getCurrentBlock().addCommand(store);

            createBranch(function.getCurrentBlock(), allocCondition);

            BasicBlock allocMerge = function.appendBlock("alloc_merge");
            createConditionalBranch(allocCondition, condition.getResult(), allocBody, allocMerge);

            return arrayValue;
        } else {
            if (type instanceof PointerType) {
                Command command = new Command(
                        arrayValue,
                        STORE,
                        List.of(new NullValue(type))
                );
                function.getCurrentBlock().addCommand(command);
                return command.getResult();
            } else {
                Command command = new Command(
                        arrayValue,
                        STORE,
                        List.of(type.getType() == INT_8 ? new CharValue((char) 0) : new IntValue(0))
                );
                function.getCurrentBlock().addCommand(command);
                return command.getResult();
            }
        }
    }

    private Value translateArrayConstructorExpression(Function function, ArrayConstructorExpressionNode
            expressionNode) {
        PointerType targetType = (PointerType) matchType(expressionNode.getResultType());

        VariableValue arrayValue = new VariableValue("$array_" + ARRAY_SIZE_COUNT, targetType);
        Command arrayVariable = new Command(
                arrayValue,
                ALLOC,
                List.of()
        );
        function.getCurrentBlock().addCommand(arrayVariable);

        List<VariableValue> arrayAccessValues = new ArrayList<>();
        List<VariableValue> iteratorValues = new ArrayList<>();
        List<VariableValue> sizeValues = new ArrayList<>();

        Type type = targetType;

        for (ExpressionNode node : expressionNode.getSizeExpression()) {
            VariableValue size = new VariableValue("$size_" + ARRAY_SIZE_COUNT, INT_32);
            VariableValue iterator = new VariableValue("$alloc_iterator_" + ARRAY_SIZE_COUNT, INT_32);
            VariableValue arrayAccessVariable = new VariableValue("$array_access_" + ARRAY_SIZE_COUNT,
                    type);

            ARRAY_SIZE_COUNT++;
            Command allocVariable = new Command(
                    size,
                    ALLOC,
                    List.of()
            );
            function.getCurrentBlock().addCommand(allocVariable);

            Command allocIterator = new Command(
                    iterator,
                    ALLOC,
                    List.of()
            );
            function.getCurrentBlock().addCommand(allocIterator);

            Command defineAccess = new Command(
                    arrayAccessVariable,
                    ALLOC,
                    List.of()
            );
            function.getCurrentBlock().addCommand(defineAccess);

            Command writeIterator = new Command(
                    iterator,
                    STORE,
                    List.of(new IntValue(0))
            );
            function.getCurrentBlock().addCommand(writeIterator);

            Value sizeValue = translateExpression(function, node);

            Command writeSize = new Command(
                    size,
                    STORE,
                    List.of(sizeValue)
            );
            function.getCurrentBlock().addCommand(writeSize);

            arrayAccessValues.add(arrayAccessVariable);
            iteratorValues.add(iterator);
            sizeValues.add(size);

            type = type.getType();
        }

        Value mainAllocation = addWhileAllocation(function, arrayValue,
                targetType, arrayAccessValues, iteratorValues, sizeValues);

        Command read = new Command(
                createTempVariable(mainAllocation.getType()),
                LOAD,
                List.of(mainAllocation)
        );
        function.getCurrentBlock().addCommand(read);

        return read.getResult();
    }

    private Value translateNullConstantExpression(Function function, NullConstantExpressionNode expressionNode) {
        return new NullValue(matchType(expressionNode.getResultType()));
    }

    private Value translateFloatConstantExpression(Function function, FloatConstantExpressionNode expressionNode) {
        return new FloatValue(expressionNode.getValue());
    }

    private Value translateCharConstantExpression(Function function, CharConstantExpressionNode expressionNode) {
        return new CharValue(expressionNode.getValue());
    }

    private Value translateIntConstantExpression(Function function, IntConstantExpressionNode expressionNode) {
        return new IntValue(expressionNode.getValue());
    }

    private Value translateStringConstantExpressionNode(Function function,
                                                        StringConstantExpressionNode expressionNode) {
        PointerType pointerStructType = new PointerType(
                classes.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().endsWith("_String"))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null)
        );
        PointerType pointerType = new PointerType(INT_64);
        Command command = new Command(
                createTempVariable(pointerType),
                STRUCT_ALLOCATION,
                List.of(pointerType, new IntValue(pointerStructType.getType().getSize()))
        );
        function.getCurrentBlock().addCommand(command);

        Command castCommand = new Command(
                createTempVariable(pointerStructType),
                CAST,
                List.of(command.getResult())
        );
        function.getCurrentBlock().addCommand(castCommand);

        Command struct = new Command(
                createTempVariable(new PointerType(INT_8)),
                FIELD_ACCESS,
                List.of(castCommand.getResult(), new IntValue(0)));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(struct);

        StringValue value = new StringValue("@.str" + LITERAL_COUNT++, expressionNode.getValue());
        literals.add(value);

        Command castStr = new Command(
                createTempVariable(new PointerType(INT_8)),
                CAST,
                List.of(value)
        );
        function.getCurrentBlock().addCommand(castStr);

        Command store = new Command(
                struct.getResult(),
                STORE,
                List.of(castStr.getResult()));

        current = function.getCurrentBlock();
        current.addCommand(store);

        Command structSize = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castCommand.getResult(), new IntValue(1)));

        current = function.getCurrentBlock();
        current.addCommand(structSize);

        Command storeSize = new Command(
                structSize.getResult(),
                STORE,
                List.of(new IntValue(value.getType().getSize())));

        current = function.getCurrentBlock();
        current.addCommand(storeSize);

        return castCommand.getResult();
    }

    private Value translateBoolConstantExpression(Function function, BoolConstantExpressionNode expressionNode) {
        return new BoolValue(expressionNode.getValue());
    }

    private Value translateVariableExpression(Function function, VariableExpressionNode expressionNode) {
        BasicBlock current = function.getCurrentBlock();

        Value value = variables.get(expressionNode.getIdentifierNode().getName());

        if (value == null) {
            value = predefinedFunctions.stream()
                    .filter(f -> expressionNode.getIdentifierNode().getName().endsWith(f.getName()))
                    .findFirst()
                    .orElse(null);
        }

        if (value instanceof Function) {
            return value;
        } else {
            Command command = new Command(createTempVariable(matchType(
                    expressionNode.getResultType())),
                    LOAD,
                    List.of(value));
            current.addCommand(command);

            return command.getResult();
        }
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
        Value left = translateLeftValue(function, expressionNode.getLeft());
        Value right = translateExpression(function, expressionNode.getRight());

        Command command = new Command(left, STORE, List.of(right));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        return command.getResult();
    }

    private Value translateLeftValue(Function function, ExpressionNode left) {
        if (left instanceof VariableExpressionNode) {
            VariableExpressionNode variableExpressionNode = (VariableExpressionNode) left;
            return variables.get(variableExpressionNode.getIdentifierNode().getName());
        } else if (left instanceof ArrayAccessExpressionNode) {
            ArrayAccessExpressionNode expressionNode = (ArrayAccessExpressionNode) left;
            Value arrayValue = translateExpression(function, expressionNode.getArray());
            Value offsetValue = translateExpression(function, expressionNode.getArgument());

            Command command = new Command(
                    createTempVariable(matchType(expressionNode.getResultType())),
                    ARRAY_REFERENCE,
                    List.of(arrayValue, offsetValue));

            BasicBlock current = function.getCurrentBlock();
            current.addCommand(command);
            return command.getResult();
        } else if (left instanceof FieldAccessExpressionNode) {
            FieldAccessExpressionNode fieldAccessExpressionNode = (FieldAccessExpressionNode) left;

            Value structValue = translateExpression(function, fieldAccessExpressionNode.getLeft());

            PointerType pointerType = (PointerType) structValue.getType();
            StructType structType = (StructType) pointerType.getType();
            int index = 0;
            String name = fieldAccessExpressionNode.getRight().getIdentifierNode().getName();

            for (int i = 0; i < structType.getTypes().size(); i++) {
                if (structType.getTypes().get(i).getName().equals(name)) {
                    index = i;
                    break;
                }
            }

            Command command = new Command(
                    createTempVariable(matchType(fieldAccessExpressionNode.getResultType())),
                    FIELD_ACCESS,
                    List.of(structValue, new IntValue(index)));

            BasicBlock current = function.getCurrentBlock();
            current.addCommand(command);
            return command.getResult();
        }
        return null;
    }

    private Value createTempVariable(Type type) {
        return new LocalVariableValue(
                TEMP_VARIABLE_COUNT++,
                type);
    }
}
