package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.TranslationNode;
import lang.scope.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InterfaceStatementNode extends StatementNode {
    private final IdentifierNode identifierNode;
    private final TranslationNode translationNode;
    private final List<IdentifierNode> extendNames;
    private final List<AstNode> extendNodes = new ArrayList<>();
    private Scope innerScope;

    public InterfaceStatementNode(IdentifierNode identifierNode,
                                  List<IdentifierNode> extendNames,
                                  TranslationNode translationNode) {
        this.identifierNode = identifierNode;
        this.extendNames = extendNames;
        this.translationNode = translationNode;
    }

    public void addExtendNode(AstNode astNode) {
        this.extendNodes.add(astNode);
    }

    public List<AstNode> getExtendNodes() {
        return extendNodes;
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

    @Override
    public String astDebug(int shift) {

        return SHIFT.repeat(shift) + "InterfaceStatement: \n" +
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
        return "interface " + identifierNode.toString() + " : " +
                extendNames.stream().map(Objects::toString).collect(Collectors.joining(",")) + "\n" +
                translationNode.toString();
    }
}
