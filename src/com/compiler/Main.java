package com.compiler;

import com.compiler.ast.FunctionsNode;
import com.compiler.ir.Driver;
import com.compiler.lexer.Lexer;
import com.compiler.parser.Parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Reader reader = new FileReader("test");
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        FunctionsNode functionsNodes = parser.parse();

        System.out.println(functionsNodes.astDebug());
        Driver.drive(functionsNodes);
    }
}
