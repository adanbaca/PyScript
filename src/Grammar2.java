import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;


class Tokenizer {
    public enum Type {
        INT("^\\d+"),
        VARNAME("^[a-zA-Z]\\w*"),
        STRING("^\"[^\"]*\""),
        BOOLEAN("^(True|False)"),
        OPERATOR("^[+\\-*/%=]"),
        PAREN_OPEN("^\\("),
        PAREN_CLOSE("^\\)"),
        LOOP("^while"),
        IF("^if"),
        ELSE("^else"),
        ELIF("^elif"),
        LET("^let"),
        WHITESPACE("^\\s+"),
        EOF("");

        public final String pattern;
        Type(String pattern){
            this.pattern = pattern;
        }
    }

    private class Token {
        public Type type;
        public String lexeme;

        public Token(Type type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        @Override
        public String toString() {
            return type + " " + lexeme;
        }
    }

}


public class Grammar2 {
    // Define regex patterns for variable name and expression
//    Pattern variableNamePattern = Pattern.compile("^[a-zA-Z]\\w*[a-zA-Z_]\\w*$");
    
    private static Pattern bool = Pattern.compile("^t$|^f$");

    private static Pattern ints = Pattern.compile("^\\d+$");
    private static Pattern var = Pattern.compile("^\\w+$");


    private Stack<String> blockStack = new Stack<String>();
    public HashMap<String, HashMap<String, Object>> globalVariables= new HashMap<String, HashMap<String, Object>>();


    public static boolean program(String[] input){
        return block(input);
    }

    public static boolean block(String[] input){
        if (statement(input)) {
            return true;
        }
        else if (statementBlock(input)){
            return true;
        }
        return false;
    }

    public static boolean statementBlock(String[] input){
        return false;

    }

    public static boolean statement(String[] input){
        if (varDec(input)){
            return true;
        }
        else if (assignment(input)){
            return true;
        }
        else if (conditional(input)) {
            return true;
        }
        else if (loop(input)){
            return true;
        }
        else if (printStatement(input)){
            return true;
        }
        return false;

    }
    public static boolean varDec(String[] input){
        if (input.length==7&&input[0].equals("let")&&input[4].equals("=")) {
            if (varName(input[2]) && expr(input[6])){
                return true;
            }
        }
        return false;
    }

    public static boolean assignment(String[] input){
        if (input[1].equals("=")){
            if input[0].
        }
    }

    public static boolean cond(String[] input){

    }




}


