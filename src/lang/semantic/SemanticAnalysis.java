package lang.semantic;

import lang.ast.ArrayTypeNode;
import lang.ast.AstNode;
import lang.ast.BasicTypeNode;
import lang.ast.FileNode;
import lang.ast.FunctionNode;
import lang.ast.GlobalBasicType;
import lang.ast.ImportNode;
import lang.ast.ObjectTypeNode;
import lang.ast.ParameterNode;
import lang.ast.Program;
import lang.ast.TypeNode;
import lang.ast.expression.*;
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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static lang.ast.GlobalBasicType.REF_TYPE;

public class SemanticAnalysis {

    private final String rootPath;
    private final List<FileNode> fileNodes;
    private final Map<FileNode, List<FileNode>> importedFiles;
    private FunctionDefinitionNode mainFunction;
    private final List<FunctionDefinitionNode> functions;
    private final List<ClassStatementNode> classes;

    public SemanticAnalysis(String rootPath, List<FileNode> fileNodes) {
        this.rootPath = rootPath;
        this.fileNodes = fileNodes;
        this.importedFiles = new HashMap<>();
        this.functions = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public Program analyse() {
        findMainFunction();

        for (FileNode fileNode : fileNodes) {
            analyseImports(fileNode);
        }

        for (FileNode fileNode : fileNodes) {
            analyseDefinitionsStart(fileNode);
        }

        for (FileNode fileNode : fileNodes) {
            linkImports(fileNode);
        }

        for (FileNode fileNode : fileNodes) {
            analyseDefinitionsMiddle(fileNode);
        }

        for (FileNode fileNode : fileNodes) {
            analyseDefinitionsEnd(fileNode);
        }

        return new Program(
                mainFunction,
                new ArrayList<>(classes),
                new ArrayList<>(functions),
                new ArrayList<>(fileNodes));
    }

    private void findMainFunction() {
        for (FileNode fileNode : fileNodes) {
            for (FunctionDefinitionNode functionDefinition : fileNode
                    .getStatementNodes()
                    .stream()
                    .filter(st -> st instanceof FunctionDefinitionNode)
                    .map(st -> (FunctionDefinitionNode) st)
                    .collect(Collectors.toList())) {
                if (functionDefinition.getIdentifierNode().getName().equals("main")) {
                    if (mainFunction != null) {
                        throw new IllegalArgumentException("Main already defined, first definition: "
                                + mainFunction.getIdentifierNode().getToken());
                    }
                    mainFunction = functionDefinition;
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
        String absolutePath = Paths.get(rootPath, path.toArray(String[]::new)).toString();

        return fileNodes.stream()
                .filter(f -> f.getPath().equals(absolutePath))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find file"));
    }

    private void analyseDefinitionsStart(FileNode fileNode) {
        fileNode.getStatementNodes().removeIf(s -> s instanceof EmptyStatementNode);

        Scope scope = new Scope(null);
        fileNode.setScope(scope);
        scope.setOwner(fileNode);

        for (StatementNode node : fileNode.getStatementNodes()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalStart((ClassStatementNode) node, scope);
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalStart((InterfaceStatementNode) node, scope);
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalStart((FunctionDefinitionNode) node, scope);
            } else if (node instanceof DeclarationStatementNode) {
                analyseDeclarationInGlobalStart((DeclarationStatementNode) node, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void linkImports(FileNode fileNode) {
        Scope scope = fileNode.getScope();
        List<StatementNode> imports = importedFiles.getOrDefault(fileNode, List.of())
                .stream()
                .flatMap(f -> f.getStatementNodes().stream())
                .collect(Collectors.toList());

        for (StatementNode statementNode : imports) {
            scope.addWeakDeclaration(statementNode);
        }
    }

    private void analyseDefinitionsMiddle(FileNode fileNode) {
        Scope scope = fileNode.getScope();
        for (StatementNode node : fileNode.getStatementNodes()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalMiddle((ClassStatementNode) node, scope);
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalMiddle((InterfaceStatementNode) node, scope);
            }
        }
    }

    private void analyseDefinitionsEnd(FileNode fileNode) {
        Scope scope = fileNode.getScope();
        for (StatementNode node : fileNode.getStatementNodes()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalEnd((ClassStatementNode) node, scope);
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalEnd((InterfaceStatementNode) node, scope);
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalEnd((FunctionDefinitionNode) node, scope);
            } else if (node instanceof DeclarationStatementNode) {
                analyseDeclarationInGlobalEnd((DeclarationStatementNode) node, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseStatement(StatementNode node, Scope scope) {
        if (node instanceof BreakStatementNode) {
            analyseBreak((BreakStatementNode) node, scope);
        } else if (node instanceof ClassStatementNode) {
            analyseClassInGlobalStart((ClassStatementNode) node, scope);
            analyseClassInGlobalMiddle((ClassStatementNode) node, scope);
            analyseClassInGlobalEnd((ClassStatementNode) node, scope);
        } else if (node instanceof CompoundStatementNode) {
            analyseCompound((CompoundStatementNode) node, scope);
        } else if (node instanceof ContinueStatementNode) {
            analyseContinue((ContinueStatementNode) node, scope);
        } else if (node instanceof DeclarationStatementNode) {
            analyseDeclaration((DeclarationStatementNode) node, scope);
        } else if (node instanceof IfElseStatementNode) {
            analyseIfElse((IfElseStatementNode) node, scope);
        } else if (node instanceof InterfaceStatementNode) {
            analyseInterfaceInGlobalStart((InterfaceStatementNode) node, scope);
            analyseInterfaceInGlobalMiddle((InterfaceStatementNode) node, scope);
            analyseInterfaceInGlobalEnd((InterfaceStatementNode) node, scope);
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
        AstNode cycle = findCycle(scope);
        node.setCycle(cycle);
    }

    private void analyseClassInGlobalStart(ClassStatementNode classNode, Scope parentScope) {
        classNode.setScope(parentScope);
        parentScope.addDeclaration(classNode);

        classes.add(classNode);

        Scope scope = new Scope(parentScope);
        classNode.setInnerScope(scope);
        scope.setOwner(classNode);
        classNode.getTranslationNode().getStatements().removeIf(s -> s instanceof EmptyStatementNode);

        for (StatementNode node : classNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalStart((ClassStatementNode) node, scope);
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalStart((InterfaceStatementNode) node, scope);
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalStart((FunctionDefinitionNode) node, scope);
            } else if (node instanceof DeclarationStatementNode) {
                classNode.addField((DeclarationStatementNode) node);
                analyseDeclarationInGlobalStart((DeclarationStatementNode) node, scope);
            } else if (node instanceof ConstructorDefinitionNode) {
                analyseConstructorDefinitionNodeStart((ConstructorDefinitionNode) node, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseClassInGlobalMiddle(ClassStatementNode classNode, Scope scope) {
        List<AstNode> extendsNode = classNode.getExtendNames()
                .stream()
                .map(i -> {
                    AstNode node = scope.findDefinitionByVariable(i.getName());

                    if (node == null) {
                        throw new IllegalArgumentException("Undefined node");
                    } else if (node instanceof ClassStatementNode) {
                        classNode.addExtendNode(node);
                        classNode.getInnerScope().addAlternativeScope(((ClassStatementNode) node).getInnerScope());
                    } else if (node instanceof InterfaceStatementNode) {
                        classNode.addExtendNode(node);
                        classNode.getInnerScope().addAlternativeScope(((InterfaceStatementNode) node).getInnerScope());
                    }

                    return node;
                })
                .collect(Collectors.toList());

        for (StatementNode node : classNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalMiddle((ClassStatementNode) node, node.getScope());
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalMiddle((InterfaceStatementNode) node, node.getScope());
            }
        }
    }

    private void analyseClassInGlobalEnd(ClassStatementNode classNode, Scope parentScope) {
        for (StatementNode node : classNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalEnd((ClassStatementNode) node, node.getScope());
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalEnd((InterfaceStatementNode) node, node.getScope());
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalEnd((FunctionDefinitionNode) node, node.getScope());
            } else if (node instanceof DeclarationStatementNode) {
                analyseDeclarationInGlobalEnd((DeclarationStatementNode) node, node.getScope());
            } else if (node instanceof ConstructorDefinitionNode) {
                analyseConstructorDefinitionNodeEnd((ConstructorDefinitionNode) node, node.getScope());
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
                        i++;
                        break;
                    } else {
                        break;
                    }

                    i++;
                }

                i--;

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

    private void analyseConstructorDefinitionNodeStart(ConstructorDefinitionNode node, Scope scope) {
        node.setScope(scope);

        if (scope.getOwner() instanceof ClassStatementNode) {
            node.getFunctionNode().setTypeNode(
                    new ObjectTypeNode(((ClassStatementNode) scope.getOwner()).getIdentifierNode()));
            node.setClass((ClassStatementNode) scope.getOwner());
            ((ClassStatementNode) scope.getOwner()).addConstructor(node);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private void analyseConstructorDefinitionNodeEnd(ConstructorDefinitionNode node, Scope scope) {
        Scope constructorScope = new Scope(scope);
        scope.setOwner(node);
        node.getFunctionNode().getParametersNode().getParameters()
                .forEach(p -> {
                    analyseType(p.getTypeNode(), scope);
                    constructorScope.addDeclaration(p);
                });
        analyseStatement(node.getStatementNode(), constructorScope);
    }

    private void analyseContinue(ContinueStatementNode node, Scope scope) {
        node.setScope(scope);

        AstNode cycle = findCycle(scope);
        node.setCycle(cycle);
    }

    private void analyseDeclaration(DeclarationStatementNode node, Scope parentScope) {
        node.setScope(parentScope);
        parentScope.addDeclaration(node);

        node.getTypeNode().setScope(parentScope);
        node.getIdentifierNode().setScope(parentScope);

        analyseType(node.getTypeNode(), parentScope);

        if (node.getExpressionNode() != null) {
            node.getExpressionNode().setScope(parentScope);
            analyseExpression(node.getExpressionNode(), parentScope);

            TypeNode typeNode = node.getExpressionNode().getResultType();

            if (node.getTypeNode() instanceof ObjectTypeNode && (typeNode instanceof ObjectTypeNode
                    || typeNode.equals(REF_TYPE))) {

                // ....
                return;
            }

            if (!typeNode.equals(node.getTypeNode())) {
                throw new IllegalArgumentException("Wrong types " + node.getTypeNode().toString() + " " +
                        node.getExpressionNode().toString() + " : " +
                        node.getTypeNode() + " and " + node.getExpressionNode().getResultType().toString());
            }
        }
    }

    private void analyseDeclarationInGlobalStart(DeclarationStatementNode node, Scope parentScope) {
        node.setScope(parentScope);
        parentScope.addDeclaration(node);

        node.getTypeNode().setScope(parentScope);
        node.getIdentifierNode().setScope(parentScope);
    }

    private void analyseDeclarationInGlobalEnd(DeclarationStatementNode node, Scope parentScope) {
        analyseType(node.getTypeNode(), parentScope);

        if (node.getExpressionNode() != null) {
            node.getExpressionNode().setScope(parentScope);
            analyseExpression(node.getExpressionNode(), parentScope);
        }
    }

    private void analyseFunction(FunctionDefinitionNode function, Scope parentScope) {
        function.setScope(parentScope);
        parentScope.addDeclaration(function);

        if (function.getStatementNode() != null) {
            functions.add(function);

            Scope scope = new Scope(parentScope);
            scope.setOwner(function);
            function.getFunctionNode().getParametersNode().getParameters()
                    .forEach(p -> {
                        analyseType(p.getTypeNode(), parentScope);
                        scope.addDeclaration(p);
                    });
            analyseStatement(function.getStatementNode(), scope);
        }
    }

    private void analyseFunctionInGlobalStart(FunctionDefinitionNode function, Scope parentScope) {
        function.setScope(parentScope);
        parentScope.addDeclaration(function);
    }

    private void analyseFunctionInGlobalEnd(FunctionDefinitionNode function, Scope parentScope) {
        if (function.getStatementNode() != null) {
            functions.add(function);
            Scope scope = new Scope(parentScope);
            scope.setOwner(function);
            function.getFunctionNode().getParametersNode().getParameters()
                    .forEach(p -> {
                        analyseType(p.getTypeNode(), parentScope);
                        scope.addDeclaration(p);
                    });
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

    private void analyseInterfaceInGlobalStart(InterfaceStatementNode interfaceStatementNode, Scope parentScope) {
        interfaceStatementNode.setScope(parentScope);
        parentScope.addDeclaration(interfaceStatementNode);

        Scope scope = new Scope(parentScope);
        interfaceStatementNode.setInnerScope(scope);
        scope.setOwner(interfaceStatementNode);

        interfaceStatementNode.getTranslationNode().getStatements().removeIf(s -> s instanceof EmptyStatementNode);

        for (StatementNode node : interfaceStatementNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalStart((ClassStatementNode) node, scope);
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalStart((InterfaceStatementNode) node, scope);
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalStart((FunctionDefinitionNode) node, scope);
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseInterfaceInGlobalMiddle(InterfaceStatementNode interfaceNode, Scope scope) {
        List<AstNode> extendsNode = interfaceNode.getExtendNames()
                .stream()
                .map(i -> {
                    AstNode node = scope.findDefinitionByVariable(i.getName());

                    if (node == null) {
                        throw new IllegalArgumentException("Undefined node");
                    } else if (node instanceof ClassStatementNode) {
                        interfaceNode.addExtendNode(node);
                        interfaceNode.getInnerScope().addAlternativeScope(((ClassStatementNode) node).getInnerScope());
                    } else if (node instanceof InterfaceStatementNode) {
                        interfaceNode.addExtendNode(node);
                        interfaceNode.getInnerScope().addAlternativeScope(((InterfaceStatementNode) node).getInnerScope());
                    }

                    return node;
                })
                .collect(Collectors.toList());
    }

    private void analyseInterfaceInGlobalEnd(InterfaceStatementNode interfaceStatementNode, Scope parentScope) {
        for (StatementNode node : interfaceStatementNode.getTranslationNode().getStatements()) {
            if (node instanceof ClassStatementNode) {
                analyseClassInGlobalEnd((ClassStatementNode) node, node.getScope());
            } else if (node instanceof InterfaceStatementNode) {
                analyseInterfaceInGlobalEnd((InterfaceStatementNode) node, node.getScope());
            } else if (node instanceof FunctionDefinitionNode) {
                analyseFunctionInGlobalEnd((FunctionDefinitionNode) node, node.getScope());
            } else {
                throw new IllegalStateException("Wrong statement");
            }
        }
    }

    private void analyseReturn(ReturnStatementNode node, Scope parentScope) {
        node.setScope(parentScope);

        FunctionDefinitionNode functionDefinitionNode = findFunction(parentScope);

        node.setFunction(functionDefinitionNode);
        functionDefinitionNode.addReturn(node);

        if (node.getExpressionNode() != null) {
            node.getExpressionNode().setScope(parentScope);
            analyseExpression(node.getExpressionNode(), parentScope);

            if (!functionDefinitionNode.getFunctionNode().getTypeNode()
                    .equals(node.getExpressionNode().getResultType())) {
                throw new IllegalArgumentException("Wrong type " +
                        functionDefinitionNode.getFunctionNode().getTypeNode().toString() + " "
                        + node.getExpressionNode().getResultType());
            }
        } else {
            if (!functionDefinitionNode.getFunctionNode().getTypeNode().equals(GlobalBasicType.VOID_TYPE)) {
                throw new IllegalArgumentException("Wrong type " +
                        functionDefinitionNode.getFunctionNode().getTypeNode().toString());
            }
        }
    }

    private FunctionDefinitionNode findFunction(Scope parentScope) {
        if (parentScope.getOwner() instanceof FunctionDefinitionNode) {
            return (FunctionDefinitionNode) parentScope.getOwner();
        } else {
            return findFunction(parentScope.getParentScope());
        }
    }

    private WhileStatementNode findCycle(Scope parentScope) {
        if (parentScope.getOwner() instanceof WhileStatementNode) {
            return (WhileStatementNode) parentScope.getOwner();
        } else {
            return findCycle(parentScope.getParentScope());
        }
    }

    private void analyseWhile(WhileStatementNode node, Scope scope) {
        node.getConditionNode().setScope(scope);
        analyseExpression(node.getConditionNode(), scope);

        Scope whileScope = new Scope(scope);
        whileScope.setOwner(node);
        analyseStatement(node.getBodyNode(), whileScope);
    }

    private void analyseExpression(ExpressionNode expressionNode, Scope parentScope) {
        if (expressionNode instanceof AssigmentExpressionNode) {
            analyseAssigmentExpression(expressionNode, parentScope);
        } else if (expressionNode instanceof ConditionalExpressionNode) {
            analyseConditionalExpression(expressionNode, parentScope);
        } else if (expressionNode instanceof AdditiveExpressionNode) {
            analyseAdditionalExpression(expressionNode, parentScope);
        } else if (expressionNode instanceof MultiplicativeExpressionNode) {
            analyseMultiplicativeExpression(expressionNode, parentScope);
        } else if (expressionNode instanceof LogicalAndExpressionNode) {
            analyseLogicalAndExpression((LogicalAndExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof LogicalOrExpressionNode) {
            analyseLogicalOrExpression((LogicalOrExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof RelationalExpressionNode) {
            analyseRelationalExpression((RelationalExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof EqualityExpressionNode) {
            analyseEqualityExpression((EqualityExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof VariableExpressionNode) {
            analyseVariableExpression((VariableExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof BoolConstantExpressionNode) {
            analyseBoolConstantExpression((BoolConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof IntConstantExpressionNode) {
            analyseIntConstantExpression((IntConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof FloatConstantExpressionNode) {
            analyseFloatConstantExpression((FloatConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof NullConstantExpressionNode) {
            analyseNullConstantExpression((NullConstantExpressionNode) expressionNode);
        } else if (expressionNode instanceof ArrayConstructorExpressionNode) {
            analyseArrayConstructorExpression((ArrayConstructorExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof FunctionCallExpressionNode) {
            analyseFunctionCallExpression((FunctionCallExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof ArrayAccessExpressionNode) {
            analyseArrayAccessExpression(expressionNode, parentScope);
        } else if (expressionNode instanceof FieldAccessExpressionNode) {
            analyseFieldAccessExpression((FieldAccessExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PostfixDecrementSubtractionExpressionNode) {
            analysePostfixDecrementExpression((PostfixDecrementSubtractionExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PostfixIncrementAdditiveExpressionNode) {
            analysePostfixIncrementExpression((PostfixIncrementAdditiveExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PostfixIncrementMultiplicativeExpressionNode) {
            analysePostfixMultiplicative((PostfixIncrementMultiplicativeExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PrefixDecrementSubtractionExpressionNode) {
            analysePrefixDecrement((PrefixDecrementSubtractionExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PrefixIncrementAdditiveExpressionNode) {
            analysePrefixIncrement((PrefixIncrementAdditiveExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof PrefixIncrementMultiplicativeExpressionNode) {
            analysePrefixMultiplicative((PrefixIncrementMultiplicativeExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof CastExpressionNode) {
            analyseCastExpression((CastExpressionNode) expressionNode, parentScope);
        } else if (expressionNode instanceof ObjectConstructorExpressionNode) {
            analyseObjectConstructorExpression((ObjectConstructorExpressionNode) expressionNode, parentScope);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private void analyseObjectConstructorExpression(ObjectConstructorExpressionNode expressionNode, Scope parentScope) {
        ObjectTypeNode typeNode = (ObjectTypeNode) expressionNode.getTypeNode();

        ClassStatementNode classStatementNode =
                (ClassStatementNode) parentScope.findDefinitionByVariable(typeNode.getIdentifierNode().getName());

        classStatementNode.getFields();

        expressionNode.setResultType(typeNode);
    }

    private void analyseCastExpression(CastExpressionNode expressionNode, Scope parentScope) {
        CastExpressionNode castExpressionNode = expressionNode;

        ExpressionNode node = castExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        castExpressionNode.setResultType(castExpressionNode.getTypeNode());
    }

    private void analysePrefixMultiplicative(PrefixIncrementMultiplicativeExpressionNode expressionNode, Scope parentScope) {
        PrefixIncrementMultiplicativeExpressionNode prefixIncrementMultiplicativeExpressionNode =
                expressionNode;

        ExpressionNode node = prefixIncrementMultiplicativeExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analysePrefixIncrement(PrefixIncrementAdditiveExpressionNode expressionNode, Scope parentScope) {
        PrefixIncrementAdditiveExpressionNode prefixIncrementAdditiveExpressionNode =
                expressionNode;

        ExpressionNode node = prefixIncrementAdditiveExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analysePrefixDecrement(PrefixDecrementSubtractionExpressionNode expressionNode, Scope parentScope) {
        PrefixDecrementSubtractionExpressionNode prefixDecrementSubtractionExpressionNode =
                expressionNode;

        ExpressionNode node = prefixDecrementSubtractionExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analysePostfixMultiplicative(PostfixIncrementMultiplicativeExpressionNode expressionNode, Scope parentScope) {
        PostfixIncrementMultiplicativeExpressionNode postfixIncrementMultiplicativeExpressionNode =
                expressionNode;

        ExpressionNode node = postfixIncrementMultiplicativeExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analysePostfixIncrementExpression(PostfixIncrementAdditiveExpressionNode expressionNode, Scope parentScope) {
        PostfixIncrementAdditiveExpressionNode postfixIncrementAdditiveExpressionNode =
                expressionNode;

        ExpressionNode node = postfixIncrementAdditiveExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analysePostfixDecrementExpression(PostfixDecrementSubtractionExpressionNode expressionNode, Scope parentScope) {
        PostfixDecrementSubtractionExpressionNode postfixDecrementSubtractionExpressionNode =
                expressionNode;

        ExpressionNode node = postfixDecrementSubtractionExpressionNode.getExpressionNode();
        analyseExpression(node, parentScope);

        expressionNode.setResultType(node.getResultType());
        if (!node.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + node.toString());
        }
    }

    private void analyseFieldAccessExpression(FieldAccessExpressionNode expressionNode, Scope parentScope) {
        FieldAccessExpressionNode fieldAccessExpressionNode = expressionNode;

        ExpressionNode left = fieldAccessExpressionNode.getLeft();
        ExpressionNode right = fieldAccessExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);
    }

    private void analyseArrayAccessExpression(ExpressionNode expressionNode, Scope parentScope) {
        ArrayAccessExpressionNode arrayAccessExpressionNode = (ArrayAccessExpressionNode) expressionNode;

        ExpressionNode array = arrayAccessExpressionNode.getArray();
        ExpressionNode argument = arrayAccessExpressionNode.getArgument();

        analyseExpression(array, parentScope);
        analyseExpression(argument, parentScope);

        if (!argument.getResultType().equals(GlobalBasicType.INT_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + argument.toString());
        }

        expressionNode.setResultType(((ArrayTypeNode) array.getResultType()).getTypeNode());
    }

    private void analyseFunctionCallExpression(FunctionCallExpressionNode expressionNode, Scope parentScope) {
        FunctionCallExpressionNode functionCallExpressionNode = expressionNode;

        ExpressionNode function = functionCallExpressionNode.getFunction();
        analyseExpression(function, parentScope);

        TypeNode typeNode = function.getResultType();

        if (typeNode instanceof FunctionNode) {
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
        } else if (typeNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) typeNode;
            if (objectTypeNode.getDefinitionNode() instanceof ClassStatementNode) {
                ClassStatementNode classStatementNode = (ClassStatementNode) objectTypeNode.getDefinitionNode();
                functionCallExpressionNode.setTarget(classStatementNode.getConstructors()
                        .stream()
                        .findFirst()
                        .orElseThrow(RuntimeException::new));
            }
            functionCallExpressionNode.setResultType(objectTypeNode);
        }
    }

    private void analyseArrayConstructorExpression(ArrayConstructorExpressionNode expressionNode, Scope parentScope) {
        ArrayConstructorExpressionNode arrayConstructorExpressionNode = expressionNode;

        analyseType(arrayConstructorExpressionNode.getTypeNode(), parentScope);

        List<ExpressionNode> sizeExpression = arrayConstructorExpressionNode.getSizeExpression();

        sizeExpression.forEach(e -> analyseExpression(e, parentScope));
        ArrayTypeNode arrayTypeNode = null;

        for (ExpressionNode node : sizeExpression) {
            if (arrayTypeNode == null) {
                arrayTypeNode = new ArrayTypeNode(arrayConstructorExpressionNode.getTypeNode());
            } else {
                arrayTypeNode = new ArrayTypeNode(arrayTypeNode);
            }
        }

        arrayConstructorExpressionNode.setResultType(arrayTypeNode);
    }

    private void analyseNullConstantExpression(NullConstantExpressionNode expressionNode) {
        NullConstantExpressionNode nullConstantExpressionNode = expressionNode;
    }

    private void analyseFloatConstantExpression(FloatConstantExpressionNode expressionNode) {
        FloatConstantExpressionNode floatConstantExpressionNode = expressionNode;
    }

    private void analyseIntConstantExpression(IntConstantExpressionNode expressionNode) {
        IntConstantExpressionNode intConstantExpressionNode = expressionNode;
    }

    private void analyseBoolConstantExpression(BoolConstantExpressionNode expressionNode) {
        BoolConstantExpressionNode boolConstantExpressionNode = expressionNode;
    }

    private void analyseVariableExpression(VariableExpressionNode expressionNode, Scope parentScope) {
        VariableExpressionNode variableExpressionNode = expressionNode;
        String name = expressionNode.getIdentifierNode().getName();
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
            ClassStatementNode classStatementNode = (ClassStatementNode) variableNode;
            ObjectTypeNode objectTypeNode = new ObjectTypeNode(classStatementNode.getIdentifierNode());
            objectTypeNode.setDefinition(classStatementNode);
            variableExpressionNode.setResultType(objectTypeNode);
            variableExpressionNode.setIdentifier(classStatementNode.getIdentifierNode());
        } else if (variableNode instanceof InterfaceStatementNode) {
            InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode) variableNode;
            variableExpressionNode.setResultType(new ObjectTypeNode(
                    interfaceStatementNode.getIdentifierNode()));
            variableExpressionNode.setIdentifier(interfaceStatementNode.getIdentifierNode());
        } else {
            throw new IllegalArgumentException("Unknown");
        }
    }

    private void analyseEqualityExpression(EqualityExpressionNode expressionNode, Scope parentScope) {
        EqualityExpressionNode equalityExpressionNode = expressionNode;

        ExpressionNode left = equalityExpressionNode.getLeft();
        ExpressionNode right = equalityExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);

        if (!left.getResultType().equals(right.getResultType())) {
            throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
        }

        equalityExpressionNode.setResultType(GlobalBasicType.BOOL_TYPE);
    }

    private void analyseRelationalExpression(RelationalExpressionNode expressionNode, Scope parentScope) {
        RelationalExpressionNode relationalExpressionNode = expressionNode;

        ExpressionNode left = relationalExpressionNode.getLeft();
        ExpressionNode right = relationalExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);

        if (!left.getResultType().equals(right.getResultType())) {
            throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString());
        }

        relationalExpressionNode.setResultType(GlobalBasicType.BOOL_TYPE);
    }

    private void analyseLogicalOrExpression(LogicalOrExpressionNode expressionNode, Scope parentScope) {
        LogicalOrExpressionNode logicalOrExpressionNode = expressionNode;

        ExpressionNode left = logicalOrExpressionNode.getLeft();
        ExpressionNode right = logicalOrExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);

        if (!left.getResultType().equals(GlobalBasicType.BOOL_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + left.toString());
        }

        if (!right.getResultType().equals(GlobalBasicType.BOOL_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + right.toString());
        }

        logicalOrExpressionNode.setResultType(GlobalBasicType.BOOL_TYPE);
    }

    private void analyseLogicalAndExpression(LogicalAndExpressionNode expressionNode, Scope parentScope) {
        LogicalAndExpressionNode logicalAndExpressionNode = expressionNode;

        ExpressionNode left = logicalAndExpressionNode.getLeft();
        ExpressionNode right = logicalAndExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);

        if (!left.getResultType().equals(GlobalBasicType.BOOL_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + left.toString());
        }

        if (!right.getResultType().equals(GlobalBasicType.BOOL_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + right.toString());
        }

        logicalAndExpressionNode.setResultType(GlobalBasicType.BOOL_TYPE);
    }

    private void analyseMultiplicativeExpression(ExpressionNode expressionNode, Scope parentScope) {
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
    }

    private void analyseAdditionalExpression(ExpressionNode expressionNode, Scope parentScope) {
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
    }

    private void analyseConditionalExpression(ExpressionNode expressionNode, Scope parentScope) {
        ConditionalExpressionNode conditionalExpressionNode = (ConditionalExpressionNode) expressionNode;

        ExpressionNode cond = conditionalExpressionNode.getConditionNode();
        ExpressionNode then = conditionalExpressionNode.getThenNode();
        ExpressionNode els = conditionalExpressionNode.getElseNode();

        analyseExpression(cond, parentScope);
        analyseExpression(then, parentScope);
        analyseExpression(els, parentScope);

        if (!cond.getResultType().equals(GlobalBasicType.BOOL_TYPE)) {
            throw new IllegalArgumentException("Wrong types " + cond.toString());
        }

        if (!then.getResultType().equals(els.getResultType())) {
            throw new IllegalArgumentException("Wrong types " + then.toString() + " " + els.toString());
        }

        expressionNode.setResultType(then.getResultType());
    }

    private void analyseAssigmentExpression(ExpressionNode expressionNode, Scope parentScope) {
        AssigmentExpressionNode assigmentExpressionNode = (AssigmentExpressionNode) expressionNode;

        ExpressionNode left = assigmentExpressionNode.getLeft();
        ExpressionNode right = assigmentExpressionNode.getRight();

        analyseExpression(left, parentScope);
        analyseExpression(right, parentScope);

        TypeNode typeNode = null;
        if (left.getResultType() instanceof BasicTypeNode
                && right.getResultType() instanceof BasicTypeNode) {
            typeNode = defineBinaryOperationType(expressionNode, left, right);
        } else if (left.getResultType() instanceof ObjectTypeNode
                && right.getResultType() instanceof ObjectTypeNode) {
            if (left.getResultType().equals(right.getResultType())) {
                typeNode = left.getResultType();
            }
        } else if (left.getResultType() instanceof ObjectTypeNode && right.getResultType() == REF_TYPE) {
            typeNode = left.getResultType();
        } else if (left.getResultType() instanceof ArrayTypeNode) {
            typeNode = left.getResultType();
        }

        if (typeNode == null) {
            throw new IllegalArgumentException("Wrong types " + left.toString() + " " + right.toString() + " : " +
                    left.getResultType().toString() + " and " + right.getResultType().toString());
        }
    }

    private void analyseType(TypeNode typeNode, Scope parentScope) {
        if (typeNode instanceof ArrayTypeNode) {
            ArrayTypeNode arrayTypeNode = (ArrayTypeNode) typeNode;
            analyseType(arrayTypeNode.getTypeNode(), parentScope);
        } else if (typeNode instanceof ObjectTypeNode) {
            ObjectTypeNode objectTypeNode = (ObjectTypeNode) typeNode;
            String name = objectTypeNode.getIdentifierNode().getName();
            AstNode variableNode = parentScope.findDefinitionByVariable(name);

            if (variableNode == null) {
                throw new IllegalArgumentException("Undefined type " + name);
            }

            if (variableNode instanceof ClassStatementNode) {
                ClassStatementNode classStatementNode = (ClassStatementNode) variableNode;
                objectTypeNode.setDefinition(classStatementNode);
            } else if (variableNode instanceof InterfaceStatementNode) {
                InterfaceStatementNode interfaceStatementNode = (InterfaceStatementNode) variableNode;
                objectTypeNode.setDefinition(interfaceStatementNode);
            } else {
                throw new IllegalArgumentException("Unknown");
            }
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
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.FLOAT
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(leftType);
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
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.BOOL) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.CHAR) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.SHORT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.INT) {
            expressionNode.setResultType(leftType);
        } else if (leftType.getType() == TypeNode.Type.DOUBLE
                && rightType.getType() == TypeNode.Type.LONG) {
            expressionNode.setResultType(leftType);
        }

        return expressionNode.getResultType();
    }
}
