import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Tokenizer {
    public enum Type {
        INT("^\\d+"),
        STRING("^\"[^\"]*\""),
        BOOLEAN("^(True|False)\\b"),
        COMPARISON_OPERATOR("^(==|!=|<=|>=|<|>)"),
        ASSIGNMENT("^="),
        NUM_OPERATOR("^[+\\-*/%]"),
        BOOL_OPERATOR("^(not|and|or)\\b"),
        BRACE_OPEN("^\\{"),
        BRACE_CLOSE("^\\}"),
        PAREN_OPEN("^\\("),
        PAREN_CLOSE("^\\)"),
        LOOP("^while\\b"),
        PRINT("^print\\b"),
        IF("^if\\b"),
        ELSE("^else\\b"),
        ELIF("^elif\\b"),
        LET("^let\\b"),
        VAR_NAME("^[a-zA-Z]\\w*"),
        WHITESPACE("^\\s+"),
        EOF("");

        public final String pattern;
        Type(String pattern){
            this.pattern = pattern;
        }
    }

    public class Token {
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

    private ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        while (!input.isEmpty()) {
            boolean matched = false;

            for (Type type : Type.values()) {
                Pattern pattern = Pattern.compile(type.pattern);
                Matcher matcher = pattern.matcher(input);
                if (matcher.find() && matcher.start() == 0) {
                    matched = true;
                    String lexeme = matcher.group();
                    tokens.add(new Token(type, lexeme));
                    input = input.substring(lexeme.length());
                    if (type == Type.WHITESPACE) {
                        // Ignore whitespace tokens for the output
                        tokens.remove(tokens.size() - 1);
                    }
                    break;
                }
            }

            if (!matched) {
                throw new IllegalArgumentException("Unexpected character in input: " + input);
            }
        }

        tokens.add(new Token(Type.EOF, ""));  // End of file token
        return tokens;
    }

    public static void main(String[] args) {
        Tokenizer tokenizer = new Tokenizer();
        String input = "notTrue";
        ArrayList<Token> tokens = tokenizer.tokenize(input);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

}

public class Grammar2 {
    private List<Tokenizer.Token> tokens;
    private int curr = 0;

    public Grammar2(List<Tokenizer.Token> tokens){
        this.tokens = tokens;
    }

    private boolean match(Tokenizer.Type expected){
        if (atEnd()){
            return false;
        }
        if (tokens.get(curr).type != expected){
            return false;
        }
        curr++;
        return true;
    }

    private boolean atEnd(){
        return curr >= tokens.size() || tokens.get(curr).type == Tokenizer.Type.EOF;
    }

//    public boolean parse() throws ParseException {
//        while (!atEnd()){
//            if (!parseProgram()){
//                throw new ParseException("Syntax Error", 0);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean parseProgram() {
//
//        return false;
//    }
}


