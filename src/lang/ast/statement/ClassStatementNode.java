package lang.ast.statement;

import lang.ast.AstNode;
import lang.ast.IdentifierNode;
import lang.ast.TranslationNode;
import lang.ast.statement.StatementNode;

import java.util.List;

public class ClassStatementNode extends StatementNode {
    private final IdentifierNode identifierNode;
    private final TranslationNode translationNode;

    public ClassStatementNode(IdentifierNode identifierNode,
                                  TranslationNode translationNode) {
        this.identifierNode = identifierNode;
        this.translationNode = translationNode;
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
                translationNode.astDebug(shift + 1);
    }

    @Override
    public List<? extends AstNode> getChildren() {
        return List.of(translationNode);
    }
}
