package Environment;
import static LexicalAnalysis.Type.*;

import Environment.*;
import LexicalAnalysis.Lexeme;


public class EnvironmentTest {
    public static void main(String[] args) {
        Environment globalEnv = new Environment();
        Environment env1 = new Environment(globalEnv);
    
        Lexeme x = new Lexeme(IDENTIFIER, 1, "x");
        Lexeme xTwo = new Lexeme(IDENTIFIER, 20, "x");
    
        Lexeme a = new Lexeme(IDENTIFIER, 1, "a");
        Lexeme b = new Lexeme(IDENTIFIER, 1, "b");
        Lexeme numberTest = new Lexeme(INTEGER, 1, 2);
        Lexeme numberTest2 = new Lexeme(INTEGER, 1, 22);


        globalEnv.add(INTEGER, x, numberTest);
        globalEnv.add(INTEGER, a, numberTest);
        env1.add(INTEGER, b, numberTest);
        env1.add(INTEGER, xTwo, numberTest2);


        System.out.println(env1);

        System.out.println("-----Splitting Test File Here----");
        System.out.println(env1.lookup(x));
        System.out.println(globalEnv.lookup(x));
        System.out.println(globalEnv.lookup(b));
        System.out.println(env1.lookup(b));
        System.out.println(globalEnv.lookup(xTwo));

    }
 



}
