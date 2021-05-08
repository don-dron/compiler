package com.compiler.ast;

import java.util.Map;
import java.util.stream.Collectors;

public class ParameterNode extends AstNode {
    private final Map<IdentifierNode, TypeNode> map;

    public ParameterNode(Map<IdentifierNode, TypeNode> map) {
        this.map = map;
    }

    public Map<IdentifierNode, TypeNode> getMap() {
        return map;
    }

    @Override
    public String astDebug(int shift) {
        return (SHIFT.repeat(shift) + "Parameters:\n" +
                map.entrySet()
                        .stream()
                        .map(entry -> SHIFT.repeat(shift+1) + entry.getKey().getName() +
                                " -> " + entry.getValue().getType())
                        .collect(Collectors.joining("\n")))
                .stripTrailing();
    }
}
