package AnyPercent;

//BIG NOTE: There is a Print statement inside the Lex function that makes printing lexemes FAR more readable, so thats what I have.
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import Environment.Environment;
import Evaluation.Evaluator;
import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Lexer;
import Recognizing.Parser;

import static LexicalAnalysis.Type.*;



public class AnyPercent {
private static final ArrayList<String> syntaxErrorMessages = new ArrayList<>(); 
private static final ArrayList<String> runtimeErrorMessages = new ArrayList<>(); 

    public static void main (String[] args) throws IOException {
        
        try {
            if (args.length == 1) runFile(args[0]);
            else {
                System.out.println("How to use: AnyPercent[path to .any file]");
                System.exit(64);
            }
        } catch (IOException exception) {
            throw new IOException(exception.toString());
        }
        printErrors();
    }
    public static void syntaxError(String message, int lineNumber) {
        syntaxErrorMessages.add("Syntax error (line "+ lineNumber + "): "+message); 
        
    }
    public static void syntaxError(String message, Lexeme lexeme) {
        syntaxErrorMessages.add("Syntax error at "+ lexeme + " : "+message); 
    }
    public static void runtimeError(String message, Lexeme lexeme) {
        syntaxErrorMessages.add("Runtime error at "+ lexeme + ". "+message); 
        printErrors();
        System.exit(65);
    }
    public static void runtimeError(String message, int lineNumber) {
        syntaxErrorMessages.add("Runtime error at line "+ lineNumber + ". "+message); 
        printErrors();
        System.exit(65);
    }
    private static String getSourceCodeFromFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }
    private static void runFile(String path) throws IOException {
        System.out.println("Running " + path + "...");
        String source = getSourceCodeFromFile(path).replaceAll("//.*//", "");

        // Lexing
        Lexer lexer = new Lexer(source);
        ArrayList<Lexeme> lexemes = lexer.lex();
        System.out.println(lexemes);

        //Parsing
        Parser parser = new Parser(lexemes);
        Lexeme programParseTree = parser.program();
        programParseTree.printAsParseTree();
        printErrors();

        Environment globalEnvironment = new Environment();
        Evaluator evaluator = new Evaluator();
        Lexeme programResult = evaluator.eval(programParseTree, globalEnvironment);
        // System.out.println("Program Result: \n" + programResult);
        printErrors();
    }
    private static void printErrors() {
        //colors
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RED_BACKGROUND = "\u001B[41m";
        final String ANSI_RESET = "\u001B[0m";
//
        for (String syntaxErrorMessage : syntaxErrorMessages) System.out.println(ANSI_YELLOW + syntaxErrorMessage + ANSI_RESET);
        for (String runtimeErrorMessage : runtimeErrorMessages) System.out.println(ANSI_RED_BACKGROUND + runtimeErrorMessage + ANSI_RESET);
    }
}