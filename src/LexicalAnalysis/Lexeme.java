package LexicalAnalysis;
import java.util.ArrayList;

import Environment.Environment;
public class Lexeme {

//instance variables
private final Type type;

private Integer lineNumber;

private Integer intVal;
private Double realVal;
private Boolean booleanVal;
private String stringVal;
private ArrayList<Lexeme> arrayVal = new ArrayList<>();
private ArrayList<Lexeme> children = new ArrayList<>();
private Environment definingEnvironment;


//Constructors

public Lexeme(Type type) {this.type=type;}

public Lexeme(Type type, Integer lineNumber) {
    this.type=type;
    this.lineNumber=lineNumber;
}

public Lexeme(Type type, Integer lineNumber, Integer intVal) {
    this.type=type;
    this.lineNumber=lineNumber;
    this.intVal=intVal;
}

public Lexeme(Type type, Integer lineNumber, Double realVal) {
    this.type=type;
    this.lineNumber=lineNumber;
    this.realVal=realVal;
}

public Lexeme(Type type, Integer lineNumber, Boolean booleanVal) {
    this.type=type;
    this.lineNumber=lineNumber;
    this.booleanVal=booleanVal;
}

public Lexeme(Type type, Integer lineNumber, String stringVal) {
    this.type=type;
    this.lineNumber=lineNumber;
    this.stringVal=stringVal;
}

public Lexeme(Type type, Integer lineNumber, ArrayList<Lexeme> arrayVal) {
    this.type=type;
    this.lineNumber=lineNumber;
    this.arrayVal=arrayVal;
}


//Getters:
public Integer getIntVal() {return intVal;}
public Double getRealVal() {return realVal;}
public Boolean getBooleanVal() {return booleanVal;}
public String getStringVal() {return stringVal;}
public ArrayList<Lexeme> getArrayVal() {return arrayVal;}
public Integer getLineNumber() {return lineNumber;}
public Type getType() {return type;}
public ArrayList<Lexeme> getChildren() {
    return children;
}
public Environment getDefiningEnvironment(Environment environment) {
    return definingEnvironment;
}


//Setters:
public void setIntVal(Integer intVal) {this.intVal = intVal;}
public void setRealVal(double realVal) {this.realVal = realVal;}
public void setBooleanVal(Boolean booleanVal) {this.booleanVal = booleanVal;}
public void setStringVal(String stringVal) {this.stringVal = stringVal;}
public void setArrayVal(ArrayList<Lexeme> arrayVal) {this.arrayVal = arrayVal;}
public void setLineNumber(Integer lineNumber) {this.lineNumber = lineNumber;}
public void setDefiningEnvironment(Environment environment) {
    definingEnvironment = environment;
}

public void addChild(Lexeme lexeme) {
    this.children.add(lexeme);
}
public void addChildren(ArrayList<Lexeme> lexemes) {
    for (int i = 0; i < lexemes.size(); i++) {
        addChild(lexemes.get(i));
    }
    //children.addAll(lexemes);
}

public void printAsParseTree() {
    System.out.println(getPrintableTree(this, 0));
}

public String getPrintableTree(Lexeme root, int level) {
    if (root == null) return "(Empty Parsetree)";
    StringBuilder treeString = new StringBuilder(root.toString());

    StringBuilder spacer = new StringBuilder("\n");
    spacer.append("\t".repeat(level));
    System.out.println(level);

    int numChildren = root.children.size();
    if (numChildren > 0) {
        treeString.append(" (with ").append(numChildren).append(numChildren == 1 ? " child):" : " children):");
        for (int i = 0; i < numChildren; i++) {
            Lexeme child = root.children.get(i);
            treeString
            .append(spacer).append("(").append(i+1).append(") ")
            .append(getPrintableTree(child, level+1));
        }
    }
    return treeString.toString();
}

//ToString:

public String toValueOnlyString() {
    String val = "EMPTY";
    if (intVal!=null) {
        val = intVal.toString();
    }
    if (realVal!=null) {
        val = realVal.toString();
    }
    if (booleanVal!=null) {
        val = booleanVal.toString();
    }
    if (stringVal!=null) {
        val = stringVal;
    }
    return val;
}
public boolean equals(Lexeme other){
    if(type != other.getType()) return false;;
    if(realVal != null && realVal.equals(other.realVal)) return true;
    if(stringVal != null && stringVal.equals(other.stringVal)) return true;
    if(intVal != null && intVal.equals(other.intVal)) return true;
    return(booleanVal != null && booleanVal.equals(other.booleanVal));
}

public String toString() {
    

String str = ("(Line: " + lineNumber +  ") Type: "  + type);

if (intVal!=null) {
    str +=  ". Value: " + intVal;
}
if (realVal!=null) {
    str +=  ". Value: " + realVal;
}
if (booleanVal!=null) {
    str +=  ". Value: " + booleanVal;
}
if (stringVal!=null) {
    str +=  ". Value: " + stringVal;
}
return str;
}


}
