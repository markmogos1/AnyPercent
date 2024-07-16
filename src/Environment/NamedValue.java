package Environment;

import static LexicalAnalysis.Type.*;

import java.util.HashMap;

import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Type;


public class NamedValue {
    //Instance Variables
    private Lexeme name;
    private Lexeme value;

    private Type type;
    private boolean isConstant;
    private boolean isOpen;



    //Constructor
    public NamedValue(Type type, Lexeme name) {
        this.type = type;
        this.name = name;
        this.value = new Lexeme(NULL);
    }
    //Getters and setters
  
    public Lexeme getIdentifier() {
        return name;
    }
    public Lexeme getValue() {
        return value;
    }
    public Type getType() {
        return type;
    }
    public boolean getOpenStatus() {
        return isOpen;
    }
    public boolean getConstantStatus() {
        return isConstant;
    }
    public void setValue(Lexeme value) {
        this.value = value;
    }



    //TOSTRING
    public String toString() {
        return name.getStringVal() + ": " + value.toValueOnlyString() + "( " + type + ")";
    }

}
