package lang;

import lang.ast.TranslationNode;
import lang.lexer.Lexer;
import lang.lexer.Token;
import lang.parser.Parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Reader reader = new FileReader("test");
        Lexer lexer = new Lexer(reader);

        Parser parser = new Parser(lexer, "name");
        TranslationNode translationNode = parser.parse();

        System.out.println(translationNode.astDebug(0));
    }
}

