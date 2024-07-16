package LexicalAnalysis;
import java.util.*;

import AnyPercent.AnyPercent;

import static LexicalAnalysis.Type.*;

//

public class Lexer {
    private final String source;
    private final ArrayList<Lexeme> lexemes;
    private int currentPosition;
    private int startOfCurrentLexeme;
    private int lineNumber;

    private final HashMap<String, Type> keywords;

    public Lexer(String source) {
        this.source = source;
        this.lexemes = new ArrayList<>();
        this.keywords = getKeywords();
        this.currentPosition = 0;
        this.startOfCurrentLexeme = 0;
        this.lineNumber = 1;

    }

    private char peek() {
        if (isAtEnd()) return 0;
        return source.charAt(currentPosition);
    }

    public ArrayList<Lexeme> lex() {
        while (!isAtEnd()) {
            startOfCurrentLexeme = currentPosition;
            Lexeme nextLexeme = getNextLexeme();
            System.out.println("NextLexeme: "+nextLexeme);
            if (nextLexeme != null) lexemes.add(nextLexeme);
        }
        lexemes.add(new Lexeme(END_OF_FILE, lineNumber));
        System.out.println("returning lexemes");
        return lexemes;
    }

    private Lexeme getNextLexeme() {
        char c = advance();

        switch (c) {
            //ignore indents/line breaks
            case '\t', '\n', '\r' -> {
                return null;
            }
        

        case '[' -> {
            return new Lexeme(OBRACKET, lineNumber);
        }
        case ']' -> {
            return new Lexeme(CBRACKET, lineNumber);
        }
        case '@' -> { 
            return new Lexeme(FACTORIAL, lineNumber);
        }
        case '?' -> {
            return new Lexeme(OR, lineNumber);
        }
        case '&' -> {
            return new Lexeme(AND, lineNumber);
        }
        case ' ' -> { //WHITESPACE LEXEME
            return null;
        }
        case '\'' -> {
            return new Lexeme(RETURNINDICATOR, lineNumber);
        }


        //One or two char tokens
        case '+' -> {
            if (match('+')) return new Lexeme(PLUS_PLUS, lineNumber);
            else if (match('=')) return new Lexeme(PLUS_EQUALS, lineNumber);
            else return new Lexeme(PLUS, lineNumber);
        }
        case '-' -> {
            if (match('-')) return new Lexeme(MINUS_MINUS, lineNumber);
            else if (match('=')) return new Lexeme(MINUS_EQUALS, lineNumber);
            else return new Lexeme(MINUS, lineNumber);
        }
        case '*' -> {
            if (match('*')) return new Lexeme(TIMES_TIMES, lineNumber);
            else if (match('=')) return new Lexeme(TIMES_EQUALS, lineNumber);
            else return new Lexeme(TIMES, lineNumber);
        }
        case '/' -> {
            if (match('=')) return new Lexeme(DIVIDE_EQUALS, lineNumber);
            else return new Lexeme(DIVIDE, lineNumber);
        }
        case '%' -> {
            if (match('=')) return new Lexeme(MOD_EQUALS, lineNumber);
            else return new Lexeme(MOD, lineNumber);
        }
        case '^' -> {
            if (match('^')) return new Lexeme(POWEROF_POWEROF, lineNumber);
            else return new Lexeme(POWEROF, lineNumber);
        }
        case '>' -> {
            if (match('=')) return new Lexeme(GREATEROREQUALS, lineNumber);
            else return new Lexeme(GREATER, lineNumber);
        }
        case '<' -> {
            if (match('=')) return new Lexeme(LESSOREQUALS, lineNumber);
            else return new Lexeme(LESS, lineNumber);
        }
        case '=' -> {
            if (match('=')) return new Lexeme(EQUALS_EQUALS, lineNumber);
            else return new Lexeme(EQUALS, lineNumber);
        }
        case '!' -> {
            if (match('=')) return new Lexeme(DOESNOTEQUAL, lineNumber);
            else return new Lexeme(NOT, lineNumber);
        }
        case '"' -> {
            return lexString();
        }
        default -> {
            if (isDigit(c)) return lexNumber();
            else if (isAlpha(c)) return lexIdentifierOrKeyword();
            else {
                error("Unrecognized character '" + c + "'");
                return null;
            }
        }
    }
    }
    private Lexeme lexString() {
        while (!(isAtEnd() || peek() == '"')) advance();

        String str = source.substring(startOfCurrentLexeme+1, currentPosition);

        if (isAtEnd()) error("Unterminated string: '" + str + "'");
        else advance(); //munch on that quotation mark

        return new Lexeme(STRING, lineNumber, str);
    }
    
    private Lexeme lexNumber() {
        boolean isInteger = true;
        while (isDigit(peek())) advance();

        //if we stop seeing digits bc of a .
        if (peek() == '.') {
            isInteger = false;

            //if no more nums after .
            if (!isDigit(peekNext())) {
                //cant be a floating point number either
                String malformedReal = source.substring(startOfCurrentLexeme, currentPosition+1);
                error("Malformed real number: '" + malformedReal + "'");
            }
            advance();

            while (isDigit(peek())) advance();

        }
        String numberString = source.substring(startOfCurrentLexeme, currentPosition);
        if (isInteger) {
            int intValue = Integer.parseInt(numberString);
            return new Lexeme(INTEGER, lineNumber, intValue);
        } else {
            double realValue = Double.parseDouble(numberString);
            return new Lexeme(DOUBLE, lineNumber, realValue);
        }
    }


    private Lexeme lexIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(startOfCurrentLexeme, currentPosition);
        Type type = keywords.get(text);

        //if no type found, treat as identifier
        if (type == null)  {

            return new Lexeme(IDENTIFIER, lineNumber, text); }
            
            else if (type == TRUE) return new Lexeme(BOOLEAN, lineNumber, true);
            else if (type == FALSE) return new Lexeme(BOOLEAN, lineNumber, false);

            
            //Check if they're is and es. Make a little loop update: cant figure it out so I'm going to use spaces and change it with your help tomorrow

        return new Lexeme(type, lineNumber);
    }


    private char peekNext() {
        if (currentPosition + 1 >= source.length()) return '\0';
        return source.charAt(currentPosition+1);
    }
    private boolean match(char expected) {
        if(isAtEnd() || source.charAt(currentPosition) != expected ) return false;
        currentPosition++;
        return true;
    } 
    private boolean isAtEnd() {
        return currentPosition >= source.length();
    }
    private char advance() {
        char currentChar = source.charAt(currentPosition);
        if(currentChar == '\n' || currentChar == '\r') lineNumber++;
        currentPosition++;
        return currentChar;
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'A' && c <= 'Z') ||
               (c >= 'a' && c <= 'z') ||
               (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void error(String message) {
        AnyPercent.syntaxError(message, lineNumber);
    }

    private HashMap<String, Type> getKeywords() {
        HashMap<String, Type> keywords = new HashMap<>();
        //Type:
        keywords.put("I", INTEGER_INDICATOR);
        keywords.put("S", STRING_INDICATOR);
        keywords.put("D", DOUBLE_INDICATOR);
        keywords.put("C", CHAR_INDICATOR);
        keywords.put("B", BOOLEAN_INDICATOR);


        //Loops/Conditionals
        keywords.put("L", LOOPINITIALIZATION);
        keywords.put("f", FOR);
        keywords.put("w", WHILE);
        keywords.put("wt", WHILETRUE);
        keywords.put("i", IF);
        keywords.put("e", ELSE);

        //Functions:
        keywords.put("d", FUNCTIONDECLARATION);
        keywords.put("p", PRINT);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("n", CONSISTENCYINDICATOR);
        keywords.put("o", OPENFUNCTION);
        keywords.put("c", CLOSEDFUNCTION);








        return keywords;
    }
}
