package lang.lr;

import lang.ir.BasicBlock;
import lang.ir.Function;
import lang.ir.Module;
import lang.ir.Type;

import java.util.stream.Collectors;

import static lang.ir.Type.VOID;

public class LLVMTranslator {

    private final Module module;

    public LLVMTranslator(Module module) {
        this.module = module;
    }

    public String translate() {
        StringBuilder builder = new StringBuilder();
        builder.append("; ModuleID = 'main'\n" +
                "source_filename = \"main\"\n")
        .append("declare i32* @malloc(i32)\n");
        builder.append(module.getFunctions()
                .stream()
                .map(this::translateFunction)
                .collect(Collectors.joining("\n")));

        return builder.toString();
    }

    private String translateFunction(Function function) {
        StringBuilder builder = new StringBuilder();

        builder
                .append("define")
                .append(" ")
                .append(function.getType() == VOID ? "void" : "i32")
                .append(" ")
                .append("@")
                .append(function.getName())
                .append("(")
                .append(function.getParameterTypes()
                        .stream()
                        .map(Type::toLLVM)
                        .collect(Collectors.joining(",")))
                .append(")")
                .append("{\n");

        builder.append(function.getBlocks()
                .stream()
                .map(this::translateBlock)
                .collect(Collectors.joining("\n")));

        builder.append("\n}");

        return builder.toString();
    }

    private String translateBlock(BasicBlock b) {
        StringBuilder builder = new StringBuilder();

        builder.append("\t").append(b.getName()).append(":\n");

        builder.append(
                b.getCommands()
                        .stream()
                        .map(c -> "\t\t" + c.toLLVM())
                        .collect(Collectors.joining("\n"))
        );

        builder.append("\n\t\t").append(b.getTerminator().toLLVM());

        return builder.toString();
    }
}
