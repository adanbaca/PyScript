import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Make sure to check if we are at the end of a line correctly or not

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

    public ArrayList<Token> tokenize(String input) {
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

    public static void main(String[] args) throws ParseException {
        Tokenizer tokenizer = new Tokenizer();
        String input = "let x = (a and True)";
        ArrayList<Token> tokens = tokenizer.tokenize(input);
        Grammar2 grammar = new Grammar2(tokens);

        for (Token token : tokens) {
            System.out.println(token);
        }
        grammar.parse();
    }

}

public class Grammar2 {


    public List<Tokenizer.Token> tokens;
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

    public void addTokens(List<Tokenizer.Token> newTokens) {
        tokens.addAll(newTokens);
    }

    private boolean atEnd(){
        return curr >= tokens.size() || tokens.get(curr).type == Tokenizer.Type.EOF;
    }

    public boolean parse() throws ParseException {
        while (!atEnd()){
            if (!parseProgram()){
                throw new ParseException("Syntax Error", 0);
            }
        }
        return true;
    }

    private boolean parseProgram() {
        return parseBlock();
    }

    private boolean parseBlock(){
        while (!atEnd() && tokens.get(curr).type != Tokenizer.Type.BRACE_CLOSE){
            if (!parseStatement()){
                return false;
            }
        }
        return true;
    }

    private boolean parseStatement() {
        if (match(Tokenizer.Type.LET)) {
            return parseVarDec();
        } else if (match(Tokenizer.Type.IF)) {
            return parseCond();
        } else if (match(Tokenizer.Type.LOOP)) {
            return parseLoop();
        } else if (match(Tokenizer.Type.PRINT)) {
            return parsePrint();
        } else if (match(Tokenizer.Type.VAR_NAME)) {
            return parseAssign();
        }
        return false;
    }

    private boolean parseVarDec(){
        if (!match(Tokenizer.Type.VAR_NAME)){
            return false;
        }
        if (!match(Tokenizer.Type.ASSIGNMENT)){
            return false;
        }
        return parseExpression();
    }

    private boolean parseCond(){
        if (!match(Tokenizer.Type.PAREN_OPEN)||!parseExpression()||!match(Tokenizer.Type.PAREN_CLOSE)){
            return false;
        }
        if (!match(Tokenizer.Type.BRACE_OPEN)||!parseBlock()||!match(Tokenizer.Type.BRACE_CLOSE)){
            return false;
        }
        if (match(Tokenizer.Type.ELIF)){
            return parseCond();
        }
        if (match(Tokenizer.Type.ELSE)){
            if (!match(Tokenizer.Type.BRACE_OPEN)||!parseBlock()||!match(Tokenizer.Type.BRACE_CLOSE)){
                return false;
            }
        }
        return true;
    }

    private boolean parseLoop(){
        if (!match(Tokenizer.Type.PAREN_OPEN) || !parseExpression() || !match(Tokenizer.Type.PAREN_CLOSE)){
            return false;
        }

        if (!match(Tokenizer.Type.BRACE_OPEN) || !parseExpression() || !match(Tokenizer.Type.PAREN_CLOSE)){
            return false;
        }

        return true;

    }

    private boolean parsePrint(){
        boolean expr = false;
        if (match(Tokenizer.Type.PRINT)&&match(Tokenizer.Type.PAREN_OPEN)){
            expr = parseExpression();
        }
        return expr&&match(Tokenizer.Type.PAREN_CLOSE);
    }

    private boolean parseAssign(){
        if (!match(Tokenizer.Type.VAR_NAME)){
            return false;
        }

        if (!match(Tokenizer.Type.ASSIGNMENT)){
            return false;
        }
        return parseExpression();
    }

    private boolean parseExpression(){
        int oldCur = curr;
        if (parseNumExpression()) {
            return true;
        }
        curr = oldCur;
        oldCur = curr;
        if (parseBoolExpression()){
            return true;
        }
//        else if (parseStrExpression()){
            // evaluate string expressi}
        curr = oldCur;
        oldCur = curr;
        if (match(Tokenizer.Type.VAR_NAME)){
            return true;
        }
        return false;
    }

    private boolean parseNumExpression() {
        // Parse a term
        if (!parseTerm()) {
            return false;
        }

        // Check for additional terms
        while (match(Tokenizer.Type.NUM_OPERATOR)) {
            if (!parseTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseTerm() {
        // Check for a factor
        if (!parseFactor()) {
            return false;
        }

        // Check for additional factors
        while (match(Tokenizer.Type.NUM_OPERATOR)) {
            if (!parseFactor()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseFactor() {
        // Check for a number
        if (match(Tokenizer.Type.INT)) {
            return true;
        }

        // Check for a variable name
        if (match(Tokenizer.Type.VAR_NAME)) {
            return true;
        }

        // Check for a parenthesized expression
        if (match(Tokenizer.Type.PAREN_OPEN)) {
            if (!parseNumExpression()) {
                return false;
            }
            if (!match(Tokenizer.Type.PAREN_CLOSE)) {
                return false;
            }
            return true;
        }

        return false;
    }

    private boolean parseBoolExpression() {
        // Parse a bool term
        if (!parseBoolTerm()) {
            return false;
        }

        // Check for additional bool terms
        while (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseBoolTerm() {
        // Check for a bool factor
        if (!parseBoolFactor()) {
            return false;
        }

        // Check for additional bool factors
        while (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolFactor()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseBoolFactor() {
        // Check for a boolean literal
        if (match(Tokenizer.Type.BOOLEAN)) {
            return true;
        }

        // Check for a comparison expression
        if (parseComparisonExpression()) {
            return true;
        }

        // Check for a variable name
        if (match(Tokenizer.Type.VAR_NAME)) {
            return true;
        }

        // Check for a parenthesized bool expression
        if (match(Tokenizer.Type.PAREN_OPEN)) {
            if (!parseBoolExpression()) {
                return false;
            }
            if (!match(Tokenizer.Type.PAREN_CLOSE)) {
                return false;
            }
            return true;
        }

        return false;
    }

    private boolean parseComparisonExpression() {
        // Parse the first operand
        int oldCur = curr;
        if (!parseNumExpression()) {
            return false;
        }
        curr = oldCur;
        oldCur = curr;
        // Check for a comparison operator
        if (!match(Tokenizer.Type.COMPARISON_OPERATOR)) {
            return false;
        }

        if (!parseNumExpression()) {
            return false;
        }
        curr = oldCur;

        return true;
    }
}


