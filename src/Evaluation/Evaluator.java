package Evaluation;

import AnyPercent.AnyPercent;
import Environment.Environment;
import LexicalAnalysis.*;

// import static AnyPercent.Evaluation.Operations.*;
import static LexicalAnalysis.Type.*;

import java.lang.Math;



public class Evaluator {

    //Evaluation!
    public Lexeme eval(Lexeme tree, Environment environment) {
        if (tree == null) return new Lexeme(null);
        switch (tree.getType()) {
            case STATEMENT_LIST: return evalStatementList(tree, environment);

            case INTEGER, STRING, DOUBLE, BOOLEAN: return tree;

            case IDENTIFIER: return environment.lookup(tree);

            case EQUALS: return evalVariableModification(tree, environment);
            case FOR, WHILE: return evalLoop(tree, environment);
            //Unary Operators Only
            case UNARYEXPRESSION: 
            if (tree.getChildren().get(1).getType()==PLUS_PLUS) return evalPlusPlus(tree, environment);
            else evalMinusMinus(tree, environment);
            case FUNCTIONDECLARATION: return evalFunctionDeclaration(tree, environment);
            case FUNCTIONCALL: return evalFunctionCall(tree, environment);
            case IF: return evalConditionalStatement(tree, environment);
            //Easy Operators
            case PLUS, MINUS, TIMES, DIVIDE, MOD, POWEROF: return evalSimpleOperator(tree, environment);

            case PRINT: return evalPrintStatement(tree, environment);

            //Comparison Operators
            case GREATER, GREATEROREQUALS, LESS, LESSOREQUALS, EQUALS_EQUALS, DOESNOTEQUAL: return evalComparisonOperator(tree, environment);


            default: return new Lexeme(NULL);
        }
    }

    private Lexeme evalStatementList(Lexeme tree, Environment environment) {
    
        Lexeme result = new Lexeme(null);
        for (Lexeme statement : tree.getChildren()) {
            result = eval(statement, environment);
        }
        return result;
    }

    private Lexeme evalVariableModification(Lexeme tree, Environment environment) {
        Lexeme value = null;
  

        value = eval(tree.getChildren().get(1), environment);

        if (environment.softLookup(tree.getChildren().get(0))==null) {
            if (tree.getChildren().size() == 2) {
                if (value.getType() == INTEGER) {
                    environment.add(INTEGER, tree.getChildren().get(0), value);
                }
                else if (value.getType() == STRING) {
                    environment.add(STRING, tree.getChildren().get(0), value);
                }
                else if (value.getType() == BOOLEAN) {
                    environment.add(BOOLEAN, tree.getChildren().get(0), value);
                }
                else if (value.getType() == DOUBLE) {
                    environment.add(DOUBLE, tree.getChildren().get(0), value);
                }
                return value;
            }
            
        }
        else {
           environment.update(tree.getChildren().get(0), value);
        }
        //TODO: ADD VARIABLE ASSIGNMENT RIGHT HERE AS AN ELSE IF
        return value;
    }
    private Lexeme evalPrintStatement(Lexeme tree, Environment environment) {
        Lexeme evaledStatement = eval(tree.getChildren().get(0), environment);
        if (evaledStatement == null) {
            System.out.println();
        }
        else if (evaledStatement.getIntVal()!=null) {
            System.out.println(evaledStatement.getIntVal());
        }
        else if (evaledStatement.getRealVal()!=null) {
            System.out.println(evaledStatement.getRealVal());
        }
        else if (evaledStatement.getBooleanVal() != null) {
            System.out.println(evaledStatement.getBooleanVal());
        }
        else if (evaledStatement.getStringVal() != null) {
            System.out.println(evaledStatement.getStringVal());
        }
        return tree.getChildren().get(0);
    }

    private Lexeme evalFunctionDeclaration(Lexeme tree, Environment environment) {
        tree.setDefiningEnvironment(environment);
        Lexeme functionName;
        if (tree.getChildren().get(0).getType() == CONSISTENCYINDICATOR) {
            functionName = tree.getChildren().get(3);
        }
        else functionName = tree.getChildren().get(2);
         

        environment.add(FUNCTIONDECLARATION, functionName, tree);

        return functionName;
    }


    private Lexeme evalConditionalStatement(Lexeme tree, Environment environment) {
        int ifCount = 0;
        while (tree.getChildren().get(ifCount).getType() == IF) {
            ifCount++;
        }
        boolean elsePresence = (tree.getChildren().get(ifCount).getType() == ELSE);
        int elseCount = 0;
        if (elsePresence) elseCount++;

        for (int i = 0; i < ifCount; i++) {
            if ((eval(tree.getChildren().get(ifCount+i+elseCount), environment)).getBooleanVal()) {
                return eval(tree.getChildren().get(2*ifCount+elseCount+i), environment);
            }
        }
        if (elsePresence) {
            return eval(tree.getChildren().get(3*ifCount+elseCount), environment);
        }
        return tree;
    }
    private Lexeme evalLoop(Lexeme tree, Environment environment) {
        Lexeme result = null;

        if (tree.getType() == FOR) {
           if (tree.getChildren().get(2).getChildren().get(1).getType() == PLUS_PLUS) {
            for (int i = tree.getChildren().get(0).getChildren().get(1).getIntVal(); i < tree.getChildren().get(1).getChildren().get(1).getIntVal(); i++) {
                Environment loopEnvironment = new Environment(environment);
                Lexeme id = new Lexeme(IDENTIFIER, tree.getLineNumber(), "i");
                loopEnvironment.add(INTEGER, id, new Lexeme(INTEGER, id.getLineNumber(), i));
                result = eval(tree.getChildren().get(3), environment);
           } 
           }
           else if (tree.getChildren().get(2).getChildren().get(1).getType() == MINUS_MINUS) {
            for (int i = tree.getChildren().get(0).getChildren().get(1).getIntVal(); i < tree.getChildren().get(1).getChildren().get(1).getIntVal(); i--) {
                Environment loopEnvironment = new Environment(environment);
                Lexeme id = new Lexeme(IDENTIFIER, tree.getLineNumber(), "i");
                loopEnvironment.add(INTEGER, id, new Lexeme(INTEGER, id.getLineNumber(), i));
                result = eval(tree.getChildren().get(3), environment);
           } 
           }

        }
        else if (tree.getType()==WHILE) {
            while (eval(tree.getChildren().get(0), environment).getBooleanVal()) {
                result = eval(tree.getChildren().get(1), environment);
            }
        }
        return result;
    }
    private Lexeme evalPlusPlus(Lexeme tree, Environment environment) {
        Lexeme evaled = eval(tree.getChildren().get(0), environment);
        if (evaled.getType() == INTEGER) return new Lexeme(INTEGER, tree.getLineNumber(), evaled.getIntVal() + 1);
        if (evaled.getType() == DOUBLE) return new Lexeme(DOUBLE, tree.getLineNumber(), evaled.getRealVal() + 1);
        return error("Cannot use modifier '++' on type '"  + tree.getChildren().get(0).getType(), tree.getChildren().get(0));//DEFAULT
    }
    private Lexeme evalMinusMinus(Lexeme tree, Environment environment) {
        if (tree.getChildren().get(0).getType() == INTEGER) return new Lexeme(INTEGER, tree.getLineNumber(), tree.getChildren().get(0).getIntVal() - 1);
        if (tree.getChildren().get(0).getType() == DOUBLE) return new Lexeme(DOUBLE, tree.getLineNumber(), tree.getChildren().get(0).getRealVal() - 1);
        return error("Cannot use modifier '--' on type '"  + tree.getChildren().get(0).getType(), tree.getChildren().get(0));//DEFAULT
    }
    
    private Lexeme evalComparisonOperator(Lexeme tree, Environment environment) {
        
        Lexeme lex1 = eval(tree.getChildren().get(0), environment);
        Lexeme lex2 = eval(tree.getChildren().get(1), environment);
        if (lex1 == null || lex2 == null) return error("Undefined Variable or Misinputted Expression", lex1);
        switch (tree.getType()) {
            case GREATER, GREATEROREQUALS, LESS, LESSOREQUALS: return evalMagnitudeOperator(lex1, lex2, tree.getType());
            case EQUALS_EQUALS, DOESNOTEQUAL: return evalEqualityOperator(lex1, lex2, tree.getType());
            default: return new Lexeme(NULL);
        }
    }
    private Lexeme evalMagnitudeOperator(Lexeme lex1, Lexeme lex2, Type operatorType) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        
        if (type1 == INTEGER) {
            if (type2 == INTEGER) {
                switch (operatorType) {
                    case GREATER: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() > lex2.getIntVal());
                    case GREATEROREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() >= lex2.getIntVal());
                    case LESS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() < lex2.getIntVal());
                    case LESSOREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() <= lex2.getIntVal());
                    default: return error("Invalid Operator", lex1);
                }
              
        }
          else if (type2 == DOUBLE) {
                    switch (operatorType) {
                        case GREATER: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() > lex2.getRealVal());
                        case GREATEROREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() >= lex2.getRealVal());
                        case LESS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() < lex2.getRealVal());
                        case LESSOREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal() <= lex2.getRealVal());
                        default: return error("Invalid Operator", lex1);
                }
            }
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) {
                switch (operatorType) {
                    case GREATER: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() > lex2.getIntVal());
                    case GREATEROREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() >= lex2.getIntVal());
                    case LESS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() < lex2.getIntVal());
                    case LESSOREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() <= lex2.getIntVal());
                    default: return error("Invalid Operator", lex1);
                }
        }
          else if (type2 == DOUBLE) {
                    switch (operatorType) {
                        case GREATER: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() > lex2.getRealVal());
                        case GREATEROREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() >= lex2.getRealVal());
                        case LESS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() < lex2.getRealVal());
                        case LESSOREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal() <= lex2.getRealVal());
                        default: return error("Invalid Operator", lex1);
                }
            }
        }
        else if (type1 == STRING) {
            
        }
        else if (type1 == BOOLEAN) {
            if (type2 == BOOLEAN) {
                int bool1 = lex1.getBooleanVal() ? 1 : 0;
                int bool2 = lex2.getBooleanVal() ? 1 : 0;
                switch (operatorType) {
                    case GREATER: return new Lexeme(BOOLEAN, lex1.getLineNumber(), bool1 > bool2);
                    case GREATEROREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), bool1 >= bool2);
                    case LESS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), bool1 < bool2);
                    case LESSOREQUALS: return new Lexeme(BOOLEAN, lex1.getLineNumber(), bool1 <= bool2);
                    default: return error("Invalid Operator", lex1);
                }
            }
        }
        return error("Cannot compare type '" + type1 + "' with type '" + type2 + "'.", lex1); //DEFAULT

    }
    

    private Lexeme evalEqualityOperator(Lexeme lex1, Lexeme lex2, Type operatorType) {
        if (operatorType == EQUALS_EQUALS) return evalEqualsEquals(lex1, lex2, operatorType);
        else return evalNotEquals(lex1, lex2, operatorType);
    }
    private Lexeme evalEqualsEquals(Lexeme lex1, Lexeme lex2, Type operatorType) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
       

        if (type1 == INTEGER) {
            if (type2 == BOOLEAN) {
                int x = lex2.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x==lex1.getIntVal());
            }
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal()==lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal()==(lex2.getRealVal().intValue()));
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal().toString().equals(lex2.getStringVal()));

        }
        else if (type1 == DOUBLE) {
            if (type2 == BOOLEAN) {
                double x = lex2.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x==lex1.getRealVal());
            }
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal().intValue()==lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal()==lex2.getRealVal());
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal().toString().equals(lex2.getStringVal()));
        }
        else if (type1 == STRING) {
            if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex2.getRealVal().toString().equals(lex1.getStringVal()));
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex2.getIntVal().toString().equals(lex1.getStringVal()));
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.equals(lex2));
        }
        else if (type1 == BOOLEAN) {
            if (type2 == BOOLEAN) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getBooleanVal()==lex2.getBooleanVal());
            
            else if (type2 == INTEGER) {
                int x = lex1.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x==lex2.getIntVal());
            }
            else if (type2 == DOUBLE) {
                double x = lex1.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x==lex2.getRealVal());         
               }
        }
        return error("Unable to perform operator 'equals' (==) with type " + type1 + " and type " + type2, lex1);

        
     }
     private Lexeme evalNotEquals(Lexeme lex1, Lexeme lex2, Type operatorType) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
       

        if (type1 == INTEGER) {
            if (type2 == BOOLEAN) {
                int x = lex2.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x!=lex1.getIntVal());
            }
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal()!=lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getIntVal()!=(lex2.getRealVal().intValue()));
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), !(lex1.getIntVal().toString().equals(lex2.getStringVal())));

        }
        else if (type1 == DOUBLE) {
            if (type2 == BOOLEAN) {
                double x = lex2.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x!=lex1.getRealVal());
            }
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal().intValue()!=lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getRealVal()!=lex2.getRealVal());
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), !(lex1.getRealVal().toString().equals(lex2.getStringVal())));
        }
        else if (type1 == STRING) {
            if (type2 == DOUBLE) return new Lexeme(BOOLEAN, lex1.getLineNumber(), !(lex2.getRealVal().toString().equals(lex1.getStringVal())));
            else if (type2 == INTEGER) return new Lexeme(BOOLEAN, lex1.getLineNumber(), !(lex2.getIntVal().toString().equals(lex1.getStringVal())));
            else if (type2 == STRING) return new Lexeme(BOOLEAN, lex1.getLineNumber(), !(lex1.equals(lex2)));
        }
        else if (type1 == BOOLEAN) {
            if (type2 == BOOLEAN) return new Lexeme(BOOLEAN, lex1.getLineNumber(), lex1.getBooleanVal()!=lex2.getBooleanVal());
            
            else if (type2 == INTEGER) {
                int x = lex1.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x!=lex2.getIntVal());
            }
            else if (type2 == DOUBLE) {
                double x = lex1.getBooleanVal() ? 1 : 0;
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), x!=lex2.getRealVal());         
               }
        }
        return error("Unable to perform operator 'not equals' (!=) with type " + type1 + " and type " + type2, lex1);

        
     }


    private Lexeme evalSimpleOperator(Lexeme tree, Environment environment) {
        Lexeme lex1 = eval(tree.getChildren().get(0), environment);
        Lexeme lex2 = eval(tree.getChildren().get(1), environment);
        if (lex1 == null || lex2 == null) return error("Undefined Variable or Misinputted Expression", lex1);

        switch (tree.getType()) {
            case PLUS: return add(lex1, lex2);
            case MINUS: return subtract(lex1, lex2);
            case TIMES: return multiply(lex1, lex2);
            case DIVIDE: return divide(lex1, lex2);
            case POWEROF: return powerOf(lex1, lex2);
            case MOD: return mod(lex1, lex2);
            default: return new Lexeme(NULL);
        }
    }
    private Lexeme add(Lexeme lex1, Lexeme lex2) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() + lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), (double)(lex1.getIntVal()) + lex2.getRealVal());
            else if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), (Integer.toString(lex1.getIntVal())) + lex2.getStringVal());
            else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() + x);
            }
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() + lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() + lex2.getRealVal());
            else if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), Double.toString(lex1.getRealVal())+lex2.getStringVal()); //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
            else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() + x);
        }
    }
        else if (type1 == STRING) {
            if (type2 == STRING) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal() + lex2.getStringVal());
            else if (type2 == BOOLEAN) {
                String temp = (lex2.getBooleanVal() ? "true" : "false");
                return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal() + temp);
             }
             else if (type2 == DOUBLE) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal()+Double.toString(lex2.getRealVal())); 
             else if (type2 == INTEGER) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal()+Integer.toString(lex2.getIntVal())); //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
        }
        else if (type1 == BOOLEAN) {
            if (type2 == INTEGER) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(), lex2.getIntVal() + x);
            }
            else if (type2 == DOUBLE) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), lex2.getRealVal() + x);
            }
            else if (type2 == STRING) {
                String temp = (lex1.getBooleanVal() ? "true" : "false");
                return new Lexeme(STRING, lex1.getLineNumber(),  temp+lex2.getStringVal());
            }
            else if (type2 == BOOLEAN) {
                boolean temp = lex1.getBooleanVal() || lex2.getBooleanVal();
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), temp); 
            }
        }
        return error("Cannot add type '"  +type1 +"' with type '" + type2 + "'.", lex1);//DEFAULT
    }
    
    private Lexeme subtract(Lexeme lex1, Lexeme lex2) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() - lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), (double)(lex1.getIntVal()) - lex2.getRealVal());
            else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() - x);
            }
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() - lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() - lex2.getRealVal());
            else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() - x);
        }
    }
        else if (type1 == STRING) {
            if (type2 == STRING) {
                String x = lex1.getStringVal().replace(lex2.getStringVal(), "");
                return new Lexeme(STRING, lex1.getLineNumber(), x); 
             }
            //  else if (type2 == DOUBLE) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal()+Double.toString(lex2.getRealVal())); 
            //  else if (type2 == INTEGER) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal()+Double.toString(lex2.getRealVal())); //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
        }
        else if (type1 == BOOLEAN) {
            if (type2 == INTEGER) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(),  x-lex2.getIntVal());
            }
            else if (type2 == DOUBLE) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), x-lex2.getRealVal());
            }

        }
        return error("Cannot subtract type '"  +type1 +"' with type '" + type2 + "'.", lex1);//DEFAULT
    }
    
    private Lexeme multiply(Lexeme lex1, Lexeme lex2) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() * lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), (double)(lex1.getIntVal()) * lex2.getRealVal());
            else if (type2 == STRING) {
                String temp = "";
                for (int i = 0; i < lex1.getIntVal(); i++) {
                    temp += lex2.getStringVal();
                }
                return new Lexeme(STRING, lex1.getLineNumber(), temp);
            }
            else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() * x);
            }
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() * lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() * lex2.getRealVal());
            else if (type2 == STRING) {
                String temp = "";
                for (int i = 0; i < (lex1.getRealVal().intValue()); i++) {
                    temp += lex2.getStringVal();
                }
                return new Lexeme(STRING, lex1.getLineNumber(), temp);
            }      
             else if (type2 == BOOLEAN) {
                int x=0;
                if (lex2.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() * x);
        }
    }
        else if (type1 == STRING) {
            if (type2 == BOOLEAN) {
                if (lex2.getBooleanVal()) return new Lexeme(STRING, lex1.getLineNumber(), lex1.getStringVal());
                else return new Lexeme(STRING, lex1.getLineNumber(), "");
             }
             else if (type2 == DOUBLE) {
                String temp = "";
                for (int i = 0; i < (lex2.getRealVal().intValue()); i++) {
                    temp += lex1.getStringVal();
                }
                return new Lexeme(STRING, lex1.getLineNumber(), temp);
                }
             else if (type2 == INTEGER) {
                String temp = "";
                for (int i = 0; i < lex2.getIntVal(); i++) {
                    temp += lex1.getStringVal();
                }
                return new Lexeme(STRING, lex1.getLineNumber(), temp);            } //This one is backwards cause i wrote it and was lazy just know that it is backwards if things arent working!
        }
        else if (type1 == BOOLEAN) {
            if (type2 == INTEGER) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(INTEGER, lex1.getLineNumber(), lex2.getIntVal() * x);
            }
            else if (type2 == DOUBLE) {
                int x=0;
                if (lex1.getBooleanVal()) x=1;
                return new Lexeme(DOUBLE, lex1.getLineNumber(), lex2.getRealVal() * x);
            }
            else if (type2 == STRING) {
                if (lex1.getBooleanVal()) return new Lexeme(STRING, lex1.getLineNumber(), lex2.getStringVal());
                else return new Lexeme(STRING, lex1.getLineNumber(), "");
            }
            else if (type2 == BOOLEAN) {
                boolean temp = lex1.getBooleanVal() && lex2.getBooleanVal();
                return new Lexeme(BOOLEAN, lex1.getLineNumber(), temp); 
            }
        }
        return error("Cannot multiply type '"  +type1 +"' with type '" + type2 + "'.", lex1);//DEFAULT
    }
    
    private Lexeme divide(Lexeme lex1, Lexeme lex2) {
        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() / lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), (double)(lex1.getIntVal()) / lex2.getRealVal());
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() / lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() / lex2.getRealVal());
    }
      
       
        return error("Cannot divide type '"  +type1 +"' with type '" + type2 + "'.", lex1);//DEFAULT
    }

    private Lexeme powerOf(Lexeme lex1, Lexeme lex2) {

        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), (int)(Math.pow(lex1.getIntVal(), lex2.getIntVal())));
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), Math.pow((double)(lex1.getIntVal()), lex2.getRealVal()));
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), Math.pow(lex1.getRealVal(), lex2.getIntVal()));
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), Math.pow(lex1.getRealVal(), lex2.getRealVal()));
    }
      
       
        return error("Cannot raise type '"  +type1 +"' to type '" + type2 + "'.", lex1);//DEFAULT
    }

    private Lexeme mod(Lexeme lex1, Lexeme lex2) {

        Type type1 = lex1.getType();
        Type type2 = lex2.getType();
        //INTS
        if (type1 == INTEGER) {
            //INTS
            if (type2 == INTEGER) return new Lexeme(INTEGER, lex1.getLineNumber(), lex1.getIntVal() % lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getIntVal() % lex2.getRealVal());
        }
        else if (type1 == DOUBLE) {
            if (type2 == INTEGER) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() % lex2.getIntVal());
            else if (type2 == DOUBLE) return new Lexeme(DOUBLE, lex1.getLineNumber(), lex1.getRealVal() % lex2.getRealVal());
    }
      
       
        return error("Cannot perform modulus operation on type '"  +type1 +"' with type '" + type2 + "'.", lex1);//DEFAULT
    }
    
    
    private Lexeme evalFunctionCall(Lexeme tree, Environment environment) { //gotta figure this out! doesnt throw an error soooo
        Lexeme functionName = tree.getChildren().get(0); 
        Lexeme functionDefinitionTree = environment.lookup(functionName);
        if (functionDefinitionTree.getType() != FUNCTIONDECLARATION) {
            return error("Not a function", functionName);
        }
        int consistent = 0;
        
        if (functionDefinitionTree.getChildren().get(0).getType() == CONSISTENCYINDICATOR) {
             consistent++;
        }

        Lexeme parameterList = functionDefinitionTree.getChildren().get(3+consistent);

        Lexeme argumentList = tree.getChildren().get(1);
        Lexeme evaluatedArgList = evalArgumentList(argumentList, environment);
        //Verify arguments here!
        Environment callEnvironment = new Environment(environment);
        //For (every param) : callEnvironment.add(parameterList.get(i).get(1), evaluatedArgList.get(i))
        for (int i = 0; i < parameterList.getChildren().size(); i++) {
            callEnvironment.add(FUNCTIONCALL, parameterList.getChildren().get(i).getChildren().get(1), evaluatedArgList.getChildren().get(i));
        }
        Lexeme functionBody = functionDefinitionTree.getChildren().get(4+consistent);
        
        return eval(functionBody, callEnvironment);
    }
    private Lexeme evalArgumentList(Lexeme tree, Environment environment) {
        Lexeme argumentList = new Lexeme(ARGUMENTLIST);
        Lexeme temp;
        for (int i = 0; i < tree.getChildren().size(); i++) {
            temp = eval(tree.getChildren().get(i), environment);
            argumentList.addChild(temp);
        }
        return argumentList;

    }

    //Error Reporting
    private Lexeme error(String message, Lexeme lexeme) {
        AnyPercent.runtimeError(message, lexeme);
        return new Lexeme(ERROR, lexeme.getLineNumber(), message);
        
    }
    private Lexeme error(String message, int lineNumber) {
        AnyPercent.runtimeError(message, lineNumber);
        return new Lexeme(ERROR, lineNumber, message);
    }

}
