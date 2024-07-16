package Evaluation;

import Environment.Environment;
import LexicalAnalysis.*;
import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Type;
import static LexicalAnalysis.Type.*;
import AnyPercent.AnyPercent;


public enum Operations {;

    public static Lexeme add(Lexeme lexeme1, Lexeme lexeme2) {
        System.out.println("Starting Add Now");
        Lexeme lex1 = lexeme1;
        Lexeme lex2 = lexeme2;
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
  

        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() + lex2.getIntVal());
            if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), (double)(lex1.getIntVal()) + lex2.getRealVal());
            if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), (Integer.toString(lex1.getIntVal())) + lex2.getStringVal());
            if (type2 == BOOLEAN) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() + (lex2.getBooleanVal() ? 1 : 0) );
        }
        if (type1 == DOUBLE) {
            if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() + lex2.getRealVal());
            if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), lex2.getRealVal() + Double.toString(lex1.getRealVal())); //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
            if (type2 == BOOLEAN) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() + (lex2.getBooleanVal() ? 1 : 0) );
        }
        if (type1 == STRING) {
            if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal() + lex2.getStringVal());
            if (type2 == BOOLEAN) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal() + (lex2.getBooleanVal() ? "true" : "false"));
        }
        if (type1 == BOOLEAN) {
            if (type2 == BOOLEAN) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getBooleanVal() || lex2.getBooleanVal());
        }
   
       //Round 2!
       if (type2 == INTEGER) {
        //INTS
        if (type1 == INTEGER) return new Lexeme(INTEGER, lex2.getLineNumber(), lex2.getIntVal() + lex1.getIntVal());
        if (type1 == DOUBLE) return new Lexeme(DOUBLE, lex2.getLineNumber(), (double)(lex2.getIntVal()) + lex1.getRealVal());
        if (type1 == STRING) return new Lexeme(STRING, lex2.getLineNumber(), (Integer.toString(lex2.getIntVal())) + lex1.getStringVal());
        if (type1 == BOOLEAN) return new Lexeme(INTEGER, lex2.getLineNumber(), lex2.getIntVal() + (lex1.getBooleanVal() ? 2 : 0) );
    }
    if (type2 == DOUBLE) {
        if (type1 == DOUBLE) return new Lexeme(DOUBLE, lex2.getLineNumber(), lex2.getRealVal() + lex1.getRealVal());
        if (type1 == STRING) return new Lexeme(STRING, lex2.getLineNumber(), lex1.getRealVal() + Double.toString(lex2.getRealVal())); //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
        if (type1 == BOOLEAN) return new Lexeme(DOUBLE, lex2.getLineNumber(), lex2.getRealVal() + (lex1.getBooleanVal() ? 2 : 0) );
    }
    if (type2 == STRING) {
        if (type1 == STRING) return new Lexeme(STRING, lex2.getLineNumber(), lex2.getStringVal() + lex1.getStringVal());
        if (type1 == BOOLEAN) return new Lexeme(STRING, lex2.getLineNumber(), lex2.getStringVal() + (lex1.getBooleanVal() ? "true" : "false"));
    }
    if (type2 == BOOLEAN) {
        if (type1 == BOOLEAN) return new Lexeme(BOOLEAN, lex2.getLineNumber(), lex2.getBooleanVal() || lex1.getBooleanVal());
    }




        

        //Go backwards with reversing type1 and type2 & lex1 and lex2? seems like the easist way...


        return new Lexeme(NULL); //DEFAULT
    }
}
