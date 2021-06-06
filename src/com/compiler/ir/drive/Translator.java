package com.compiler.ir.drive;

import com.compiler.ast.FunctionsNode;
import com.compiler.ir.Module;

import java.util.stream.Collectors;

public class Translator {

    public Module drive(FunctionsNode functionsNode) {
        return new Module(functionsNode.getFunctionNodes()
                .stream()
                .map(f -> new Driver().driveFunction(f))
                .collect(Collectors.toList()));
    }

}
