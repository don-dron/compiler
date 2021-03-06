package lang.ir.translate;

import lang.ast.*;
import lang.ast.expression.*;
import lang.ast.expression.binary.*;
import lang.ast.expression.consts.*;
import lang.ast.expression.unary.postfix.*;
import lang.ast.expression.unary.prefix.CastExpressionNode;
import lang.ast.expression.unary.prefix.PrefixDecrementSubtractionExpressionNode;
import lang.ast.expression.unary.prefix.PrefixIncrementAdditiveExpressionNode;
import lang.ast.expression.unary.prefix.PrefixIncrementMultiplicativeExpressionNode;
import lang.ast.statement.*;
import lang.ir.Module;
import lang.ir.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lang.ir.Operation.*;
import static lang.ir.Type.*;

public class Translator {
    private static final String ARRAY_PREFIX = "$_array_";
    private static final int ARRAY_CODE = -1;

    private final Program program;
    private int LITERAL_COUNT = 0;
    private int THIS_COUNT = 0;
    private int RET_COUNT = 0;
    private int CONSTRUCTOR_COUNT = 0;
    private int REF_COUNTER_COUNT = 0;
    private int TYPE_COUNTER_COUNT = 0;
    private int TEMP_VARIABLE_COUNT = 0;
    private int STRUCT_ID_COUNTER = 0;
    private int TEMP_LEFT_COUNT = 0;
    private int ARRAY_SIZE_COUNT = 0;
    private int DESTRUCTOR_COUNT = 0;
    private int ARRAY_DESTRUCTOR_COUNT = 0;

    private final List<VariableValue> globalVars;
    private final List<StringValue> literals;
    private final Map<String, StructType> classes;
    private final Map<String, Value> variables;
    private final Map<WhileStatementNode, BasicBlock> whileToConditionBlock;
    private final Map<WhileStatementNode, BasicBlock> whileToMergeBlock;
    private Map<ConstructorDefinitionNode, Function> constructors;
    private Map<String, Function> destructors;
    private Map<String, String> structToDestructors;
    private Map<String, String> arrayDestructors;
    private final List<Function> predefinedFunctions;

    private final StructType commonStruct = new StructType(
            "$$$_commmon_struct",
            List.of(
                    new VariableValue("$$_counter", INT_32),
                    new VariableValue("$$_struct_type", INT_32)
            ),
            0L
    );

    private final StructType commonArray = new StructType(
            "$$$_commmon_array",
            List.of(
                    new VariableValue("$$_counter", INT_32),
                    new VariableValue("$$_struct_type", INT_32),
                    new VariableValue("$$_length", INT_32),
                    new VariableValue("$$_dummy", INT_32)
            ),
            1L
    );

    private final Function addInc = translateCommonAddIncFunction("$$$_add_inc");

    private final LinkedHashMap<AstNode, List<VariableValue>> currentDestructors;

    private VariableValue destructorsArray;
    private Function destructorTemplate;
    private Function commonDestructor;

    public Translator(Program program) {
        this.program = program;
        literals = new ArrayList<>();
        classes = new LinkedHashMap<>();
        variables = new LinkedHashMap<>();
        whileToConditionBlock = new LinkedHashMap<>();
        whileToMergeBlock = new LinkedHashMap<>();
        predefinedFunctions = new ArrayList<>();
        destructors = new LinkedHashMap<>();
        structToDestructors = new LinkedHashMap<>();
        currentDestructors = new LinkedHashMap<>();
        globalVars = new ArrayList<>();
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

    private Function getFreeFunction() {
        Function function = new Function("free", true);
        function.setParameterTypes(List.of(new PointerType(INT_32)));
        function.setResultType(VOID);
        return function;
    }

    private Function getYieldFunction() {
        Function function = new Function("__yield", true);
        function.setParameterTypes(List.of());
        function.setResultType(VOID);
        return function;
    }

    private Function getSleepFunction() {
        Function function = new Function("__sleep", true);
        function.setParameterTypes(List.of(INT_32));
        function.setResultType(VOID);
        return function;
    }

    private Function getJoinFunction() {
        Function function = new Function("__join", true);
        function.setParameterTypes(List.of(INT_64));
        function.setResultType(VOID);
        return function;
    }

    private Function getCreateFiberFunction() {
        Function function = new Function("__create_fiber", true);
        function.setParameterTypes(List.of(new PointerType(new FunctionType(VOID, List.of(VOID)))));
        function.setResultType(INT_64);
        return function;
    }

    private Function getDeleteFiberFunction() {
        Function function = new Function("__delete_fiber", true);
        function.setParameterTypes(List.of(INT_64));
        function.setResultType(VOID);
        return function;
    }

    private Function getCasFunction() {
        Function function = new Function("__cas", true);
        function.setParameterTypes(List.of(INT_64, INT_64, INT_64));
        function.setResultType(INT_32);
        return function;
    }

    private Function getExchangeFunction() {
        Function function = new Function("__exchange", true);
        function.setParameterTypes(List.of(INT_64, INT_64));
        function.setResultType(INT_64);
        return function;
    }

    private Function getLoadFunction() {
        Function function = new Function("__load", true);
        function.setParameterTypes(List.of(INT_64));
        function.setResultType(INT_64);
        return function;
    }

    private Function getStoreFunction() {
        Function function = new Function("__store", true);
        function.setParameterTypes(List.of(INT_64, INT_64));
        function.setResultType(VOID);
        return function;
    }

    public Module translate() {
        predefinedFunctions.add(getPutStringFunction());
        predefinedFunctions.add(getPutcharFunction());
        predefinedFunctions.add(getGetcharFunction());
        predefinedFunctions.add(getMallocFunction());
        predefinedFunctions.add(getFreeFunction());
        predefinedFunctions.add(getJoinFunction());
        predefinedFunctions.add(getCreateFiberFunction());
        predefinedFunctions.add(getSleepFunction());
        predefinedFunctions.add(getYieldFunction());
        predefinedFunctions.add(getDeleteFiberFunction());
        predefinedFunctions.add(getCasFunction());
        predefinedFunctions.add(getExchangeFunction());
        predefinedFunctions.add(getLoadFunction());
        predefinedFunctions.add(getStoreFunction());


        predefinedFunctions
                .forEach(f -> {
                    variables.put(f.getName(), f);
                });

        classes.put(commonStruct.getName(), commonStruct);
        STRUCT_ID_COUNTER++;

        classes.put(commonArray.getName(), commonArray);
        STRUCT_ID_COUNTER++;

        program.getClasses()
                .forEach(this::translateClassDeclaration);
        program.getClasses()
                .forEach(this::translateClassDefinition);

        program.getGlobalVariables()
                .forEach(declarationStatementNode -> {
                    GlobalVariableValue globalVariableValue = new GlobalVariableValue(
                            declarationStatementNode.getIdentifierNode().getName(),
                            matchType(declarationStatementNode.getTypeNode()),
                            getDefaultValue(matchType(declarationStatementNode.getTypeNode()))
                    );
                    variables.put(declarationStatementNode.getIdentifierNode().getName(), globalVariableValue);
                    globalVars.add(globalVariableValue);
                });


        Map<Function, FunctionDefinitionNode> functionToStatement = program.getFunctions()
                .stream()
                .collect(Collectors.toMap(
                        functionDefinitionNode -> {
                            String name = functionDefinitionNode.getIdentifierNode().getName();

                            if (program.getMainFunction().getIdentifierNode().getName()
                                    .equals(functionDefinitionNode.getIdentifierNode().getName())) {
                                name = "lang_main";
                            }

                            Function function = new Function(name);
                            variables.put(name, function);
                            return function;
                        },
                        java.util.function.Function.identity()));

        // array with pointer

        // array with primitive

        // for classes

        // common destruct
        translateCommonDestructorDeclaration();

        // common struct
        translateCommonStructDestructorDeclaration();
        translateCommonStructDestructorDefinition();

        // common array
        translateArrayDestructorDeclaration();
        translateArrayDestructorDefinition();

        program.getClasses()
                .forEach(this::translateDestructorDeclaration);

        program.getClasses()
                .forEach(this::translateDestructorDefinition);

        destructorsArray = new VariableValue(
                "$$$_destructors_array",
                new DestructorsArrayType(INT_64,
                        destructors.size(),
                        new ArrayList<>(destructors.values())),
                true
        );
        globalVars.add(destructorsArray);

        translateCommonDestructorDefinition();


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
        functions.add(commonDestructor);
        functions.addAll(predefinedFunctions);
        functions.addAll(destructors.values());
        functions.add(addInc);

        return new Module(
                new ArrayList<>(classes.values()),
                functions,
                literals,
                globalVars);
    }

    private void translateClassDeclaration(ClassStatementNode cl) {
        List<VariableValue> values = new ArrayList<>();

        StructType structType = new StructType(
                cl.getIdentifierNode().getName(),
                values,
                ++STRUCT_ID_COUNTER);

        classes.put(cl.getIdentifierNode().getName(), structType);
    }

    private void translateClassDefinition(ClassStatementNode cl) {
        List<VariableValue> values = classes.get(cl.getIdentifierNode().getName()).getTypes();

        values.add(new VariableValue("$$_counter_" + REF_COUNTER_COUNT++, INT_32));
        values.add(new VariableValue("$$_type_" + TYPE_COUNTER_COUNT++, INT_32));

        for (DeclarationStatementNode field : cl.getFields()) {
            VariableValue variableValue = new VariableValue(
                    field.getIdentifierNode().getName(),
                    matchType(field.getTypeNode()));
            values.add(variableValue);
        }
    }

    private void translateCommonDestructorDeclaration() {
        String name = "$_common_destructor";

        commonDestructor = new Function(name);
        List<Type> parameterTypes = new ArrayList<>();
        Type type = new PointerType(INT_32);
        parameterTypes.add(type);

        commonDestructor.setParameterTypes(parameterTypes);
        commonDestructor.setResultType(VOID);
    }

    private void translateCommonDestructorDefinition() {
        Function function = commonDestructor;
        BasicBlock header = function.appendBlock("header");

        Type targetType = new PointerType(commonStruct);

        String thisName = "$$_this_value_" + THIS_COUNT++;
        VariableValue thisValue = new VariableValue(
                thisName,
                targetType
        );
        variables.put(thisName, thisValue);

        function.setThisValue(thisValue);
        Command alloc = new Command(
                thisValue,
                ALLOC,
                List.of()
        );

        header.addCommand(alloc);

        Value temp = createTempVariable(new PointerType(INT_32));
        Command castToArray = new Command(
                createTempVariable(targetType),
                CAST,
                List.of(temp)
        );
        header.addCommand(castToArray);

        Command command = new Command(
                thisValue,
                STORE,
                List.of(castToArray.getResult())
        );
        header.addCommand(command);

        BasicBlock returnBlock = function.appendBlock("return");
        returnBlock.setTerminator(new Return(null));


        function.setReturnBlock(returnBlock);

        BasicBlock entry = function.appendBlock("check_null_entry");
        createBranch(header, entry);

        {
            Command nullLoadObject = new Command(
                    createTempVariable(targetType),
                    LOAD,
                    List.of(thisValue)
            );
            entry.addCommand(nullLoadObject);

            Command condition1 = new Command(
                    createTempVariable(INT_1),
                    NE,
                    List.of(nullLoadObject.getResult(), new NullValue(thisValue.getType()))
            );
            entry.addCommand(condition1);

            BasicBlock deletableCond = function.appendBlock("check_deletable_Cond");

            Command isDeletable = new Command(
                    createTempVariable(INT_32),
                    FIELD_ACCESS,
                    List.of(nullLoadObject.getResult(), new IntValue(1))
            );
            function.getCurrentBlock().addCommand(isDeletable);

            Command isDeletableLoad = new Command(
                    createTempVariable(INT_32),
                    LOAD,
                    List.of(isDeletable.getResult())
            );
            function.getCurrentBlock().addCommand(isDeletableLoad);

            Command condition3 = new Command(
                    createTempVariable(INT_1),
                    NE,
                    List.of(isDeletableLoad.getResult(), new IntValue(0))
            );
            function.getCurrentBlock().addCommand(condition3);

            {
                BasicBlock nullBody = function.appendBlock("check_null_body");

                {
                    Command loadObject = new Command(
                            createTempVariable(targetType),
                            LOAD,
                            List.of(thisValue)
                    );
                    nullBody.addCommand(loadObject);

                    Command countAccess = new Command(
                            createTempVariable(INT_32),
                            FIELD_ACCESS,
                            List.of(loadObject.getResult(), new IntValue(0))
                    );
                    nullBody.addCommand(countAccess);

                    Command loadCount = new Command(
                            createTempVariable(INT_32),
                            LOAD,
                            List.of(countAccess.getResult())
                    );
                    nullBody.addCommand(loadCount);

                    Command decCount = new Command(
                            createTempVariable(INT_32),
                            SUB,
                            List.of(loadCount.getResult(), new IntValue(1))
                    );
                    nullBody.addCommand(decCount);

                    Command storeCount = new Command(
                            countAccess.getResult(),
                            STORE,
                            List.of(decCount.getResult())
                    );
                    nullBody.addCommand(storeCount);

                    BasicBlock ifCond = function.appendBlock("destructor_if_cond");
                    createBranch(nullBody, ifCond);

                    Command condition2 = new Command(
                            createTempVariable(INT_1),
                            LE,
                            List.of(decCount.getResult(), new IntValue(0))
                    );

                    ifCond.addCommand(condition2);

                    BasicBlock ifBody = function.appendBlock("destructor_if_body");
                    BasicBlock prev = function.getCurrentBlock();
                    BasicBlock destructBlock = function.appendBlock("end_function_destructor");
                    createBranch(prev, destructBlock);

                    Command loadStructId = new Command(
                            createTempVariable(INT_32),
                            FIELD_ACCESS,
                            List.of(loadObject.getResult(), new IntValue(1))
                    );
                    function.getCurrentBlock().addCommand(loadStructId);

                    Command structIdLoad = new Command(
                            createTempVariable(INT_32),
                            LOAD,
                            List.of(loadStructId.getResult())
                    );
                    function.getCurrentBlock().addCommand(structIdLoad);

                    Command structMinus = new Command(
                            createTempVariable(INT_32),
                            SUB,
                            List.of(structIdLoad.getResult(), new IntValue(1))
                    );
                    function.getCurrentBlock().addCommand(structMinus);

                    Command accessFunction = new Command(
                            createTempVariable(new PointerType(destructorTemplate.getFunctionType())),
                            FIELD_ACCESS,
                            List.of(new VariableValue(
                                    destructorsArray.getName(),
                                    new PointerType(destructorsArray.getType()),
                                    destructorsArray.isGlobal()
                            ), structMinus.getResult())
                    );
                    function.getCurrentBlock().addCommand(accessFunction);


                    Command loadFunction = new Command(
                            createTempVariable(new PointerType(destructorTemplate.getFunctionType())),
                            LOAD,
                            List.of(accessFunction.getResult())
                    );
                    function.getCurrentBlock().addCommand(loadFunction);


                    Command copyCall = new Command(
                            null,
                            CALL,
                            List.of(loadFunction.getResult(), temp)
                    );
                    function.getCurrentBlock().addCommand(copyCall);

                    createConditionalBranch(ifCond, condition2.getResult(), ifBody, returnBlock);
                }

                createConditionalBranch(deletableCond, condition3.getResult(), nullBody, returnBlock);
            }

            createConditionalBranch(entry, condition1.getResult(), deletableCond, returnBlock);
        }

        createBranch(function.getCurrentBlock(), returnBlock);

        TEMP_VARIABLE_COUNT = 0;
    }

    private void translateCommonStructDestructorDeclaration() {
        String name = "$_common_struct_destructor_";

        Function function = new Function(name);
        destructors.put(name, function);
        variables.put(name, function);

        destructorTemplate = function;
    }

    private void translateCommonStructDestructorDefinition() {
        Function function = destructors.get("$_common_struct_destructor_");

        BasicBlock header = function.appendBlock("header");
        List<Type> parameterTypes = new ArrayList<>();

        Type type = new PointerType(INT_32);
        parameterTypes.add(type);

        String thisName = "$$_this_value_" + THIS_COUNT++;
        VariableValue thisValue = new VariableValue(
                thisName,
                new PointerType(commonStruct)
        );
        variables.put(thisName, thisValue);

        function.setThisValue(thisValue);
        Command alloc = new Command(
                thisValue,
                ALLOC,
                List.of()
        );

        header.addCommand(alloc);

        Value temp = createTempVariable(type);
        Command castToArray = new Command(
                createTempVariable(new PointerType(commonStruct)),
                CAST,
                List.of(temp)
        );
        header.addCommand(castToArray);

        Command command = new Command(
                thisValue,
                STORE,
                List.of(castToArray.getResult())
        );
        header.addCommand(command);

        BasicBlock returnBlock = function.appendBlock("return");
        returnBlock.setTerminator(new Return(null));

        function.setParameterTypes(parameterTypes);
        function.setResultType(VOID);
        function.setReturnBlock(returnBlock);

        BasicBlock destructBlock = function.appendBlock("_common_array_destructor_start_");
        createBranch(header, destructBlock);

        Command freeCommand = new Command(
                null,
                STRUCT_FREE,
                List.of(temp)
        );
        function.getCurrentBlock().addCommand(freeCommand);

        createBranch(function.getCurrentBlock(), returnBlock);

        TEMP_VARIABLE_COUNT = 0;
    }


    private void translateArrayDestructorDeclaration() {
        String name = "$_common_array_destructor_";

        Function function = new Function(name);
        destructors.put(name, function);
        variables.put(name, function);
    }

    private void translateArrayDestructorDefinition() {
        Function function = destructors.get("$_common_array_destructor_");

        BasicBlock header = function.appendBlock("header");
        List<Type> parameterTypes = new ArrayList<>();

        PointerType type = new PointerType(INT_32);
        parameterTypes.add(type);

        String thisName = "$$_this_value_" + THIS_COUNT++;
        VariableValue thisValue = new VariableValue(
                thisName,
                new PointerType(commonArray)
        );
        variables.put(thisName, thisValue);

        function.setThisValue(thisValue);
        Command alloc = new Command(
                thisValue,
                ALLOC,
                List.of()
        );

        header.addCommand(alloc);

        Value temp = createTempVariable(type);
        Command castToArray = new Command(
                createTempVariable(new PointerType(commonArray)),
                CAST,
                List.of(temp)
        );
        header.addCommand(castToArray);

        Command command = new Command(
                thisValue,
                STORE,
                List.of(castToArray.getResult())
        );
        header.addCommand(command);

        BasicBlock returnBlock = function.appendBlock("return");
        returnBlock.setTerminator(new Return(null));

        function.setParameterTypes(parameterTypes);
        function.setResultType(VOID);
        function.setReturnBlock(returnBlock);

        BasicBlock destructBlock = function.appendBlock("_common_array_destructor_start_");
        createBranch(header, destructBlock);

        Command loadObject = new Command(
                createTempVariable(new PointerType(commonArray)),
                LOAD,
                List.of(thisValue)
        );
        function.getCurrentBlock().addCommand(loadObject);

        Command readSize = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(loadObject.getResult(), new IntValue(2))
        );
        function.getCurrentBlock().addCommand(readSize);

        Command loadSize = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(readSize.getResult())
        );
        function.getCurrentBlock().addCommand(loadSize);

        Command castForAdd = new Command(
                createTempVariable(INT_64),
                PTRTOINT,
                List.of(loadObject.getResult())
        );
        function.getCurrentBlock().addCommand(castForAdd);

        Command withHeaderAccess = new Command(
                createTempVariable(INT_64),
                ADD,
                List.of(castForAdd.getResult(), new IntValue(commonArray.getSize()))
        );
        function.getCurrentBlock().addCommand(withHeaderAccess);

        VariableValue iterator = new VariableValue("$alloc_iterator_" + ARRAY_SIZE_COUNT, INT_32);

        ARRAY_SIZE_COUNT++;
        Command allocIterator = new Command(
                iterator,
                ALLOC,
                List.of()
        );
        function.getCurrentBlock().addCommand(allocIterator);

        Command allocIteratorXor = new Command(
                iterator,
                STORE,
                List.of(new IntValue(0))
        );
        function.getCurrentBlock().addCommand(allocIteratorXor);

        BasicBlock prev = function.getCurrentBlock();
        BasicBlock whileCond = function.appendBlock("array_destructor_cond");
        createBranch(prev, whileCond);

        Command castReverse = new Command(
                createTempVariable(new PointerType(new PointerType(commonArray))),
                INTTOPTR,
                List.of(withHeaderAccess.getResult())
        );
        function.getCurrentBlock().addCommand(castReverse);

        Command readIter = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(iterator)
        );
        function.getCurrentBlock().addCommand(readIter);

        Command cond = new Command(
                createTempVariable(INT_1),
                LT,
                List.of(readIter.getResult(), loadSize.getResult())
        );
        function.getCurrentBlock().addCommand(cond);

        BasicBlock whileBody = function.appendBlock("array_destructor_body_1");

        Command arrayRead = new Command(
                createTempVariable(new PointerType(commonArray)),
                ARRAY_ACCESS,
                List.of(castReverse.getResult(), readIter.getResult()));
        function.getCurrentBlock().addCommand(arrayRead);

        Command load = new Command(
                createTempVariable(new PointerType(commonArray)),
                LOAD,
                List.of(arrayRead.getResult())
        );

        function.getCurrentBlock().addCommand(load);

        addDestructorCall(function, type, load.getResult());
        Command addIter = new Command(
                createTempVariable(INT_32),
                ADD,
                List.of(readIter.getResult(), new IntValue(1))
        );
        function.getCurrentBlock().addCommand(addIter);

        Command storeIter = new Command(
                iterator,
                STORE,
                List.of(addIter.getResult())
        );
        function.getCurrentBlock().addCommand(storeIter);

        BasicBlock whileMerge = function.appendBlock("array_destructor_merge_1");
        createBranch(whileBody, whileCond);
        createConditionalBranch(whileCond, cond.getResult(), whileBody, whileMerge);


        Command castCommand = new Command(
                createTempVariable(new PointerType(INT_32)),
                CAST,
                List.of(loadObject.getResult())
        );
        function.getCurrentBlock().addCommand(castCommand);

        Command freeCommand = new Command(
                null,
                STRUCT_FREE,
                List.of(castCommand.getResult())
        );
        function.getCurrentBlock().addCommand(freeCommand);

        createBranch(function.getCurrentBlock(), returnBlock);

        TEMP_VARIABLE_COUNT = 0;
    }

    private void translateDestructorDeclaration(ClassStatementNode classStatementNode) {
        String name = "$_destructor_" + DESTRUCTOR_COUNT++;

        Function function = new Function(name);
        destructors.put(name, function);
        structToDestructors.put(classStatementNode.getIdentifierNode().getName(), name);
    }

    private void translateDestructorDefinition(ClassStatementNode classStatementNode) {
        assert classStatementNode != null;

        Function function =
                destructors.get(
                        structToDestructors.get(classStatementNode.getIdentifierNode().getName()));

        BasicBlock header = function.appendBlock("header");
        List<Type> parameterTypes = new ArrayList<>();

        Type type = new PointerType(INT_32);
        parameterTypes.add(type);

        String thisName = "$$_this_value_" + THIS_COUNT++;
        Type targetType = matchType(new ObjectTypeNode(classStatementNode.getIdentifierNode()));

        VariableValue thisValue = new VariableValue(
                thisName,
                targetType
        );
        variables.put(thisName, thisValue);

        function.setThisValue(thisValue);
        Command alloc = new Command(
                thisValue,
                ALLOC,
                List.of()
        );

        header.addCommand(alloc);

        Value temp = createTempVariable(type);
        Command castToArray = new Command(
                createTempVariable(targetType),
                CAST,
                List.of(temp)
        );
        header.addCommand(castToArray);

        Command command = new Command(
                thisValue,
                STORE,
                List.of(castToArray.getResult())
        );
        header.addCommand(command);

        BasicBlock returnBlock = function.appendBlock("return");
        returnBlock.setTerminator(new Return(null));


        function.setParameterTypes(parameterTypes);
        function.setResultType(VOID);
        function.setReturnBlock(returnBlock);

        BasicBlock entry = function.appendBlock("check_null_entry");
        createBranch(header, entry);


        Command loadObject = new Command(
                createTempVariable(targetType),
                LOAD,
                List.of(thisValue)
        );
        function.getCurrentBlock().addCommand(loadObject);

        BasicBlock prev = function.getCurrentBlock();
        BasicBlock destructBlock = function.appendBlock("end_function_destructor");
        createBranch(prev, destructBlock);


        String internalDestructor = classStatementNode.getTranslationNode().getStatements()
                .stream()
                .filter(it -> it instanceof FunctionDefinitionNode)
                .filter(it -> ((FunctionDefinitionNode) it).getIdentifierNode()
                        .getName().endsWith("__internal_destruct"))
                .map(it -> ((FunctionDefinitionNode) it).getIdentifierNode()
                        .getName())
                .findFirst()
                .orElse(null);

        if (internalDestructor != null) {
            Value functionValue = variables.get(internalDestructor);

            Command loadObjectt = new Command(
                    createTempVariable(targetType),
                    LOAD,
                    List.of(thisValue)
            );
            function.getCurrentBlock().addCommand(loadObjectt);

            Command castCommand = new Command(
                    createTempVariable(new PointerType(commonStruct)),
                    CAST,
                    List.of(loadObjectt.getResult())
            );
            function.getCurrentBlock().addCommand(castCommand);

            Command commandt = new Command(null, CALL, List.of(functionValue, loadObjectt.getResult()));
            function.getCurrentBlock().addCommand(commandt);
        }
        StructType structType = classes.get(classStatementNode.getIdentifierNode().getName());
        for (VariableValue allocated : structType.getTypes()) {
            if (allocated.getType() instanceof PointerType && !(allocated.getType().getType() instanceof FunctionType)) {
                PointerType pointerType = (PointerType) allocated.getType();

                BasicBlock last = function.getCurrentBlock();
                BasicBlock deleteFieldIfCond = function.appendBlock("end_function_destructor_if_cond");
                createBranch(last, deleteFieldIfCond);

                int index = 0;
                String fieldName = allocated.getName();

                for (int i = 0; i < structType.getTypes().size(); i++) {
                    if (structType.getTypes().get(i).getName().equals(fieldName)) {
                        index = i;
                        break;
                    }
                }

                Command fieldAccess = new Command(
                        createTempVariable(allocated.getType()),
                        FIELD_ACCESS,
                        List.of(loadObject.getResult(), new IntValue(index)));
                deleteFieldIfCond.addCommand(fieldAccess);

                Command forDelete = new Command(
                        createTempVariable(allocated.getType()),
                        LOAD,
                        List.of(fieldAccess.getResult())
                );
                deleteFieldIfCond.addCommand(forDelete);

                addDestructorCall(function, (PointerType) forDelete.getResult().getType(), forDelete.getResult());
            }
        }


        Command freeCommand = new Command(
                null,
                STRUCT_FREE,
                List.of(temp)
        );
        function.getCurrentBlock().addCommand(freeCommand);

        createBranch(function.getCurrentBlock(), returnBlock);
        TEMP_VARIABLE_COUNT = 0;
    }

    private Value getDefaultValue(Type type) {
        if (type == INT_1 || type == INT_8) {
            return new CharValue((char) 0);
        } else if (type == INT_32) {
            return new IntValue(0);
        } else if (type == INT_64) {
            return new LongValue(0L);
        } else {
            return new NullValue(type);
        }
    }

    private Function translateFunction(Function function, FunctionDefinitionNode functionDefinitionNode) {
        BasicBlock header = function.appendBlock("header");

        List<Type> parameterTypes = new ArrayList<>();

        String thisName = null;
        if (functionDefinitionNode.getScope() != null
                && functionDefinitionNode.getScope().getOwner() instanceof ClassStatementNode
                && !constructors.containsValue(function)) {
            ClassStatementNode classStatementNode = (ClassStatementNode)
                    functionDefinitionNode.getScope().getOwner();
            Type type = matchType(new ObjectTypeNode(classStatementNode.getIdentifierNode()));
            parameterTypes.add(type);

            thisName = "$$_this_value_" + THIS_COUNT++;
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
        currentDestructors.put(functionDefinitionNode, new ArrayList<>());

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

            if (thisName == null || !variableValue.getName().equals(thisName)) {
                currentDestructors.get(functionDefinitionNode).add(variableValue);
            }

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

            Value returnVariableValue = new VariableValue(name, type);
            variables.put(name, returnVariableValue);

            Command alloc = new Command(
                    returnVariableValue,
                    ALLOC,
                    List.of()
            );
            header.addCommand(alloc);

            if (constructors.containsValue(function)) {
                thisName = "$$_this_value_" + THIS_COUNT++;
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
                        returnVariableValue,
                        STORE,
                        List.of(castCommand.getResult())
                );
                function.getCurrentBlock().addCommand(storeRet);

                StructType structType = (StructType) pointerStructType.getType();

                Command thisAccess = new Command(
                        createTempVariable(type),
                        LOAD,
                        List.of(thisVariableValue)
                );
                function.getCurrentBlock().addCommand(thisAccess);

                for (int i = 0; i < structType.getTypes().size(); i++) {
                    Command fieldAccess = new Command(
                            createTempVariable(structType.getTypes().get(i).getType()),
                            FIELD_ACCESS,
                            List.of(thisAccess.getResult(), new IntValue(i))
                    );
                    function.getCurrentBlock().addCommand(fieldAccess);

                    if (structType.getTypes().get(i).getType() instanceof PointerType) {
                        Command writeCommand = new Command(
                                fieldAccess.getResult(),
                                STORE,
                                List.of(new NullValue(fieldAccess.getResult().getType()))
                        );
                        function.getCurrentBlock().addCommand(writeCommand);
                    } else {
                        Command writeCommand = new Command(
                                fieldAccess.getResult(),
                                STORE,
                                List.of(getDefaultValue(fieldAccess.getResult().getType()))
                        );
                        function.getCurrentBlock().addCommand(writeCommand);
                    }
                }

                Command fieldAccess = new Command(
                        createTempVariable(INT_32),
                        FIELD_ACCESS,
                        List.of(castCommand.getResult(), new IntValue(1)));
                function.getCurrentBlock().addCommand(fieldAccess);

                Command forDelete = new Command(
                        fieldAccess.getResult(),
                        STORE,
                        List.of(new IntValue((int) structType.getStructId()))
                );
                function.getCurrentBlock().addCommand(forDelete);
            }

            returnBlock = function.appendBlock("return");

            Command loadReturn = new Command(
                    createTempVariable(type),
                    LOAD,
                    List.of(returnVariableValue)
            );
            function.getCurrentBlock().addCommand(loadReturn);

            if (loadReturn.getResult().getType() instanceof PointerType) {
                translateObjectIncCountCall(function, loadReturn.getResult(),
                        (PointerType) returnVariableValue.getType(), false);
            }

            function.setReturnValue(returnVariableValue);
            function.getCurrentBlock().setTerminator(new Return(loadReturn.getResult()));
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

        BasicBlock prev = function.getCurrentBlock();
        BasicBlock destructBlock = function.appendBlock("end_function_destructor");
        createBranch(prev, destructBlock);

        BasicBlock finalReturnBlock = returnBlock;

        function.getBlocks().stream()
                .filter(bb -> bb.getTerminator() instanceof Branch &&
                        ((Branch) bb.getTerminator()).getTarget().equals(finalReturnBlock))
                .forEach(bb -> {
                    bb.getOutput().remove(((Branch) bb.getTerminator()).getTarget());
                    ((Branch) bb.getTerminator()).getTarget().getInput().remove(bb);
                    createBranch(bb, destructBlock);
                });

        addDestructorsList(function,
                currentDestructors.getOrDefault(functionDefinitionNode, new ArrayList<>()));
        currentDestructors.remove(functionDefinitionNode);

        createBranch(function.getCurrentBlock(), returnBlock);
        TEMP_VARIABLE_COUNT = 0;
        return function;
    }

    private void addDestructorsList(Function function, List<VariableValue> destructors) {
        for (VariableValue allocated : destructors) {
            if (allocated.getType() instanceof PointerType
            && !(allocated.getType().getType() instanceof  FunctionType)) {
                Command forDelete = new Command(
                        createTempVariable(allocated.getType()),
                        LOAD,
                        List.of(allocated)
                );
                function.getCurrentBlock().addCommand(forDelete);

                addDestructorCall(function, (PointerType)
                        forDelete.getResult().getType(), forDelete.getResult());
            }
        }
    }

    public static String graphVizDebug(Function functionBlock, Integer clusterId) {
        StringBuilder s = new StringBuilder();

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            String body = basicBlock.getName()
                    + ":\n" +
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

        s
                .append("\n")
                .append("\tsubgraph")
                .append(" ")
                .append("cluster_" + (clusterId == null ? 0 : clusterId))
                .append(" ")
                .append("{\n")
                .append("\t\tstyle=filled;\n")
                .append("\t\tcolor=lightgrey;\n")
                .append("\t\tnode [style=filled,color=white];\n")
                .append("\t\tlabel = \"" + functionBlock.getName() + "\";\n");

        for (BasicBlock basicBlock : functionBlock.getBlocks()) {
            for (BasicBlock other : basicBlock.getOutput()) {
                s.append("\t\t\"")
                        .append(basicBlock.getName())
                        .append("\"")
                        .append(" -> ")
                        .append("\"")
                        .append(other.getName())
                        .append("\";\n");
            }
        }

        s.append("\t}");

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
        } else if (expressionNode instanceof LongConstantExpressionNode) {
            return translateLongConstantExpression(function, (LongConstantExpressionNode) expressionNode);
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
                        .map(exp -> {
                            Value v = translateExpression(function, exp);

                            if (v.getType() instanceof PointerType &&
                                    !(v.getType().getType() instanceof FunctionType)) {
                                Command castCommand = new Command(
                                        createTempVariable(new PointerType(commonStruct)),
                                        CAST,
                                        List.of(v)
                                );
                                function.getCurrentBlock().addCommand(castCommand);

                                Command copyCall = new Command(
                                        null,
                                        CALL,
                                        List.of(addInc, castCommand.getResult())
                                );
                                function.getCurrentBlock().addCommand(copyCall);
                            }
                            return v;
                        })
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
        currentDestructors.put(node, new ArrayList<>());

        node.getStatements().forEach(n -> translateStatement(function, n));

        addDestructorsList(function, currentDestructors.getOrDefault(node, new ArrayList<>()));
        currentDestructors.remove(node);
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

    private Function translateCommonAddIncFunction(String name) {
        Function function = new Function(name);
        BasicBlock header = function.appendBlock("header");
        List<Type> parameterTypes = new ArrayList<>();

        Type type = new PointerType(commonStruct);
        parameterTypes.add(type);

        String thisName = "$$_this_value_" + THIS_COUNT++;
        VariableValue thisValue = new VariableValue(
                thisName,
                type
        );

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
                List.of(createTempVariable(new PointerType(commonStruct)))
        );

        header.addCommand(command);

        BasicBlock returnBlock = function.appendBlock("return");
        returnBlock.setTerminator(new Return(null));

        function.setParameterTypes(parameterTypes);
        function.setResultType(VOID);
        function.setReturnBlock(returnBlock);

        BasicBlock entry = function.appendBlock("common_entry");
        createBranch(header, entry);

        Command commandLoad = new Command(
                createTempVariable(new PointerType(commonStruct)),
                LOAD,
                List.of(thisValue)
        );

        function.getCurrentBlock().addCommand(commandLoad);

        translateObjectIncCount(function, commandLoad.getResult(), (PointerType) type);

        createBranch(function.getCurrentBlock(), returnBlock);

        TEMP_VARIABLE_COUNT = 0;

        return function;
    }

    private void translateObjectIncCount(Function function,
                                         Value fieldAccess,
                                         PointerType pointerType) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock ifCond = function.appendBlock("assigment_inc_count_if_cond");
        createBranch(last, ifCond);

        Command condition = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(fieldAccess, new NullValue(pointerType.getType()))
        );
        ifCond.addCommand(condition);

        Command type = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(fieldAccess, new IntValue(1)));
        function.getCurrentBlock().addCommand(type);

        Command loadType = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(type.getResult())
        );
        function.getCurrentBlock().addCommand(loadType);

        Command condition4 = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(loadType.getResult(), new IntValue(0))
        );
        function.getCurrentBlock().addCommand(condition4);

        Command condAll = new Command(
                createTempVariable(INT_1),
                AND,
                List.of(condition.getResult(), condition4.getResult())
        );
        function.getCurrentBlock().addCommand(condAll);

        BasicBlock ifBody = function.appendBlock("assigment_inc_count_if_body");

        Command counter = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(fieldAccess, new IntValue(0)));
        function.getCurrentBlock().addCommand(counter);

        Command loadCount = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(counter.getResult())
        );
        function.getCurrentBlock().addCommand(loadCount);

        Command inc = new Command(
                createTempVariable(INT_32),
                ADD,
                List.of(loadCount.getResult(), new IntValue(1))
        );
        function.getCurrentBlock().addCommand(inc);

        Command storeCounter = new Command(
                counter.getResult(),
                STORE,
                List.of(inc.getResult())
        );
        function.getCurrentBlock().addCommand(storeCounter);

        BasicBlock ifMerge = function.appendBlock("assigment_inc_count_if_merge");
        createBranch(ifBody, ifMerge);
        createConditionalBranch(ifCond, condAll.getResult(), ifBody, ifMerge);
    }

    private void translateTempDeadChecker(Function function,
                                          Value fieldAccess,
                                          PointerType pointerType) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock ifCond = function.appendBlock("check_temp_cond_null");
        createBranch(last, ifCond);

        Command castToCommonStruct = new Command(
                createTempVariable(new PointerType(commonStruct)),
                CAST,
                List.of(fieldAccess)
        );
        function.getCurrentBlock().addCommand(castToCommonStruct);

        Command condition = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(castToCommonStruct.getResult(), new NullValue(pointerType.getType()))
        );

        function.getCurrentBlock().addCommand(condition);
        last = function.getCurrentBlock();

        BasicBlock nextCond = function.appendBlock("check_temp_cond_count");

        Command counter = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castToCommonStruct.getResult(), new IntValue(0)));
        function.getCurrentBlock().addCommand(counter);

        Command loadCount = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(counter.getResult())
        );
        function.getCurrentBlock().addCommand(loadCount);

        Command condition2 = new Command(
                createTempVariable(INT_1),
                EQ,
                List.of(loadCount.getResult(), new IntValue(0))
        );
        function.getCurrentBlock().addCommand(condition2);

        BasicBlock nextBody = function.appendBlock("check_temp_body");
        addDestructorCall(function, (PointerType) fieldAccess.getType(), castToCommonStruct.getResult());
        BasicBlock ifMerge = function.appendBlock("check_temp_merge");

        createBranch(nextBody, ifMerge);

        createConditionalBranch(nextCond, condition2.getResult(), nextBody, ifMerge);
        createConditionalBranch(ifCond, condition.getResult(), nextCond, ifMerge);
    }

    private void translateObjectIncCountCall(Function function,
                                             Value fieldAccess,
                                             PointerType pointerType,
                                             boolean returnExit) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock ifCond = function.appendBlock("start_cond");
        createBranch(last, ifCond);

        Command condition = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(fieldAccess, new NullValue(pointerType.getType()))
        );
        function.getCurrentBlock().addCommand(condition);

        Command castToArray = new Command(
                createTempVariable(new PointerType(commonStruct)),
                CAST,
                List.of(fieldAccess)
        );
        function.getCurrentBlock().addCommand(castToArray);

        Command counter = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castToArray.getResult(), new IntValue(0)));
        function.getCurrentBlock().addCommand(counter);

        Command loadCount = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(counter.getResult())
        );
        function.getCurrentBlock().addCommand(loadCount);

        Command condition2 = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(loadCount.getResult(), new IntValue(0))
        );
        function.getCurrentBlock().addCommand(condition2);

        Command type = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castToArray.getResult(), new IntValue(1)));
        function.getCurrentBlock().addCommand(type);

        Command loadType = new Command(
                createTempVariable(INT_32),
                LOAD,
                List.of(type.getResult())
        );
        function.getCurrentBlock().addCommand(loadType);

        Command condition4 = new Command(
                createTempVariable(INT_1),
                NE,
                List.of(loadType.getResult(), new IntValue(0))
        );
        function.getCurrentBlock().addCommand(condition4);

        Command condition3 = new Command(
                createTempVariable(INT_1),
                AND,
                List.of(condition.getResult(), condition2.getResult())
        );
        function.getCurrentBlock().addCommand(condition3);

        Command condAll = new Command(
                createTempVariable(INT_1),
                AND,
                List.of(condition3.getResult(), condition4.getResult())
        );
        function.getCurrentBlock().addCommand(condAll);

        BasicBlock body = function.appendBlock("body");

        {
            Command twoCounter = new Command(
                    createTempVariable(INT_32),
                    FIELD_ACCESS,
                    List.of(castToArray.getResult(), new IntValue(0)));
            function.getCurrentBlock().addCommand(twoCounter);

            Command twoLoadCount = new Command(
                    createTempVariable(INT_32),
                    LOAD,
                    List.of(twoCounter.getResult())
            );
            function.getCurrentBlock().addCommand(twoLoadCount);

            Command inc = new Command(
                    createTempVariable(INT_32),
                    returnExit ? ADD : SUB,
                    List.of(twoLoadCount.getResult(), new IntValue(1))
            );
            function.getCurrentBlock().addCommand(inc);

            Command storeCounter = new Command(
                    twoCounter.getResult(),
                    STORE,
                    List.of(inc.getResult())
            );
            function.getCurrentBlock().addCommand(storeCounter);
        }

        BasicBlock ifMerge = function.appendBlock("temp_merge");
        createBranch(body, ifMerge);
        createConditionalBranch(ifCond, condAll.getResult(), body, ifMerge);
    }


    private void addDestructorCall(Function function, PointerType pointerType, Value value) {
        if (function.getName().endsWith("__internal_destruct")) {
            return;
        }

        Command castTo32 = new Command(
                createTempVariable(new PointerType(INT_32)),
                CAST,
                List.of(value)
        );
        function.getCurrentBlock().addCommand(castTo32);

        Command destructorCall = new Command(
                null,
                CALL,
                List.of(commonDestructor, castTo32.getResult())
        );
        function.getCurrentBlock().addCommand(destructorCall);
    }

    private void translateExpressionStatement(Function function, ExpressionStatementNode node) {
        Value value = translateExpression(function, node.getExpressionNode());

        if (value != null
                && !(node.getExpressionNode() instanceof AssigmentExpressionNode)
                && value instanceof LocalVariableValue
                && value.getType() instanceof PointerType) {
            translateTempDeadChecker(function, value, (PointerType) value.getType());
        }
    }

    private void translateDeclaration(Function function, DeclarationStatementNode node) {
        VariableValue variableValue = new VariableValue(node.getIdentifierNode().getName(), matchType(node.getTypeNode()));
        variables.put(node.getIdentifierNode().getName(), variableValue);

        Command alloc = new Command(
                variableValue,
                ALLOC,
                List.of()
        );

        new ArrayList<>(currentDestructors.values())
                .get(currentDestructors.size() - 1)
                .add(variableValue);

        function.getCurrentBlock().addCommand(alloc);

        if (node.getExpressionNode() != null) {
            Value value = translateExpression(function, node.getExpressionNode());
            BasicBlock current = function.getCurrentBlock();
            current.addCommand(new Command(variableValue, STORE, List.of(value)));

            if (variableValue.getType() instanceof PointerType
                    && !(variableValue.getType().getType() instanceof FunctionType)) {
                Command castToCommon = new Command(
                        createTempVariable(new PointerType(commonStruct)),
                        CAST,
                        List.of(value)
                );
                function.getCurrentBlock().addCommand(castToCommon);

                Command copyCall = new Command(
                        null,
                        CALL,
                        List.of(addInc, castToCommon.getResult())
                );
                function.getCurrentBlock().addCommand(copyCall);
            }
        } else {
            if (variableValue.getType() instanceof PointerType) {
                Command command = new Command(
                        variableValue,
                        STORE,
                        List.of(new NullValue(variableValue.getType()))
                );
                function.getCurrentBlock().addCommand(command);
            } else {
                Command command = new Command(
                        variableValue,
                        STORE,
                        List.of(variableValue.getType() == INT_8 ? new CharValue((char) 0) : new IntValue(0))
                );
                function.getCurrentBlock().addCommand(command);
            }
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
        } else if (typeNode instanceof FunctionNode) {
            FunctionNode functionNode = (FunctionNode) typeNode;
            return new PointerType(new FunctionType(matchType(functionNode.getTypeNode()),
                    functionNode.getParametersNode()
                            .getParameters()
                            .stream()
                            .map(ParameterNode::getTypeNode)
                            .map(this::matchType)
                            .collect(Collectors.toList())));
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

            if (value.getType() instanceof PointerType) {
                translateObjectIncCountCall(function, value, (PointerType) value.getType(), true);
            }

            Command storeReturn = new Command(
                    function.getReturnValue(),
                    STORE,
                    List.of(value)
            );
            function.getCurrentBlock().addCommand(storeReturn);
        }

        Map.Entry<AstNode, List<VariableValue>> entry = new ArrayList<>(currentDestructors.entrySet())
                .get(currentDestructors.size() - 1);
        addDestructorsList(function,
                currentDestructors.values()
                        .stream()
                        .skip(1)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
        currentDestructors.remove(entry.getKey());


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

        if(value.getType() instanceof PointerType && !(type instanceof PointerType)) {
            Command command = new Command(createTempVariable(type), PTRTOINT, List.of(value));
            function.getCurrentBlock().addCommand(command);
            return command.getResult();
        }else if(!(value.getType() instanceof PointerType) && type instanceof PointerType) {
            Command command = new Command(createTempVariable(type), INTTOPTR, List.of(value));
            function.getCurrentBlock().addCommand(command);
            return command.getResult();
        }

        if (value.getType().getSize() < type.getSize()) {
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

        translateTempDeadChecker(function, structValue, (PointerType) structValue.getType());

        return load.getResult();
    }

    private Value translateArrayAccessExpression(Function function, ArrayAccessExpressionNode expressionNode) {
        Value arrayValue = translateExpression(function, expressionNode.getArray());
        Value offsetValue = translateExpression(function, expressionNode.getArgument());

        Command castForAdd = new Command(
                createTempVariable(INT_64),
                PTRTOINT,
                List.of(arrayValue)
        );
        function.getCurrentBlock().addCommand(castForAdd);

        Command withHeaderAccess = new Command(
                createTempVariable(INT_64),
                ADD,
                List.of(castForAdd.getResult(), new IntValue(commonArray.getSize()))
        );
        function.getCurrentBlock().addCommand(withHeaderAccess);

        Command castReverse = new Command(
                createTempVariable(arrayValue.getType()),
                INTTOPTR,
                List.of(withHeaderAccess.getResult())
        );
        function.getCurrentBlock().addCommand(castReverse);

        Command command = new Command(
                createTempVariable(matchType(expressionNode.getResultType())),
                ARRAY_ACCESS,
                List.of(castReverse.getResult(), offsetValue));

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
                            .map(exp -> {
                                Value v = translateExpression(function, exp);

                                if (v.getType() instanceof PointerType
                                        && !(v.getType().getType() instanceof FunctionType)) {
                                    Command castCommand = new Command(
                                            createTempVariable(new PointerType(commonStruct)),
                                            CAST,
                                            List.of(v)
                                    );
                                    function.getCurrentBlock().addCommand(castCommand);

                                    Command copyCall = new Command(
                                            null,
                                            CALL,
                                            List.of(addInc, castCommand.getResult())
                                    );
                                    function.getCurrentBlock().addCommand(copyCall);
                                }
                                return v;
                            })
                            .collect(Collectors.toList());
            values.add(0, leftValue);
            if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);

                return command.getResult();
            } else {
                Command command = new Command(null, CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);
                return command.getResult();
            }
        } else {
            Value functionValue = translateExpression(function, expressionNode.getFunction());
            List<Value> values =
                    expressionNode
                            .getParameters()
                            .getList()
                            .stream()
                            .map(exp -> {
                                Value v = translateExpression(function, exp);

                                if (v.getType() instanceof PointerType
                                        && !(v.getType().getType() instanceof FunctionType)) {
                                    Command castCommand = new Command(
                                            createTempVariable(new PointerType(commonStruct)),
                                            CAST,
                                            List.of(v)
                                    );
                                    function.getCurrentBlock().addCommand(castCommand);

                                    Command copyCall = new Command(
                                            null,
                                            CALL,
                                            List.of(addInc, castCommand.getResult())
                                    );
                                    function.getCurrentBlock().addCommand(copyCall);
                                }
                                return v;
                            })
                            .collect(Collectors.toList());
            if (!expressionNode.getResultType().equals(GlobalBasicType.VOID_TYPE)) {
                Command command = new Command(createTempVariable(matchType(expressionNode.getResultType())), CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);
                return command.getResult();
            } else {
                Command command = new Command(
                        functionValue.getType() == null || functionValue.getType().equals(VOID)
                                ? null
                                : createTempVariable(functionValue.getType()), CALL,
                        Stream.concat(Stream.of(functionValue), values.stream()).collect(Collectors.toList()));
                BasicBlock current = function.getCurrentBlock();
                current.addCommand(command);
                return command.getResult();
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

            Command withHeader = new Command(
                    createTempVariable(INT_32),
                    ADD,
                    List.of(new IntValue(commonArray.getSize()), trueSize.getResult())
            );
            function.getCurrentBlock().addCommand(withHeader);

            PointerType pointerType = new PointerType(INT_64);
            Command command = new Command(
                    createTempVariable(pointerType),
                    ARRAY_ALLOCATION,
                    List.of(pointerType, withHeader.getResult())
            );
            function.getCurrentBlock().addCommand(command);

            Command castToStruct = new Command(
                    createTempVariable(new PointerType(commonArray)),
                    CAST,
                    List.of(command.getResult())
            );
            function.getCurrentBlock().addCommand(castToStruct);

            {
                Command countAccess = new Command(
                        createTempVariable(INT_32),
                        FIELD_ACCESS,
                        List.of(castToStruct.getResult(), new IntValue(0))
                );
                function.getCurrentBlock().addCommand(countAccess);

                Command storeCount = new Command(
                        countAccess.getResult(),
                        STORE,
                        List.of(new IntValue(0))
                );
                function.getCurrentBlock().addCommand(storeCount);
            }

            {
                Command sizeAccess = new Command(
                        createTempVariable(INT_32),
                        FIELD_ACCESS,
                        List.of(castToStruct.getResult(), new IntValue(2))
                );
                function.getCurrentBlock().addCommand(sizeAccess);

                Command storeSize = new Command(
                        sizeAccess.getResult(),
                        STORE,
                        List.of(loadSize.getResult())
                );
                function.getCurrentBlock().addCommand(storeSize);
            }

            {
                Command typeAccess = new Command(
                        createTempVariable(INT_32),
                        FIELD_ACCESS,
                        List.of(castToStruct.getResult(), new IntValue(1))
                );
                function.getCurrentBlock().addCommand(typeAccess);

                Command typeStore = new Command(
                        typeAccess.getResult(),
                        STORE,
                        List.of(new IntValue(type.getType() instanceof PointerType ? 2 : 1))
                );
                function.getCurrentBlock().addCommand(typeStore);
            }


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

            Command accessOffset = new Command(
                    createTempVariable(iterator.getType()),
                    LOAD,
                    List.of(iterator)
            );
            function.getCurrentBlock().addCommand(accessOffset);

            Command accessArray = new Command(
                    createTempVariable(arrayValue.getType()),
                    LOAD,
                    List.of(arrayAccessVariable)
            );
            function.getCurrentBlock().addCommand(accessArray);

            Command castForAdd = new Command(
                    createTempVariable(INT_64),
                    PTRTOINT,
                    List.of(accessArray.getResult())
            );
            function.getCurrentBlock().addCommand(castForAdd);

            Command withHeaderAccess = new Command(
                    createTempVariable(INT_64),
                    ADD,
                    List.of(castForAdd.getResult(), new IntValue(commonArray.getSize()))
            );
            function.getCurrentBlock().addCommand(withHeaderAccess);

            Command castReverse = new Command(
                    createTempVariable(arrayValue.getType()),
                    INTTOPTR,
                    List.of(withHeaderAccess.getResult())
            );
            function.getCurrentBlock().addCommand(castReverse);

            Command arrayAccess = new Command(
                    createTempVariable(targetType.getType()),
                    ARRAY_ACCESS,
                    List.of(castReverse.getResult(), accessOffset.getResult()));
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

    private Value translateLongConstantExpression(Function function, LongConstantExpressionNode expressionNode) {
        return new LongValue(expressionNode.getValue());
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
                List.of(castCommand.getResult(), new IntValue(2)));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(struct);

        String v = expressionNode.getValue();
        int size = v.getBytes().length + commonArray.getSize();
        byte s[] = new byte[size];

        for (int i = 0; i < v.getBytes().length; i++) {
            s[i + commonArray.getSize()] = v.getBytes()[i];
        }

        byte[] bytes = ByteBuffer.allocate(4).putInt(-1).array();

        s[4] = bytes[0];
        s[5] = bytes[1];
        s[6] = bytes[2];
        s[7] = bytes[3];

        String vn = "\0\0\0\0".repeat(4) + v;
        StringValue value = new StringValue("@.str" + LITERAL_COUNT++, vn);
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
                List.of(castCommand.getResult(), new IntValue(3)));

        current = function.getCurrentBlock();
        current.addCommand(structSize);

        Command storeSize = new Command(
                structSize.getResult(),
                STORE,
                List.of(new IntValue(expressionNode.getValue().length())));
        function.getCurrentBlock().addCommand(storeSize);

        Command structType = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castCommand.getResult(), new IntValue(1)));
        function.getCurrentBlock().addCommand(structType);

        Command structTypeStore = new Command(
                structType.getResult(),
                STORE,
                List.of(new IntValue(1)));
        function.getCurrentBlock().addCommand(structTypeStore);

        Command structCount = new Command(
                createTempVariable(INT_32),
                FIELD_ACCESS,
                List.of(castCommand.getResult(), new IntValue(0)));
        function.getCurrentBlock().addCommand(structCount);

        Command structCountStore = new Command(
                structCount.getResult(),
                STORE,
                List.of(new IntValue(0)));
        function.getCurrentBlock().addCommand(structCountStore);

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
            if(value == null) {
                int a =1;
            }
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

        if (leftValue != null
                && leftValue.getType() instanceof PointerType) {
            translateTempDeadChecker(function, leftValue, (PointerType) leftValue.getType());
        }

        if (rightValue != null
                && rightValue.getType() instanceof PointerType) {
            translateTempDeadChecker(function, rightValue, (PointerType) rightValue.getType());
        }


        return command.getResult();
    }

    private Value translateConditionalExpression(Function function, ConditionalExpressionNode expressionNode) {
        BasicBlock last = function.getCurrentBlock();
        BasicBlock condition = function.appendBlock("conditional");
        BasicBlock thenBlock = null;
        BasicBlock elseBlock = null;
        BasicBlock mergeBlock = null;

        createBranch(last, condition);

        Command alloc = new Command(
                createTempVariable(matchType(expressionNode.getResultType())),
                ALLOC,
                List.of()
        );
        function.getCurrentBlock().addCommand(alloc);

        Value conditionValue = translateExpression(function, expressionNode.getConditionNode());
        BasicBlock endCondition = function.getCurrentBlock();

        thenBlock = function.appendBlock("conditional_then");
        Value firstArg = translateExpression(function, expressionNode.getThenNode());
        BasicBlock endThen = function.getCurrentBlock();
        Command firstStore = new Command(alloc.getResult(), STORE, List.of(firstArg));
        endThen.addCommand(firstStore);

        elseBlock = function.appendBlock("conditional_else");
        Value secondArg = translateExpression(function, expressionNode.getElseNode());
        BasicBlock endElse = function.getCurrentBlock();
        Command secondStore = new Command(alloc.getResult(), STORE, List.of(secondArg));
        endElse.addCommand(secondStore);

        mergeBlock = function.appendBlock("conditional_result");

        createBranch(endThen, mergeBlock);
        createBranch(endElse, mergeBlock);
        createConditionalBranch(endCondition, conditionValue, thenBlock, elseBlock);

        Command loadRet = new Command(
                createTempVariable(alloc.getResult().getType()),
                LOAD,
                List.of(alloc.getResult())
        );
        function.getCurrentBlock().addCommand(loadRet);

        return loadRet.getResult();
    }

    private Value translateAssigmentExpression(Function function, AssigmentExpressionNode expressionNode) {
        Type mt = matchType(expressionNode.getResultType());
        Value left = translateLeftValue(function, expressionNode.getLeft());
        Value saveLeft = null;

        if (mt instanceof PointerType) {
            PointerType pointerType = (PointerType) mt;

            Command leftLoad = new Command(
                    createTempVariable(pointerType),
                    LOAD,
                    List.of(left)
            );
            function.getCurrentBlock().addCommand(leftLoad);


            saveLeft = leftLoad.getResult();
        }

        Value right = translateExpression(function, expressionNode.getRight());

        Command command = new Command(left, STORE, List.of(right));

        BasicBlock current = function.getCurrentBlock();
        current.addCommand(command);

        if (mt instanceof PointerType &&
                !(mt.getType().getType() instanceof FunctionType)) {
            PointerType pointerType = (PointerType) mt;

            Value fieldAccess = translateLeftValue(function, expressionNode.getLeft());

            Command load = new Command(
                    createTempVariable(pointerType),
                    LOAD,
                    List.of(fieldAccess)
            );
            function.getCurrentBlock().addCommand(load);


            Command castCommand = new Command(
                    createTempVariable(new PointerType(commonStruct)),
                    CAST,
                    List.of(load.getResult())
            );
            function.getCurrentBlock().addCommand(castCommand);

            Command copyCall = new Command(
                    null,
                    CALL,
                    List.of(addInc, castCommand.getResult())
            );
            function.getCurrentBlock().addCommand(copyCall);
        }

        if (mt instanceof PointerType && !(mt.getType() instanceof FunctionType)) {
            addDestructorCall(function, (PointerType) saveLeft.getType(), saveLeft);
        }

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

            Command castForAdd = new Command(
                    createTempVariable(INT_64),
                    PTRTOINT,
                    List.of(arrayValue)
            );
            function.getCurrentBlock().addCommand(castForAdd);

            Command withHeaderAccess = new Command(
                    createTempVariable(INT_64),
                    ADD,
                    List.of(castForAdd.getResult(), new IntValue(commonArray.getSize()))
            );
            function.getCurrentBlock().addCommand(withHeaderAccess);

            Command castReverse = new Command(
                    createTempVariable(arrayValue.getType()),
                    INTTOPTR,
                    List.of(withHeaderAccess.getResult())
            );
            function.getCurrentBlock().addCommand(castReverse);

            Command command = new Command(
                    createTempVariable(matchType(expressionNode.getResultType())),
                    ARRAY_REFERENCE,
                    List.of(castReverse.getResult(), offsetValue));

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
