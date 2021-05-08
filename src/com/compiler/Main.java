package com.compiler;

import com.compiler.ast.FunctionsNode;
import com.compiler.ir.Driver;
import com.compiler.lexer.Lexer;
import com.compiler.lexer.Token;
import com.compiler.parser.Parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Reader reader = new FileReader("test");
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        FunctionsNode functionsNodes = parser.parse();
        Driver.drive(functionsNodes);

        System.out.println(functionsNodes.astDebug());
    }
}
