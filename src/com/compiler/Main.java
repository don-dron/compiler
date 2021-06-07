package com.compiler;

import com.compiler.ast.FunctionsNode;
import com.compiler.ir.drive.Driver;
import com.compiler.ir.Module;
import com.compiler.ir.drive.Translator;
import com.compiler.lexer.Lexer;
import com.compiler.parser.Parser;

import java.io.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Reader reader = new FileReader("test");
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        File file = new File("out.ll");
        file.deleteOnExit();
        file.createNewFile();

        FunctionsNode functionsNodes = parser.parse();

        System.out.println(functionsNodes.astDebug());
        Translator translator = new Translator();
        Module module = translator.drive(functionsNodes);
        String dump = Driver.moduleToString(module);

        System.out.println(dump);
        String graphViz = "digraph G {\n";
        graphViz += module.getFunctionBlocks().stream()
                .map(Driver::graphVizDebug).collect(Collectors.joining("\n"));

        graphViz += "}";
        System.out.println(graphViz);

        File graphVizFile = new File("gv.gv");
        graphVizFile.deleteOnExit();
        graphVizFile.createNewFile();
        FileWriter gvWriter = new FileWriter(graphVizFile);
        gvWriter.write(graphViz);
        gvWriter.flush();

        runGraphViz(graphVizFile);

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
