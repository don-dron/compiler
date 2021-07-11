package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.TranslationNode;
import lang.semantic.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClassStatementNode extends StatementNode {
    private final IdentifierNode identifierNode;
    private final List<IdentifierNode> extendNames;
    private final List<DeclarationStatementNode> fields;
    private final List<AstNode> extendNodes = new ArrayList<>();
    private final TranslationNode translationNode;
    private final List<ConstructorDefinitionNode> constructors = new ArrayList<>();
    private Scope innerScope;

    public ClassStatementNode(IdentifierNode identifierNode,
                              List<IdentifierNode> extendNames,
                              TranslationNode translationNode) {
        this.identifierNode = identifierNode;
        this.extendNames = extendNames;
        this.fields = new ArrayList<>();
        this.translationNode = translationNode;
    }

    public void setInnerScope(Scope innerScope) {
        this.innerScope = innerScope;
    }

    public Scope getInnerScope() {
        return innerScope;
    }

    public List<IdentifierNode> getExtendNames() {
        return extendNames;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public TranslationNode getTranslationNode() {
        return translationNode;
    }

    public void addExtendNode(AstNode astNode) {
        this.extendNodes.add(astNode);
    }

    public List<AstNode> getExtendNodes() {
        return extendNodes;
    }

    public void addConstructor(ConstructorDefinitionNode constructorDefinitionNode) {
        constructors.add(constructorDefinitionNode);
    }

    public List<ConstructorDefinitionNode> getConstructors() {
        return constructors;
    }

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift) + "ClassStatement: \n" +
                identifierNode.astDebug(shift + 1) + "\n" +
                extendNames.stream().map(e -> e.astDebug(shift + 1) + "\n").collect(Collectors.joining()) +
                translationNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(translationNode);
    }

    @Override
    public String toString() {
        return "class " + identifierNode.toString() + " : " +
                extendNames.stream().map(Objects::toString).collect(Collectors.joining(",")) + "\n" +
                translationNode.toString();
    }

    public List<DeclarationStatementNode> getFields() {
        return fields;
    }

    public void addField(DeclarationStatementNode node) {
        this.fields.add(node);
    }
}
