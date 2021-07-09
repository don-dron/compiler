package lang;

import lang.ast.FileNode;
import lang.ir.Module;
import lang.ir.translate.Translator;
import lang.lexer.Lexer;
import lang.lr.LLVMTranslator;
import lang.parser.Parser;
import lang.semantic.SemanticAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        List<FileNode> files = new ArrayList<>();
        File root = new File("project1");
        for (File file : getFiles(root)) {
            Reader reader = new FileReader(file);
            Lexer lexer = new Lexer(reader);

            Parser parser = new Parser(lexer, file.getAbsolutePath(), file.getPath());
            FileNode fileNode = parser.parse();

            files.add(fileNode);
        }

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis(
                root.getAbsolutePath(),
                files);

        Translator translator = new Translator(semanticAnalysis.analyse());
        Module module = translator.translate();

        LLVMTranslator llvmTranslator = new LLVMTranslator(module);

        System.out.println(llvmTranslator.translate());

        System.out.println("End");
    }

    private static List<File> getFiles(File root) {
        List<File> files = new ArrayList<>();

        if (!root.isDirectory()) {
            return List.of(root);
        } else {
            for (File file : Stream.ofNullable(root.listFiles())
                    .flatMap(Stream::of)
                    .collect(Collectors.toList())) {
                files.addAll(getFiles(file));
            }
        }
        return files;
    }
}

