package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.TranslationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClassStatementNode extends StatementNode {
    private final IdentifierNode identifierNode;
    private final List<IdentifierNode> extend;
    private final TranslationNode translationNode;
    private final List<ConstructorDefinitionNode> constructors = new ArrayList<>();

    public ClassStatementNode(IdentifierNode identifierNode,
                              List<IdentifierNode> extend,
                              TranslationNode translationNode) {
        this.identifierNode = identifierNode;
        this.extend = extend;
        this.translationNode = translationNode;
    }

    public List<IdentifierNode> getExtend() {
        return extend;
    }

    public IdentifierNode getIdentifierNode() {
        return identifierNode;
    }

    public TranslationNode getTranslationNode() {
        return translationNode;
    }

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift) + "ClassStatement: \n" +
                identifierNode.astDebug(shift + 1) + "\n" +
                extend.stream().map(e -> e.astDebug(shift + 1) + "\n").collect(Collectors.joining()) +
                translationNode.astDebug(shift + 1);
    }

    public void addConstructor(ConstructorDefinitionNode constructorDefinitionNode) {
        constructors.add(constructorDefinitionNode);
    }

    public List<ConstructorDefinitionNode> getConstructors() {
        return constructors;
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(translationNode);
    }

    @Override
    public String toString() {
        return "class " + identifierNode.toString() + " : " +
                extend.stream().map(Objects::toString).collect(Collectors.joining(",")) + "\n" +
                translationNode.toString();
    }
}
