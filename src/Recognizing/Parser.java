package Recognizing;
import java.util.ArrayList;

import AnyPercent.AnyPercent;

import static LexicalAnalysis.Type.*;
import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Type;



public class Parser {
    //Instances
    private static final boolean printDebugMessages = true;

    private ArrayList<Lexeme> lexemes;
    private Lexeme currentLexeme;
    private int nextLexemeIndex;
    //Core support
    private Type peek() {
        return currentLexeme.getType();
    }
    private Type peekNext() {
        if (nextLexemeIndex >= lexemes.size()) return null;
        return lexemes.get(nextLexemeIndex).getType();
    }
    private boolean check(Type type) {
        return currentLexeme.getType() == type;
    }
    private boolean checkNext(Type type) {
        // System.out.println("nextLexemeIndex is " +nextLexemeIndex);
        // System.out.println("lexemes.size is " +lexemes.size());
        // System.out.println("currentLexeme is " +currentLexeme.toString());
        if (nextLexemeIndex >= lexemes.size()) return false;
        return lexemes.get(nextLexemeIndex).getType() == type;
    }
    private Lexeme consume(Type expected) {
        if (check(expected)) return advance();

        error("Expected " + expected + " but found " + currentLexeme + ".");
        return new Lexeme(ERROR, currentLexeme.getLineNumber());
    }
    private Lexeme advance() {
        Lexeme toReturn = currentLexeme;
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
        return toReturn;
    }

    //Constructor
    public Parser(ArrayList<Lexeme> lexemes) {
        this.lexemes=lexemes;
        this.nextLexemeIndex=0;
        advance();
    }
    //Consumption functions
    public Lexeme program() {
        logHeading("Program");
        if ((statementListPending())) return statementList();
        return null;
    }
    private Lexeme statementList() {
        log("statementList");
        Lexeme statementList = new Lexeme(STATEMENT_LIST);
        while (statementPending()) statementList.addChild(statement());
        return statementList;
    }
    private Lexeme expression() {
        logHeading("Expression");
        if (unaryExpressionPending()) return unaryExpression();
        else if (binaryExpressionPending()) return binaryExpression();
        else if (primaryPending()) return primary();
        else error("Expression Expected");
        return new Lexeme(ERROR);
    }

    private Lexeme binaryExpression() {
        log("Binary Expression");
        ArrayList<Lexeme> children;
        children = new ArrayList<>();
        children.add(primary());
        Lexeme binaryExpression = binaryOperator();
        children.add(expression());
        binaryExpression.addChildren(children);
        return binaryExpression;
    }


    private Lexeme unaryExpression() {
        log("Unary Expression");

        Lexeme unaryExpr = new Lexeme(UNARYEXPRESSION);
        if (check(NOT)) { unaryExpr.addChild(consume(NOT));
        unaryExpr.addChild(expression());
        } 
        else if (postUnaryOperatorPendingNext()) {
            unaryExpr.addChild(primary());
            unaryExpr.addChild(postUnaryOperator());
        }
        return unaryExpr;
    }

    private Lexeme conditionalStatement() {
        logHeading("Conditional Statement");
        Lexeme ifStatement = new Lexeme(IF);
        int ifCount = 0;
        while (check(IF)) {
            ifCount++;
            ifStatement.addChild(consume(IF));
        }
        boolean elsePresence = check(ELSE);
        if (elsePresence) {
            ifStatement.addChild(consume(ELSE));
        }
        for (int i = 0; i<ifCount; i++) {
            ifStatement.addChild(expression());
        }
        for (int i = 0; i < ifCount + (elsePresence ? 1 : 0); i++) {
            ifStatement.addChild(body());
        }
        return ifStatement;
        
    }

    private Lexeme primary() {
        if (check(INTEGER)) return consume(INTEGER);
        else if (check(DOUBLE)) return consume(DOUBLE);
        else if (check(STRING)) return consume(STRING);
        else if (check(BOOLEAN)) return consume(BOOLEAN);
        else if (check(CHAR)) return consume(CHAR);
        else if (functionCallPending()) return functionCall();
        else if (paranthesizedExpressionPending()) return paranthesizedExpression();
        else if (check(IDENTIFIER)) return consume(IDENTIFIER);
        else {error("Primary Expected");
        return new Lexeme(ERROR);
    }
    }
    private Lexeme paranthesizedExpression() {

        consume(OBRACKET);
        Lexeme expr = expression();
        consume(CBRACKET);
        return expr;
    }
    private Lexeme functionCall() {
        Lexeme funcCall = new Lexeme(FUNCTIONCALL);
        funcCall.addChild(consume(IDENTIFIER));
        consume(OBRACKET);
        Lexeme argList = new Lexeme(ARGUMENTLIST);
        while (expressionPending()) {
            argList.addChild(expression());
        }
        funcCall.addChild(argList);
        consume(CBRACKET);
        return funcCall;
    }
    private Lexeme functionCallParamList() {
        Lexeme paramList = new Lexeme(PARAMETER_LIST);
        while (functionCallParamPending()) {
            paramList.addChild(functionCallParam());
        }
        return paramList;
    }
    private Lexeme functionCallParam() {
        Lexeme param = new Lexeme(PARAMETER_STATEMENT);
        param.addChild(varType());
        param.addChild(expression());
        return param;
    }
    
    private Lexeme varDeclaration() {
        Lexeme ident = consume(IDENTIFIER);
        Lexeme varDec = consume(EQUALS);
        Lexeme expr = expression();
        varDec.addChild(ident);
        varDec.addChild(expr);
        return varDec;

    }
    private Lexeme varAssignment() {
        Lexeme ident = consume(IDENTIFIER);
        Lexeme varDec = consume(EQUALS);
        Lexeme expr = expression();
        varDec.addChild(ident);
        varDec.addChild(expr);
        return varDec;
    }
    private Lexeme statement() {
        if (varDeclarationPending()) {
            return varDeclaration();
        }
        else if (varAssignmentPending()) {
            return varAssignment();
        }
        else if (functionDeclarationPending()) {
            return functionDeclaration();
        }
        else if (loopDeclarationPending()) {
            return loopDeclaration();
        }
        else if (conditionalStatementPending()) {
            return conditionalStatement();
        }
        else if (preMadeFunctionsPending()) {
            return preMadeFunctions();
        }
        else if (expressionPending()) {
            return expression();
        }
        else {error("Statement Expected");
            return new Lexeme(ERROR);
    }
            }
        private Lexeme loopDeclaration() {
            logHeading("Loop Declaration");

            if (forLoopPending()) {
                return forLoop();
            }
            if (whileLoopPending()) {
                return whileLoop();
            }
            return new Lexeme(ERROR);
        }
        private Lexeme preMadeFunctions() {
            logHeading("Print Statement");
            if (printPending()) return print();
            return new Lexeme(ERROR);
        }
        private Lexeme print() {
            Lexeme print = consume(PRINT);
            print.addChild(expression());
            consume(CBRACKET);
            return print;
        }
        private Lexeme whileLoop() {
            Lexeme whileTrueLoop;
            consume(LOOPINITIALIZATION);
            if (check(WHILETRUE)) {
                whileTrueLoop = consume(WHILETRUE); 
                whileTrueLoop.addChild(body());
            }

            else {
                whileTrueLoop = consume(WHILE);
                whileTrueLoop.addChild(expression());
                whileTrueLoop.addChild(body());
             }
             return whileTrueLoop;

        
        }
        private Lexeme comparisonOperator() {
            if (check(GREATER)) return consume(GREATER);
            else if (check(LESS)) return consume(LESS);
            else if (check(GREATEROREQUALS)) return consume(GREATEROREQUALS);
            else if (check(LESSOREQUALS)) return consume(LESSOREQUALS);
            else if (check(EQUALS)) return consume(EQUALS);
            else if (check(DOESNOTEQUAL)) return consume(DOESNOTEQUAL);
            else if (check(EQUALS_EQUALS)) return consume(EQUALS_EQUALS);
            else error("Comparison Operator Expected");
            return new Lexeme(ERROR);
        }
        private Lexeme binaryOperator() {
            if (check(PLUS)) return consume(PLUS);
            else if (check(PLUS_EQUALS)) return consume(PLUS_EQUALS);
            else if (check(MINUS)) return consume(MINUS);
            else if (check(MINUS_EQUALS)) return consume(MINUS_EQUALS);
            else if (check(TIMES)) return consume(TIMES);
            else if (check(TIMES_EQUALS)) return consume(TIMES_EQUALS);
            else if (check(DIVIDE)) return consume(DIVIDE);
            else if (check(DIVIDE_EQUALS)) return consume(DIVIDE_EQUALS);
            else if (check(MOD)) return consume(MOD);
            else if (check(MOD_EQUALS)) return consume(MOD_EQUALS);
            else if (check(POWEROF)) return consume(POWEROF);
            else if (comparisonOperatorPending()) return comparisonOperator();
            else error("Binary Operator Expected");
            return new Lexeme(ERROR);

            
        }
        private Lexeme forLoop() { //EX: L f x=1 x<4 x+2 [p "x is increasing by 1 each time"]]
            Lexeme forLoop = new Lexeme(FOR);
            logHeading("For Loop");
            consume(LOOPINITIALIZATION);
            forLoop = consume(FOR);
            log("For Initialization Complete");
             //L f 
            forLoop.addChild(varAssignment());
            log("For Variable Declaration Complete");

            //x=1
            forLoop.addChild(expression());
            //x<4
            log("For Condition Set");

            forLoop.addChild(unaryExpression());


            log("For Modifier Set");

            //x+2
            forLoop.addChild(body());
            log("For Body Complete");
            return forLoop;

            //print statement
        }

        private Lexeme postUnaryOperator() {
            if (check(PLUS_PLUS)) return consume(PLUS_PLUS);
            else if (check(MINUS_MINUS)) return consume(MINUS_MINUS);
            else if (check(TIMES_TIMES)) return consume(TIMES_TIMES);
            else if (check(POWEROF_POWEROF)) return consume(POWEROF_POWEROF);
            else if (check(FACTORIAL)) return consume(FACTORIAL);
            else error("Post Unary Operator Expected");
            return new Lexeme(ERROR);
        }

        private Lexeme functionDeclaration() {
            Lexeme func = consume(FUNCTIONDECLARATION);
        
            if (check(CONSISTENCYINDICATOR)) func.addChild(consume(CONSISTENCYINDICATOR));
            func.addChild(varType());
            func.addChild(openStatus());
            func.addChild(consume(IDENTIFIER));
            consume(OBRACKET);
            func.addChild(parameterList());
            consume(CBRACKET);
            func.addChild(body());
            return func;

        }
        private Lexeme varType() {
            if (check(INTEGER_INDICATOR)) {
                return consume(INTEGER_INDICATOR);
            } 
            else if (check(DOUBLE_INDICATOR)) {
                return consume(DOUBLE_INDICATOR);
            }
            else if (check(BOOLEAN_INDICATOR)) {
                return consume(BOOLEAN_INDICATOR);
            }
            else if (check(STRING_INDICATOR)) {
                return consume(STRING_INDICATOR);
            }
            else if (check(CHAR_INDICATOR)) {
                return consume(CHAR_INDICATOR);
            }
            return new Lexeme(ERROR);
        }
        private Lexeme openStatus() {
            if (check(OPENFUNCTION)) return consume(OPENFUNCTION);
            else if (check(CLOSEDFUNCTION)) return consume(CLOSEDFUNCTION);
            else error("Open Status Expected"); 
            return new Lexeme(ERROR);

        }
        private Lexeme parameterList() {
            Lexeme paramList = new Lexeme(PARAMETER_LIST);
            while (parameterStatementPending()) paramList.addChild(parameterStatement());
            return paramList;
        }
        private Lexeme parameterStatement() {
            Lexeme paramStatement = new Lexeme(PARAMETER_STATEMENT);
            paramStatement.addChild(varType());
            paramStatement.addChild(consume(IDENTIFIER));
            return paramStatement;
        }
        private Lexeme body() {
            consume(OBRACKET);
            Lexeme x = statementList();
            consume(CBRACKET);
            return x;
        }
    //Pending functions

    private boolean paranthesizedExpressionPending() {
        return check(OBRACKET);
    }

    private boolean printPending() {
        return check(PRINT);
    }

    private boolean statementListPending() {
        System.out.println("StatementPending value: " + statementPending());
        System.out.println("Peeking next: " + peek() + peekNext());

        return statementPending();
    }
    private boolean functionCallParamPending() {
        return varTypePending();
    }
    private boolean statementPending() {
        return varAssignmentPending() ||
        functionDeclarationPending() ||
        loopDeclarationPending() ||
        conditionalStatementPending() ||
        preMadeFunctionsPending() ||
        expressionPending();
    }
private boolean varDeclarationPending() {
    return (check(IDENTIFIER) && peekNext() == EQUALS);
}
private boolean forLoopPending() {
    return peekNext() == FOR;
}
private boolean whileLoopPending() {
    return peekNext() == WHILE || peekNext()== WHILETRUE;
}
private boolean parameterStatementPending() {
    if (varTypePending()) return true;
    return false;
}
private boolean varTypePending() {
    return 
    check(INTEGER_INDICATOR) ||
    check(DOUBLE_INDICATOR) ||
    check(BOOLEAN_INDICATOR) ||
    check(STRING_INDICATOR) ||
    check(CHAR_INDICATOR);
}
private boolean varAssignmentPending() {
    return (check(IDENTIFIER) && peekNext() == EQUALS);
}
private boolean functionDeclarationPending() {
    return check(FUNCTIONDECLARATION);
}
private boolean comparisonOperatorPending() {
    return check(GREATER) ||
    check(LESS) ||
    check(GREATEROREQUALS) ||
    check(LESSOREQUALS) ||
    check(EQUALS) ||
    check(DOESNOTEQUAL) ||
    check(EQUALS_EQUALS);
}
private boolean comparisonOperatorPendingNext() {
    return checkNext(GREATER) ||
    checkNext(LESS) ||
    checkNext(GREATEROREQUALS) ||
    checkNext(LESSOREQUALS) ||
    checkNext(EQUALS) ||
    checkNext(DOESNOTEQUAL) ||
    checkNext(EQUALS_EQUALS);
}
private boolean loopDeclarationPending() {
    return check(LOOPINITIALIZATION);
}
private boolean conditionalStatementPending() {
    return (check(IF));

}
private boolean preMadeFunctionsPending() {
    return printPending(); 
}
private boolean binaryOperatorPending() {
    return 
        check(PLUS) ||
        check(PLUS_EQUALS) ||
        check(MINUS) ||
        check(MINUS_EQUALS) ||
        check(TIMES) ||
        check(TIMES_EQUALS) ||
        check(DIVIDE) ||
        check(DIVIDE_EQUALS) ||
        check(MOD) ||
        check(MOD_EQUALS) ||
        check(POWEROF)
        || comparisonOperatorPending();
}
private boolean postUnaryOperatorPendingNext() {
    return checkNext(PLUS_PLUS) || 
    checkNext(MINUS_MINUS) || 
    checkNext(TIMES_TIMES) ||
    checkNext(POWEROF_POWEROF) || 
    checkNext(FACTORIAL);
}
private boolean expressionPending() {
    return binaryExpressionPending() || unaryExpressionPending() || primaryPending();
}
private boolean binaryExpressionPending() {
    if (!primaryPending()) return false;


    if (!(checkNext(PLUS) ||
checkNext(PLUS_EQUALS) ||
checkNext(MINUS) ||
checkNext(MINUS_EQUALS) ||
checkNext(TIMES) ||
checkNext(TIMES_EQUALS) ||
checkNext(DIVIDE) ||
checkNext(DIVIDE_EQUALS) ||
checkNext(MOD) ||
checkNext(MOD_EQUALS) ||
    checkNext(POWEROF) || comparisonOperatorPendingNext())) return false;

    return true;
}
private boolean unaryExpressionPending() {

    return primaryPending() && (checkNext(PLUS_PLUS) || 
    checkNext(MINUS_MINUS) || 
    checkNext(TIMES_TIMES) ||
    checkNext(POWEROF_POWEROF) || 
    checkNext(FACTORIAL));
}
private boolean primaryPending() {
    return check(STRING) || 
    numberPending() ||
    check(IDENTIFIER) ||
    booleanLiteralPending() ||
    functionCallPending() ||
    parenthesizedExpressionPending() || check(IDENTIFIER);
    
}
private boolean numberPending() {
    return check(INTEGER) || check(DOUBLE);
}
private boolean functionCallPending() {
    return check(IDENTIFIER) && checkNext(OBRACKET);
}
private boolean parenthesizedExpressionPending() {
    return check(OBRACKET);
}
private boolean booleanLiteralPending() {
    return check(BOOLEAN);
}
    //Grouped type enumeration
    //Error Reporting
    private Lexeme error(String message) {
        AnyPercent.syntaxError(message, currentLexeme);
        return new Lexeme(ERROR, currentLexeme.getLineNumber(), message);
    }
    //Debugging
    private static void log(String message) {
        if (printDebugMessages) System.out.println(message);
    }
    private static void logHeading(String heading) {
        if (printDebugMessages) {
            System.out.println("--------- " + heading + " ---------");
        }
        
    }
   
}
