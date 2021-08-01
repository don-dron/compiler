package lang;

import lang.ast.FileNode;
import lang.ir.Module;
import lang.ir.translate.Translator;
import lang.lexer.Lexer;
import lang.lr.LLVMTranslator;
import lang.opt.Optimizer;
import lang.parser.Parser;
import lang.semantic.SemanticAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<FileNode> files = new ArrayList<>();
        File root = new File("project2");
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

        Optimizer optimizer = new Optimizer(module);
        optimizer.optimize();

        String s = "digraph G {\n" +
                module.getFunctions().stream()
                        .map(Translator::graphVizDebug)
                        .collect(Collectors.joining("\n")) + "\n}";

        System.out.println(s);


        LLVMTranslator llvmTranslator = new LLVMTranslator(module);

        String dump = llvmTranslator.translate();
        System.out.println(dump);
        System.out.println("End");

        File file = new File("out.ll");
        file.deleteOnExit();
        file.createNewFile();

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(dump);
        fileWriter.flush();

        File bf = new File("out.o");
        bf.deleteOnExit();
        bf.createNewFile();
        runLLVM(file);

        File prog = new File("prog");
        prog.deleteOnExit();
        prog.createNewFile();

        runClang(bf, prog);

        runProgram(prog);
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

    private static void runGraphViz(File file) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("dot", "-Tpng", file.getName(), "-o", "graph.png");
        runProcesss(procBuilder);
    }

    private static void runClang(File bf, File prog) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("clang", bf.getName(), "-o", prog.getName());
        runProcesss(procBuilder);
    }

    private static void runLLVM(File file) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("llc", "-filetype=obj", file.getName());
        runProcesss(procBuilder);
    }

    private static void runProgram(File prog) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("./" + prog.getName());
        runProcesss(procBuilder);
    }

    private static void runProcesss(ProcessBuilder procBuilder) throws IOException, InterruptedException {
        procBuilder.redirectErrorStream(true);

        Process process = procBuilder.start();

        InputStream stdout = process.getInputStream();
        InputStreamReader isrStdout = new InputStreamReader(stdout);
        BufferedReader brStdout = new BufferedReader(isrStdout);

        String line = null;
        while ((line = brStdout.readLine()) != null) {
            System.out.println(line);
        }

        int exitVal = process.waitFor();

        System.out.println("Exit code = " + exitVal + " for " +
                String.join(" ", procBuilder.command()));
    }
}

