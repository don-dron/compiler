package lang.semantic;

import lang.ast.ArrayTypeNode;
import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.FileNode;
import lang.ast.FunctionNode;
import lang.ast.ImportNode;
import lang.ast.ObjectTypeNode;
import lang.ast.ParameterNode;
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
import lang.ast.statement.IfElseStatementNode;
import lang.ast.statement.IfStatementNode;
import lang.ast.statement.InterfaceStatementNode;
import lang.ast.statement.ReturnStatementNode;
import lang.ast.statement.StatementNode;
import lang.ast.statement.WhileStatementNode;
import lang.scope.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnalysis {

    private final String rootPath;
    private final List<FileNode> fileNodes;
    private final Map<FileNode, List<DeclarationStatementNode>> externDeclarationStatementNodes;
    private final Map<FileNode, List<InterfaceStatementNode>> externIntefaceStatementsNodes;
    private final Map<FileNode, List<ClassStatementNode>> externClassStatementsNodes;
    private final Map<FileNode, List<FunctionDefinitionNode>> externFunctionDefinitionNodes;
    private final Map<FileNode, List<FileNode>> importedFiles;
    private FunctionDefinitionNode mainFunction;

    public SemanticAnalysis(String rootPath, List<FileNode> fileNodes) {
        this.rootPath = rootPath;
        this.fileNodes = fileNodes;
        this.externDeclarationStatementNodes = new HashMap<>();
        this.externIntefaceStatementsNodes = new HashMap<>();
        this.externClassStatementsNodes = new HashMap<>();
        this.externFunctionDefinitionNodes = new HashMap<>();
        this.importedFiles = new HashMap<>();
    }

    public void analyse() {
        for (FileNode fileNode : fileNodes) {
            analyseImports(fileNode);
        }

        for (FileNode fileNode : fileNodes) {
            analyseDefinitions(fileNode);
        }

        findMainFunction();

        for (FileNode fileNode : fileNodes) {
            runAnalysis(fileNode);
        }

        System.out.println(Stream.of(externClassStatementsNodes,
                externDeclarationStatementNodes,
                externFunctionDefinitionNodes,
                externIntefaceStatementsNodes)
                .flatMap(mp -> mp.values().stream())
                .flatMap(Collection::stream)
                .map(st -> st.astDebug(0))
                .collect(Collectors.joining("\n")));
    }

    private void findMainFunction() {
        FileNode fileNode = null;
        for (Map.Entry<FileNode, List<FunctionDefinitionNode>> entry : externFunctionDefinitionNodes.entrySet()) {
            List<FunctionDefinitionNode> definitions = entry.getValue();

            for (FunctionDefinitionNode functionDefinition : definitions) {
                if (functionDefinition.getIdentifierNode().getName().equals("main")) {
                    if (mainFunction != null && fileNode != null) {
                        throw new IllegalArgumentException("Main already defined, first definition: "
                                + mainFunction.getIdentifierNode().getToken());
                    }
                    mainFunction = functionDefinition;
                    fileNode = entry.getKey();
                }
            }
        }
    }

    private void analyseImports(FileNode fileNode) {
        for (ImportNode importNode : fileNode.getImportNodes()) {
            FileNode file = findImportedFile(importNode.getExpressionNode());
            importedFiles.computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                    .add(file);
        }
    }

    private FileNode findImportedFile(ExpressionNode expressionNode) {
        List<String> path = new ArrayList<>();
        while (true) {
            if (expressionNode instanceof VariableExpressionNode) {
                VariableExpressionNode variable = (VariableExpressionNode) expressionNode;
                path.add(variable.getIdentifierNode().getName());
                break;
            } else if (expressionNode instanceof FieldAccessExpressionNode) {
                FieldAccessExpressionNode field = (FieldAccessExpressionNode) expressionNode;
                path.add(field.getRight().getIdentifierNode().getName());

                expressionNode = field.getLeft();
            } else {
                throw new IllegalArgumentException("Wrong path");
            }
        }

        Collections.reverse(path);
        String absolutePath = rootPath + "\\" + String.join("\\", path);

        return fileNodes.stream()
                .filter(f -> f.getPath().equals(absolutePath))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find file"));
    }

    private void analyseDefinitions(FileNode fileNode) {
        fileNode.getStatementNodes().removeIf(s -> s instanceof EmptyStatementNode);

        Scope scope = new Scope(null);
        fileNode.setScope(scope);
        scope.setOwner(fileNode);

        List<StatementNode> imports = importedFiles.getOrDefault(fileNode, List.of())
                .stream()
                .flatMap(f -> f.getStatementNodes().stream())
                .collect(Collectors.toList());

        for (StatementNode statementNode : imports) {
            scope.addWeakDeclaration(statementNode);
        }

        for (StatementNode statementNode : fileNode.getStatementNodes()) {
            if (statementNode instanceof ClassStatementNode) {
                ClassStatementNode classStatementNode = (ClassStatementNode) statementNode;
                externClassStatementsNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add(classStatementNode);
            } else if (statementNode instanceof InterfaceStatementNode) {
                InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode) statementNode;
                externIntefaceStatementsNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add(interfaceStatementNode);
            } else if (statementNode instanceof FunctionDefinitionNode) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) statementNode;
                externFunctionDefinitionNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add(functionDefinitionNode);
            } else if (statementNode instanceof DeclarationStatementNode) {
                DeclarationStatementNode declarationStatementNode = (DeclarationStatementNode) statementNode;
                externDeclarationStatementNodes
                        .computeIfAbsent(fileNode, (k) -> new ArrayList<>())
                        .add(declarationStatementNode);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void runAnalysis(FileNode fileNode) {
        for (StatementNode node : fileNode.getStatementNodes()) {
            if (node instanceof ClassStatementNode ||
                    node instanceof InterfaceStatementNode ||
                    node instanceof FunctionDefinitionNode ||
                    node instanceof DeclarationStatementNode) {
                analyseStatement(node, fileNode.getScope());
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseStatement(StatementNode node, Scope scope) {
        if (node instanceof BreakStatementNode) {
            analyseBreak((BreakStatementNode) node, scope);
        } else if (node instanceof ClassStatementNode) {
            analyseClass((ClassStatementNode) node, scope);
        } else if (node instanceof CompoundStatementNode) {
            analyseCompound((CompoundStatementNode) node, scope);
        } else if (node instanceof ContinueStatementNode) {
            analyseContinue((ContinueStatementNode) node, scope);
        } else if (node instanceof DeclarationStatementNode) {
            analyseDeclaration((DeclarationStatementNode) node, scope);
        } else if (node instanceof IfElseStatementNode) {
            analyseIfElse((IfElseStatementNode) node, scope);
        } else if (node instanceof InterfaceStatementNode) {
            analyseInterface((InterfaceStatementNode) node, scope);
        } else if (node instanceof ReturnStatementNode) {
            analyseReturn((ReturnStatementNode) node, scope);
        } else if (node instanceof WhileStatementNode) {
            analyseWhile((WhileStatementNode) node, scope);
        } else if (node instanceof FunctionDefinitionNode) {
            analyseFunction((FunctionDefinitionNode) node, scope);
        } else if (node instanceof EmptyStatementNode) {
        } else if (node instanceof ExpressionStatementNode) {
            analyseExpressionStatement((ExpressionStatementNode) node, scope);
        } else {
            throw new IllegalArgumentException("Undefined statement");
        }
    }

    private void analyseExpressionStatement(ExpressionStatementNode node, Scope scope) {
        node.setScope(scope);
        analyseExpression(node.getExpressionNode(), scope);
    }

    private void analyseBreak(BreakStatementNode node, Scope scope) {
        node.setScope(scope);
    }

    private void analyseClass(ClassStatementNode classNode, Scope parentScope) {
        classNode.setScope(parentScope);
        parentScope.addDeclaration(classNode);

        Scope scope = new Scope(parentScope);
        scope.setOwner(classNode);
        classNode.getTranslationNode().getStatements().removeIf(s -> s instanceof EmptyStatementNode);

        for (StatementNode node : classNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode ||
                    node instanceof InterfaceStatementNode ||
                    node instanceof FunctionDefinitionNode ||
                    node instanceof DeclarationStatementNode) {
                analyseStatement(node, scope);
            } else if (node instanceof ConstructorDefinitionNode) {
                analyseConstructorDefinitionNode((ConstructorDefinitionNode) node, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseCompound(CompoundStatementNode statementNode, Scope scope) {
        statementNode.setScope(scope);

        List<StatementNode> refactor = new ArrayList<>();

        for (int i = 0; i < statementNode.getStatements().size(); i++) {
            StatementNode node = statementNode.getStatements().get(i);
            if (node instanceof IfStatementNode) {
                IfStatementNode ifStatementNode = (IfStatementNode) node;
                List<ElifStatementNode> elifStatementNodes = new ArrayList<>();
                ElseStatementNode elseStatementNode = null;
                i++;

                while (i < statementNode.getStatements().size()) {
                    node = statementNode.getStatements().get(i);

                    if (node instanceof ElifStatementNode) {
                        elifStatementNodes.add((ElifStatementNode) node);
                    } else if (node instanceof ElseStatementNode) {
                        elseStatementNode = (ElseStatementNode) node;
                        break;
                    } else {
                        break;
                    }

                    i++;
                }

                refactor.add(new IfElseStatementNode(ifStatementNode, elifStatementNodes, elseStatementNode));
            } else if (node instanceof ConstructorDefinitionNode) {
                throw new IllegalArgumentException("Wrong constructor");
            } else if (!(node instanceof EmptyStatementNode)) {
                refactor.add(node);
            }
        }

        statementNode.getStatements().clear();
        statementNode.getStatements().addAll(refactor);

        for (StatementNode node : statementNode.getStatements()) {
            node.setScope(scope);
            analyseStatement(node, scope);
        }
    }

    private void analyseConstructorDefinitionNode(ConstructorDefinitionNode node, Scope scope) {
        node.setScope(scope);

        Scope constructorScope = new Scope(scope);
        scope.setOwner(node);
        analyseStatement(node.getStatementNode(), constructorScope);
    }

    private void analyseContinue(ContinueStatementNode node, Scope scope) {
        node.setScope(scope);
    }

    private void analyseDeclaration(DeclarationStatementNode node, Scope parentScope) {
        node.setScope(parentScope);
        parentScope.addDeclaration(node);

        node.getTypeNode().setScope(parentScope);
        node.getIdentifierNode().setScope(parentScope);

        if (node.getExpressionNode() != null) {
            node.getExpressionNode().setScope(parentScope);
            analyseExpression(node.getExpressionNode(), parentScope);
        }
    }

    private void analyseFunction(FunctionDefinitionNode function, Scope parentScope) {
        function.setScope(parentScope);
        parentScope.addDeclaration(function);

        if (function.getStatementNode() != null) {
            Scope scope = new Scope(parentScope);
            scope.setOwner(function);
            function.getFunctionNode().getParametersNode().getParameters()
                    .forEach(scope::addDeclaration);
            analyseStatement(function.getStatementNode(), scope);
        }
    }

    private void analyseIfElse(IfElseStatementNode statementNode, Scope scope) {
        statementNode.setScope(scope);

        statementNode.getIfStatementNode().getConditionNode().setScope(scope);

        Scope ifScope = new Scope(scope);

        ifScope.setOwner(statementNode.getIfStatementNode());

        analyseExpression(statementNode.getIfStatementNode().getConditionNode(), scope);
        analyseStatement(statementNode.getIfStatementNode().getThenNode(), ifScope);

        if (statementNode.getElifStatementNodes() != null) {
            for (ElifStatementNode node : statementNode.getElifStatementNodes()) {
                Scope elifScope = new Scope(scope);
                elifScope.setOwner(node);

                node.getConditionNode().setScope(scope);
                analyseExpression(node.getConditionNode(), scope);
                analyseStatement(node.getElseNode(), elifScope);
            }
        }

        if (statementNode.getElseStatementNode() != null) {
            Scope elseScope = new Scope(scope);
            elseScope.setOwner(statementNode.getElseStatementNode());
            analyseStatement(statementNode.getElseStatementNode().getElseNode(), elseScope);
        }
    }

    private void analyseInterface(InterfaceStatementNode node, Scope parentScope) {
        node.setScope(parentScope);
        parentScope.addDeclaration(node);

        Scope scope = new Scope(parentScope);
        scope.setOwner(node);

        node.getTranslationNode().getStatements().removeIf(s -> s instanceof EmptyStatementNode);

        for (StatementNode inner : node.getTranslationNode().getStatements()) {
            if (inner instanceof ClassStatementNode ||
                    inner instanceof InterfaceStatementNode ||
                    inner instanceof FunctionDefinitionNode) {
                analyseStatement(inner, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseReturn(ReturnStatementNode node, Scope parentScope) {
        node.setScope(parentScope);

        if (node.getExpressionNode() != null) {
            node.getExpressionNode().setScope(parentScope);
            analyseExpression(node.getExpressionNode(), parentScope);
        }
    }

    private void analyseWhile(WhileStatementNode node, Scope scope) {
        node.getConditionNode().setScope(scope);
        analyseExpression(node.getConditionNode(), scope);

        Scope whileScope = new Scope(scope);
        scope.setOwner(node);
        analyseStatement(node.getBodyNode(), whileScope);
    }

    private void analyseExpression(ExpressionNode expressionNode, Scope parentScope) {
        if (expressionNode instanceof AssigmentExpressionNode) {
            AssigmentExpressionNode assigmentExpressionNode = (AssigmentExpressionNode) expressionNode;

            ExpressionNode left = assigmentExpressionNode.getLeft();
            ExpressionNode right = assigmentExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            TypeNode typeNode = null;
            if (left.getResultType() instanceof BasicTypeNode
                    && right.getResultType() instanceof BasicTypeNode) {
                typeNode = defineBinaryOperationType(expressionNode, left, right);
            }

            if (typeNode == null) {
                throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString() + " : " +
                        left.getResultType().toString() + " and " + right.getResultType().toString());
            }
        } else if (expressionNode instanceof ConditionalExpressionNode) {
            ConditionalExpressionNode conditionalExpressionNode = (ConditionalExpressionNode) expressionNode;

            ExpressionNode cond = conditionalExpressionNode.getConditionNode();
            ExpressionNode then = conditionalExpressionNode.getThenNode();
            ExpressionNode els = conditionalExpressionNode.getElseNode();

            analyseExpression(cond, parentScope);
            analyseExpression(then, parentScope);
            analyseExpression(els, parentScope);

            if (!cond.getResultType().equals(BoolConstantExpressionNode.boolType)) {
                throw new IllegalArgumentException("Wrong types " + cond.toString());
            }

            if (!then.getResultType().equals(els.getResultType())) {
                throw new IllegalArgumentException("Wrong types " + then.toString() + " " + els.toString());
            }

            expressionNode.setResultType(then.getResultType());
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            AdditiveExpressionNode additiveExpressionNode = (AdditiveExpressionNode) expressionNode;

            ExpressionNode left = additiveExpressionNode.getLeft();
            ExpressionNode right = additiveExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            TypeNode typeNode = null;
            if (left.getResultType() instanceof BasicTypeNode
                    && right.getResultType() instanceof BasicTypeNode) {
                typeNode = defineBinaryOperationType(expressionNode, left, right);
            }

            if (typeNode == null) {
                throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
            }
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            MultiplicativeExpressionNode multiplicativeExpressionNode = (MultiplicativeExpressionNode) expressionNode;

            ExpressionNode left = multiplicativeExpressionNode.getLeft();
            ExpressionNode right = multiplicativeExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            TypeNode typeNode = null;
            if (left.getResultType() instanceof BasicTypeNode
                    && right.getResultType() instanceof BasicTypeNode) {
                typeNode = defineBinaryOperationType(expressionNode, left, right);
            }

            if (typeNode == null) {
                throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
            }
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            LogicalAndExpressionNode logicalAndExpressionNode = (LogicalAndExpressionNode) expressionNode;

            ExpressionNode left = logicalAndExpressionNode.getLeft();
            ExpressionNode right = logicalAndExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            if (!left.getResultType().equals(BoolConstantExpressionNode.boolType)) {
                throw new IllegalArgumentException("Wrong types " + left.toString());
            }

            if (!right.getResultType().equals(BoolConstantExpressionNode.boolType)) {
                throw new IllegalArgumentException("Wrong types " + right.toString());
            }

            logicalAndExpressionNode.setResultType(BoolConstantExpressionNode.boolType);
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            LogicalOrExpressionNode logicalOrExpressionNode = (LogicalOrExpressionNode) expressionNode;

            ExpressionNode left = logicalOrExpressionNode.getLeft();
            ExpressionNode right = logicalOrExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            if (!left.getResultType().equals(BoolConstantExpressionNode.boolType)) {
                throw new IllegalArgumentException("Wrong types " + left.toString());
            }

            if (!right.getResultType().equals(BoolConstantExpressionNode.boolType)) {
                throw new IllegalArgumentException("Wrong types " + right.toString());
            }

            logicalOrExpressionNode.setResultType(BoolConstantExpressionNode.boolType);
        } else if (expressionNode instanceof RelationalExpressionNode) {
            RelationalExpressionNode relationalExpressionNode = (RelationalExpressionNode) expressionNode;

            ExpressionNode left = relationalExpressionNode.getLeft();
            ExpressionNode right = relationalExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            if (!left.getResultType().equals(right.getResultType())) {
                throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
            }

            relationalExpressionNode.setResultType(BoolConstantExpressionNode.boolType);
        } else if (expressionNode instanceof EqualityExpressionNode) {
            EqualityExpressionNode equalityExpressionNode = (EqualityExpressionNode) expressionNode;

            ExpressionNode left = equalityExpressionNode.getLeft();
            ExpressionNode right = equalityExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);

            if (!left.getResultType().equals(right.getResultType())) {
                throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
            }

            equalityExpressionNode.setResultType(BoolConstantExpressionNode.boolType);
        } else if (expressionNode instanceof VariableExpressionNode) {
            VariableExpressionNode variableExpressionNode = (VariableExpressionNode) expressionNode;
            String name = ((VariableExpressionNode) expressionNode).getIdentifierNode().getName();
            AstNode variableNode = parentScope.findDefinitionByVariable(name);
            variableExpressionNode.setExpression(variableNode);

            if (variableNode == null) {
                throw new IllegalArgumentException("Undefined variable " +
                        variableExpressionNode.getIdentifierNode().toString());
            }

            if (variableNode instanceof DeclarationStatementNode) {
                DeclarationStatementNode statementNode = (DeclarationStatementNode) variableNode;
                variableExpressionNode.setResultType(statementNode.getTypeNode());
                variableExpressionNode.setIdentifier(statementNode.getIdentifierNode());
            } else if (variableNode instanceof ParameterNode) {
                ParameterNode parameterNode = (ParameterNode) variableNode;
                variableExpressionNode.setResultType(parameterNode.getTypeNode());
                variableExpressionNode.setIdentifier(parameterNode.getIdentifierNode());
            } else if (variableNode instanceof FunctionDefinitionNode) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) variableNode;
                variableExpressionNode.setResultType(functionDefinitionNode.getFunctionNode());
                variableExpressionNode.setIdentifier(functionDefinitionNode.getIdentifierNode());
            } else if (variableNode instanceof ClassStatementNode) {
                ClassStatementNode classStatementNode = (ClassStatementNode)variableNode;
                variableExpressionNode.setResultType(new ObjectTypeNode(
                        classStatementNode.getIdentifierNode()));
                variableExpressionNode.setIdentifier(classStatementNode.getIdentifierNode());
            } else if (variableNode instanceof InterfaceStatementNode) {
                InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode)variableNode;
                variableExpressionNode.setResultType(new ObjectTypeNode(
                        interfaceStatementNode.getIdentifierNode()));
                variableExpressionNode.setIdentifier(interfaceStatementNode.getIdentifierNode());
            } else {
                throw new IllegalArgumentException("Unknown");
            }
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            BoolConstantExpressionNode boolConstantExpressionNode = (BoolConstantExpressionNode) expressionNode;
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            IntConstantExpressionNode intConstantExpressionNode = (IntConstantExpressionNode) expressionNode;
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            FloatConstantExpressionNode floatConstantExpressionNode = (FloatConstantExpressionNode) expressionNode;
        } else if (expressionNode instanceof NullConstantExpressionNode) {
            NullConstantExpressionNode nullConstantExpressionNode = (NullConstantExpressionNode) expressionNode;
        } else if (expressionNode instanceof ArrayConstructorExpressionNode) {
            ArrayConstructorExpressionNode arrayConstructorExpressionNode =
                    (ArrayConstructorExpressionNode) expressionNode;

            ExpressionNode sizeExpression = arrayConstructorExpressionNode.getSizeExpression();

            analyseExpression(sizeExpression, parentScope);
        } else if (expressionNode instanceof FunctionCallExpressionNode) {
            FunctionCallExpressionNode functionCallExpressionNode = (FunctionCallExpressionNode) expressionNode;

            ExpressionNode function = functionCallExpressionNode.getFunction();
            analyseExpression(function, parentScope);

            FunctionNode functionNode = (FunctionNode) function.getResultType();

            List<ExpressionNode> expressions = functionCallExpressionNode.getParameters().getList();
            List<ParameterNode> parameterNodes = functionNode.getParametersNode().getParameters();
            if (expressions.size() != parameterNodes.size()) {
                throw new IllegalArgumentException("Wrong parameter's size");
            }

            for (int i = 0; i < expressions.size(); i++) {
                ExpressionNode node = expressions.get(i);
                ParameterNode parameterNode = parameterNodes.get(i);

                analyseExpression(node, parentScope);

                if (!parameterNode.getTypeNode().equals(node.getResultType())) {
                    throw new IllegalArgumentException("Wrong parameter type " +
                            parameterNode.getTypeNode() + " " + node.getResultType());
                }
            }

            functionCallExpressionNode.setResultType(functionNode.getTypeNode());
        } else if (expressionNode instanceof ArrayAccessExpressionNode) {
            ArrayAccessExpressionNode arrayAccessExpressionNode = (ArrayAccessExpressionNode) expressionNode;

            ExpressionNode array = arrayAccessExpressionNode.getArray();
            ExpressionNode argument = arrayAccessExpressionNode.getArgument();

            analyseExpression(array, parentScope);
            analyseExpression(argument, parentScope);

            if (!argument.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + argument.toString());
            }

            expressionNode.setResultType(((ArrayTypeNode) array.getResultType()).getTypeNode());
        } else if (expressionNode instanceof FieldAccessExpressionNode) {
            FieldAccessExpressionNode fieldAccessExpressionNode = (FieldAccessExpressionNode) expressionNode;

            ExpressionNode left = fieldAccessExpressionNode.getLeft();
            ExpressionNode right = fieldAccessExpressionNode.getRight();

            analyseExpression(left, parentScope);
            analyseExpression(right, parentScope);
        } else if (expressionNode instanceof PostfixDecrementSubtractionExpressionNode) {
            PostfixDecrementSubtractionExpressionNode postfixDecrementSubtractionExpressionNode =
                    (PostfixDecrementSubtractionExpressionNode) expressionNode;

            ExpressionNode node = postfixDecrementSubtractionExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof PostfixIncrementAdditiveExpressionNode) {
            PostfixIncrementAdditiveExpressionNode postfixIncrementAdditiveExpressionNode =
                    (PostfixIncrementAdditiveExpressionNode) expressionNode;

            ExpressionNode node = postfixIncrementAdditiveExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof PostfixIncrementMultiplicativeExpressionNode) {
            PostfixIncrementMultiplicativeExpressionNode postfixIncrementMultiplicativeExpressionNode =
                    (PostfixIncrementMultiplicativeExpressionNode) expressionNode;

            ExpressionNode node = postfixIncrementMultiplicativeExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof PrefixDecrementSubtractionExpressionNode) {
            PrefixDecrementSubtractionExpressionNode prefixDecrementSubtractionExpressionNode =
                    (PrefixDecrementSubtractionExpressionNode) expressionNode;

            ExpressionNode node = prefixDecrementSubtractionExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof PrefixIncrementAdditiveExpressionNode) {
            PrefixIncrementAdditiveExpressionNode prefixIncrementAdditiveExpressionNode =
                    (PrefixIncrementAdditiveExpressionNode) expressionNode;

            ExpressionNode node = prefixIncrementAdditiveExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof PrefixIncrementMultiplicativeExpressionNode) {
            PrefixIncrementMultiplicativeExpressionNode prefixIncrementMultiplicativeExpressionNode =
                    (PrefixIncrementMultiplicativeExpressionNode) expressionNode;

            ExpressionNode node = prefixIncrementMultiplicativeExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            if (!node.getResultType().equals(IntConstantExpressionNode.intType)) {
                throw new IllegalArgumentException("Wrong types " + node.toString());
            }
        } else if (expressionNode instanceof CastExpressionNode) {
            CastExpressionNode castExpressionNode = (CastExpressionNode) expressionNode;

            ExpressionNode node = castExpressionNode.getExpressionNode();
            analyseExpression(node, parentScope);

            castExpressionNode.setResultType(castExpressionNode.getTypeNode());
        }
    }

    private TypeNode defineBinaryOperationType(ExpressionNode expressionNode, ExpressionNode left, ExpressionNode right) {
        BasicTypeNode leftType = (BasicTypeNode) left.getResultType();
        BasicTypeNode rightType = (BasicTypeNode) right.getResultType();

        if (leftType.getType().getSize() > rightType.getType().getSize()) {
            BasicTypeNode t = leftType;
            leftType = rightType;
            rightType = t;
        }

        if (leftType.getType() == TypeNode.Type.BOOL
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.BOOL
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.BOOL
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.BOOL
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.BOOL
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.CHAR
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.CHAR
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.CHAR
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.CHAR
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.CHAR
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.SHORT
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.SHORT
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.SHORT
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.SHORT
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.SHORT
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.INT
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.INT
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.INT
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.INT
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.INT
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.LONG
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.LONG
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.LONG
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.LONG
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.LONG
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.FLOAT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.DOUBLE) {
            expressionNode.setResultType(rightType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.FLOAT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.DOUBLE) {
            expressionNode.setResultType(rightType);
        }

        return expressionNode.getResultType();
    }
}
