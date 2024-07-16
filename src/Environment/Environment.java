package Environment;

import static LexicalAnalysis.Type.*;

import java.util.ArrayList;

import AnyPercent.AnyPercent;
import Environment.NamedValue;
import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Type;



public class Environment {
    //Instance Variables
    private final Environment parent;
    private final ArrayList<NamedValue> entries;
    private Integer hash;

    //Constructors
    public Environment(Environment parent) {
        this.parent = parent;
        this.entries = new ArrayList<>();
        hash = hashCode();

    }
    //For global environment only
    public Environment() {
        this(null);
    }
    //Core Environment Functions
    public Lexeme softLookup(Lexeme identifier) {
        for (NamedValue namedValue : entries) {
            if (namedValue.getIdentifier().equals(identifier)) { 
                return namedValue.getValue();
            }
        }
        return null;
    }
    public Lexeme lookup(Lexeme identifier) {
        Lexeme value = softLookup(identifier);
        if (value == null) {
            if (parent != null) {
                return parent.lookup(identifier);
            }
            else {
                error(("'" + identifier.getStringVal() + "' is undefined."), identifier.getLineNumber());
            }
        }
        return value;
    } 

    private void error(String message, int lineNumber) {
        AnyPercent.syntaxError(message, lineNumber);
    }
    public void add(Type type, Lexeme identifier) {
        add(type, identifier, null);
    }
    public void add(Type type, Lexeme identifier, Lexeme value) {
        if (softLookup(identifier) != null) {
            error("A variable with the name '" + identifier.getStringVal() + "' is already defined and cannot be re-declared.", identifier.getLineNumber());
        } else {
            entries.add(new NamedValue(type, identifier));
            if (value != null) update(identifier, value);
        }
        
    }
    public void update(Lexeme identifier, Lexeme newValue) {
        //Ensure this identifier is defined in this or some parent environment
        lookup(identifier);

        //Search this environment and update if found locally
        for (NamedValue namedValue : entries) {
            if (namedValue.getIdentifier().equals(identifier)) {
                Type declaredType = namedValue.getType();
                Type providedType = newValue.getType();

                //if (providedType != declaredType) newValue = typeElevate(newValue, declaredType); //Do not think i am including this at the moment as I'd like my variables to be able to be assigned to whatever value
                //if (newValue == null) error("Variable '" + identifier.getStringVal() + "' has been declared as type " + declaredType + " and cannot be assigned a value of type " + providedType, identifier.getLineNumber());

                namedValue.setValue(newValue);
                
                //QUIT LOOKING when we find a match
                return;

            }
        }
   
        //If no match, go up an environemnt and try?
        parent.update(identifier, newValue);
    }
    // public Lexeme typeElevate(Lexeme newValue, Type declaredType) { //Just in clase i want to include
    //     return null;
    // }
    public int getHash() {
        return hash;
    }

    //toString
    public String toString() {
        String end = "";
        String parent;
        if(this.parent != null) parent = "\n\tParent: " + Integer.toString(this.parent.getHash());
        else parent = "\n\tNo Parent Environment (Global)";
        String values = "";
        for(int i = 0; i < entries.size(); i++){
            values += "\t" + entries.get(i).getIdentifier().getStringVal() + ": " + entries.get(i).getValue().toValueOnlyString() + ": " +entries.get(i).getType() + "\n";
        }
        if(this.parent != null){
            String nextEnv = this.parent.toString();
            end += nextEnv;
        }
        end += "-----------------\n" + "Enviornment " + hash + parent + "\n\n\tValues: \n\t" + "-----------------\n" + values;
        return end;
    }
    //Error Reporting

}
