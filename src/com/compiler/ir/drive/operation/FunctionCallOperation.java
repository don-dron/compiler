package com.compiler.ir.drive.operation;

import com.compiler.ast.IdentifierNode;
import com.compiler.ir.drive.value.Value;
import com.compiler.ir.drive.value.VariableValue;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallOperation extends Operation {
    private final IdentifierNode identifierNode;
    private final List<Value> values;
    private final VariableValue result;
    private FunctionCallOperation ssaVariant;

    public FunctionCallOperation(IdentifierNode identifierNode, List<Value> values, VariableValue result) {
        this.identifierNode = identifierNode;
        this.values = values;
        this.result = result;
    }

    @Override
    public Value getResult() {
        return result;
    }

    @Override
    public boolean hasSsaForm() {
        return ssaVariant != null;
    }

    @Override
    public Operation getSsa() {
        return ssaVariant;
    }

    @Override
    public String toString() {
        return (result == null ? "" : result.toCode() + " = ") +
                "call " + (result != null ? result.getType().toCode() : "void") + " @" + identifierNode.getName() + "(" +
                values.stream().map(value -> value.getType().toCode() + " " +
                        value.toCode()).collect(Collectors.joining(","))
                + ")";
    }
}
