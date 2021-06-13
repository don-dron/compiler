package lang;

import lang.ast.FileNode;
import lang.ast.TranslationNode;
import lang.lexer.Lexer;
import lang.parser.Parser;
import lang.semantic.SemanticAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        List<FileNode> files = new ArrayList<>();
        for(File file : Objects.requireNonNull(new File("project").listFiles())) {
            Reader reader = new FileReader(file);
            Lexer lexer = new Lexer(reader);

            Parser parser = new Parser(lexer, file.getPath());
            FileNode fileNode = parser.parse();

            files.add(fileNode);
        }

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis(files);
        semanticAnalysis.analyse();
    }
}

