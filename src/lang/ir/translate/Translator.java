package lang.ir.translate;

import lang.ast.Program;
import lang.ast.statement.FunctionDefinitionNode;
import lang.ir.Function;
import lang.ir.Module;

import java.util.stream.Collectors;

public class Translator {
    private final Program program;

    public Translator(Program program) {
        this.program = program;
    }

    public Module translate() {
        return new Module(program.getFunctions()
                .stream()
                .map(this::translateFunction)
                .collect(Collectors.toList()));
    }

    private Function translateFunction(FunctionDefinitionNode functionDefinitionNode) {
        return new Function();
    }
}
