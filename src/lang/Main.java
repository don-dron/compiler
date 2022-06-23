package lang;

import lang.ast.FileNode;
import lang.ir.Module;
import lang.ir.translate.Translator;
import lang.lexer.Lexer;
import lang.lr.LLVMTranslator;
import lang.opt.Optimizer;
import lang.parser.Parser;
import lang.semantic.SemanticAnalysis;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final String INPUT = "input";
    public static final String AST_TREE_DEBUG = "astTreeDebug";
    public static final String IR_DEBUG = "IRDebug";
    public static final String IR_DOT_GRAPH = "IRDotGraph";
    public static final String OUTPUT = "output";
    public static final String MODE = "mode";

    public static void main(String[] args) throws IOException, InterruptedException {

        Options options = new Options();

        Option input = new Option("i", INPUT, true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option astTreeDebug = new Option("ast", AST_TREE_DEBUG, false, "enable ast tree debug");
        astTreeDebug.setRequired(false);
        options.addOption(astTreeDebug);

        Option irDebug = new Option("ir", IR_DEBUG, false, "enable ir debug");
        irDebug.setRequired(false);
        options.addOption(irDebug);

        Option irGraph = new Option("irDot", IR_DOT_GRAPH, false, "build ir dot graph");
        irGraph.setRequired(false);
        options.addOption(irGraph);

        Option output = new Option("o", OUTPUT, true, "output file");
        output.setRequired(false);
        options.addOption(output);

        Option mode = new Option("m", MODE, true, "work mode");
        mode.setRequired(false);
        options.addOption(mode);

        CommandLineParser cmdParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = cmdParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Compiler", options);

            System.exit(1);
        }

        List<FileNode> files = new ArrayList<>();
        File root = new File(cmd.getOptionValue(INPUT));

        List<File> raws = getFiles(root);
        raws.add(new File("lang/lib"));

        for (File childFile : raws) {
            Reader reader = new FileReader(childFile);
            Lexer lexer = new Lexer(reader);

            Parser parser = new Parser(lexer, childFile.getAbsolutePath(), childFile.getPath());
            FileNode fileNode = parser.parse();
            files.add(fileNode);

            if (cmd.hasOption(AST_TREE_DEBUG)) {
                System.out.println(fileNode.astDebug(0));
            }
        }

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis(
                root.getPath(),
                files);

        Translator translator = new Translator(semanticAnalysis.analyse());
        Module module = translator.translate();

        Optimizer optimizer = new Optimizer(module);
        optimizer.optimize();

        if (cmd.hasOption(IR_DOT_GRAPH)) {
            String picturePath = Optional.ofNullable(cmd.getOptionValue(IR_DOT_GRAPH)).orElse("ir.dot.dump");

            AtomicInteger clusterId = new AtomicInteger();

            String digraphDebug = "digraph G {\n" +
                    module.getFunctions().stream()
                            .map(f -> Translator.graphVizDebug(f, clusterId.getAndIncrement()))
                            .collect(Collectors.joining("\n")) + "\n}";

            File dotFile = new File(picturePath);
            FileWriter fileWriter = new FileWriter(dotFile);

            fileWriter.write(digraphDebug);
            fileWriter.flush();

            runGraphViz(dotFile);
        }

        LLVMTranslator llvmTranslator = new LLVMTranslator(module);

        String dump = llvmTranslator.translate();

        if (cmd.hasOption(IR_DEBUG)) {
            System.out.println(dump);
        }

        File llFile = new File("out.ll");
        File binaryFile = new File("out.o");
        File executableFile = new File(cmd.hasOption(OUTPUT) ? cmd.getOptionValue(OUTPUT) : "program");

        FileWriter fileWriter = new FileWriter(llFile);
        fileWriter.write(dump);
        fileWriter.flush();
//
//         buildLibrary();
//
//         runLLVM(llFile);
//         runClang(binaryFile, executableFile);
//
//         if (!cmd.hasOption(MODE)
//                 || cmd.getOptionValue(MODE) == null
//                 || cmd.getOptionValue(MODE).equals("buildAndRun")) {
//             runProgram(executableFile);
//         } else if (cmd.hasOption(MODE) &&
//                 Optional.ofNullable(cmd.getOptionValue(MODE)).orElse("buildAndRun").equals("valgrindDebug")) {
//             runWithValgrind(executableFile);
//         }
    }

    private static void createAndWrite(String path, String content) throws IOException {
        File file = new File(path);
        FileWriter fileWriter = new FileWriter(file);

        fileWriter.write(content);
        fileWriter.flush();
    }

    private static List<File> getFiles(File root) {
        List<File> files = new ArrayList<>();

        if (!root.isDirectory()) {
            files.add(root);
            return files;
        } else {
            for (File file : Stream.ofNullable(root.listFiles())
                    .flatMap(Stream::of)
                    .collect(Collectors.toList())) {
                files.addAll(getFiles(file));
            }
        }
        return files;
    }

    private static void buildLibrary() throws IOException, InterruptedException {

        if (new File("./build").exists()) {
            ProcessBuilder deleteBuilder = new ProcessBuilder("rm", "-rf", "build");
            runProcess(deleteBuilder);
        }

        ProcessBuilder mkdirBuilder = new ProcessBuilder("mkdir", "build");
        runProcess(mkdirBuilder);
        List<File> sources =
                getFiles(new File("./lib"));
        List<String> outputs = new ArrayList<>();
        for (File file : sources) {
            String output = "./build/" + file.getName().replaceAll("\\.c|\\.S", ".o");
            outputs.add(output);
            String[] params =
                    Stream.of(
                                    "gcc",
                                    "-O3",
                                    "-c",
                                    "-pthread",
                                    "-Wall",
                                    "-I",
                                    "./include",
                                    SystemUtils.IS_OS_LINUX ? "-D IS_LINUX" : "",
                                    "-o",
                                    output,
                                    file.getPath())
                            .toArray(String[]::new);
            ProcessBuilder procBuilder = new ProcessBuilder(params);
            runProcess(procBuilder);
        }

        String[] llvmParams = Stream.concat(Stream.of(
                        "ar",
                        "rcs",
                        "lib.a"),
                outputs.stream()).toArray(String[]::new);
        ProcessBuilder llvmProcBuilder = new ProcessBuilder(llvmParams);
        runProcess(llvmProcBuilder);
    }

    private static void runGraphViz(File file) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("dot", "-Tpng", file.getName(), "-o", "IRGraph.png");
        runProcess(procBuilder);
    }

    private static void runClang(File bf, File prog) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("gcc",
                bf.getName(), "lib.a",
                SystemUtils.IS_OS_LINUX ? "-lpthread" : "",
                SystemUtils.IS_OS_LINUX ? "-D IS_LINUX" : "",
                "-o", prog.getName());
        runProcess(procBuilder);
    }

    private static void runLLVM(File file) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("llc", "-filetype=obj", "--relocation-model=pic",file.getName());
        runProcess(procBuilder);
    }

    private static void runProgram(File prog) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("./" + prog.getName());
        runProcess(procBuilder);
    }

    private static void runWithValgrind(File prog) throws IOException, InterruptedException {
        ProcessBuilder procBuilder = new ProcessBuilder("valgrind ./" + prog.getName());
        runProcess(procBuilder);
    }

    private static void runProcess(ProcessBuilder procBuilder) throws IOException, InterruptedException {
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

        System.err.println("Exit code = " + exitVal + " for " +
                String.join(" ", procBuilder.command()));
    }
}

