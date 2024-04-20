import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

//Make sure to check if we are at the end of a line correctly or not

class Tokenizer {
    public enum Type {
        INT("^\\d+"),
        STRING("^\"[^\"]*\""),
        BOOLEAN("^(True|False)\\b"),
        COMPARISON_OPERATOR("^(==|!=|<=|>=|<|>)"),
        ASSIGNMENT("^="),
        NUM_OPERATOR("^[+\\-*/%]"),
        BOOL_OPERATOR("^(and|or)\\b"),
        STR_OPERATOR("^@"),
        BOOL_NOT("^(not)\\b"),
        BRACE_OPEN("^\\{"),
        BRACE_CLOSE("^\\}"),
        PAREN_OPEN("^\\("),
        PAREN_CLOSE("^\\)"),
        LOOP("^while\\b"),
        PUTS("^puts\\b"),
        PRINT("^print\\b"),
        IF("^if\\b"),
        ELSE("^else\\b"),
        ELIF("^elif\\b"),
        LET("^let\\b"),
        INPUT("^[bsi]Input$"),
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
        String input = "if(5>4){";
        ArrayList<Token> tokens = tokenizer.tokenize(input);
        Grammar2 grammar = new Grammar2(tokens);

        for (Token token : tokens) {
            //System.out.println(token);
        }
        grammar.parse();
        //System.out.println(grammar.globalVariables);
    }

}

public class Grammar2 {


    public List<Tokenizer.Token> tokens;
    public HashMap<String, HashMap<String, Object>> globalVariables = new HashMap<>();
    public ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalBlockList = new ArrayList<>();
    public ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalStmtList = new ArrayList<>();
    public ArrayList<List<Tokenizer.Token>> curConditionalBlockList = new ArrayList<>();
    public ArrayList<List<Tokenizer.Token>> curConditionalStmtsList = new ArrayList<>();
    private Stack<Tokenizer.Type> bracketStack = new Stack<>();
    private boolean inCondBlock = false;
    private boolean condChain = false;
    private boolean ranChain = false;
    private Execute exec = new Execute();
    public int curr = 0;

    public Grammar2(List<Tokenizer.Token> tokens){
        this.tokens = tokens;
    }
    public Grammar2(){}

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
        tokens = newTokens;
    }
    public void addVariables(HashMap<String, HashMap<String, Object>> newVariables){
        for (String variable : newVariables.keySet()){
            globalVariables.put(variable, newVariables.get(variable));
        }
    }
    private ArrayList<List<Tokenizer.Token>> copyList(ArrayList<List<Tokenizer.Token>> orig){
        ArrayList<List<Tokenizer.Token>> newList = new ArrayList<List<Tokenizer.Token>>();
        for (List<Tokenizer.Token> tokens : orig){
            newList.add(tokens);
        }
        return newList;
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
        curr = 0;
        return true;
    }

    private boolean parseProgram() {
        return parseBlock();
    }

    private boolean parseBlock(){
        if (!(tokens.getFirst().type==Tokenizer.Type.ELIF)&&!(tokens.getFirst().type==Tokenizer.Type.ELSE)&&
                !(tokens.getFirst().type==Tokenizer.Type.BRACE_CLOSE)&&!inCondBlock){
            condChain = false;
            ranChain = false;
            conditionalStmtList.clear();
            conditionalBlockList.clear();
        }
        while (!atEnd()){
            if (!parseStatement()){
                return false;
            }
        }
        if (inCondBlock){
            curConditionalBlockList.add(tokens);
            if (tokens.getFirst().type==Tokenizer.Type.BRACE_CLOSE ){
                bracketStack.pop();
                if (bracketStack.isEmpty()) {
                    ArrayList<List<Tokenizer.Token>> copyList = copyList(curConditionalBlockList);
                    conditionalBlockList.add(copyList);
                    curConditionalBlockList.clear();
                    copyList = copyList(curConditionalStmtsList);
                    for (int i = 0; i < copyList.size(); i++) {
                        if (conditionalStmtList.size() <= i) {
                            conditionalStmtList.add(new ArrayList<>());
                        }
                        conditionalStmtList.get(i).add(copyList.get(i));
                    }
                    curConditionalStmtsList.clear();
                    if (bracketStack.isEmpty() && atEnd()) {
                        inCondBlock = false;

                    }
                }
            }
        }
        if (!inCondBlock && !conditionalBlockList.isEmpty()){
            try {
                if((condChain&& !ranChain )||!condChain) {
                    boolean loop = conditionalBlockList.get(0).get(0).get(0).type==Tokenizer.Type.LOOP;
                    globalVariables = exec.executeConditionalExpression(globalVariables, conditionalBlockList,
                            conditionalStmtList,loop);
                    ranChain = (exec.getRanChain()) ? true : ranChain;
                    //System.out.println("Returning");
                    return true;
                }
            } catch (IllegalArgumentException _) {
                System.out.println("parseConditionalError:I");
            } catch (ParseException e) {
                System.out.println("parseConditionalError:P");
            }
        }

        return true;
    }

    private boolean parseStatement() {
        if (match(Tokenizer.Type.LET)) {
            return parseVarDec();
        } else if (match(Tokenizer.Type.IF)||match(Tokenizer.Type.ELIF)||match(Tokenizer.Type.ELSE)) {
            if (tokens.getFirst().type==Tokenizer.Type.ELIF||tokens.getFirst().type==Tokenizer.Type.ELSE){
                condChain = true;
            }else if (!inCondBlock){
                condChain = false;
                ranChain = false;
            }
            return parseCond();
        } else if (match(Tokenizer.Type.LOOP)) {
            return parseCond();
        } else if (match(Tokenizer.Type.PRINT)||match(Tokenizer.Type.PUTS)) {
            if(parsePrint()){
                try {
                    if (!inCondBlock) exec.executePrintExpression(tokens, globalVariables);
                    //System.out.println("Returning");
                    return true;
                } catch (IllegalArgumentException _) {
                    System.out.println("parsePrintError");
                }
            }
            else return false;
        } else if (match(Tokenizer.Type.VAR_NAME)) {
            curr = 0;
            return parseAssign();
        } else if (match(Tokenizer.Type.BRACE_CLOSE)&&inCondBlock){
            return true;
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
        if (tokens.get(0).type==Tokenizer.Type.ELSE){
            if (!match(Tokenizer.Type.BRACE_OPEN)){
                return false;
            }
            bracketStack.push(Tokenizer.Type.BRACE_OPEN);
            curConditionalStmtsList.add(tokens);
        } else if(!match(Tokenizer.Type.PAREN_OPEN)||!parseExpression()){
            return false;
        }
        inCondBlock = true;
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
        if (match(Tokenizer.Type.PAREN_OPEN)){
            return parseExpression();
        }
        return false;
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
            if (curr == tokens.size()-1) {
                try {
                    if (!inCondBlock) globalVariables = exec.executeNumExpression(tokens, globalVariables);
                    //System.out.println("Returning");
                    return true;
                } catch (IllegalArgumentException _) {
                    System.out.println("parseNumError");
                }
            }else if (match(Tokenizer.Type.PAREN_CLOSE)&&tokens.get(0).type== Tokenizer.Type.PRINT){
                return true;
            }
        }

        curr = oldCur;
        oldCur = curr;
        if (parseBoolExpression()) {
            if (curr == tokens.size() - 1) {
                try {
                    if (!inCondBlock) globalVariables = exec.executeBoolExpression(tokens, globalVariables);
                    //            System.out.println("Returning");
                    //System.out.println();
                    return true;
                } catch (IllegalArgumentException _) {
                    System.out.println("parseBoolError");
                }

            } else if (match(Tokenizer.Type.PAREN_CLOSE)) {
                if (tokens.get(0).type == Tokenizer.Type.PRINT) {
                    return true;
                } else if (match(Tokenizer.Type.BRACE_OPEN) && (tokens.get(0).type == Tokenizer.Type.IF ||
                        tokens.get(0).type == Tokenizer.Type.ELIF ||
                        tokens.get(0).type == Tokenizer.Type.LOOP)) {
                    bracketStack.push(Tokenizer.Type.BRACE_OPEN);
                    curConditionalStmtsList.add(tokens.subList(1,curr-1));
                    return true;
                }

            }
        }
        curr = oldCur;
        oldCur = curr;
        if (parseInputExpression()){
            if (curr == tokens.size()-1){
                try {
                    if (!inCondBlock) globalVariables = exec.executeInputExpression(tokens, globalVariables);
                    //            System.out.println("Returning");
                    //System.out.println();
                    return true;
                } catch (IllegalArgumentException _) {
                    System.out.println("execInputError");
                }
                return true;
            }else if (match(Tokenizer.Type.PAREN_CLOSE)&&tokens.get(0).type== Tokenizer.Type.PRINT){
                return true;
            }
        }
        curr = oldCur;
        oldCur = curr;
        if (parseStrExpression() ){
            if (curr == tokens.size()-1){
                try {
                    if (!inCondBlock) globalVariables = exec.executeStrExpression(tokens, globalVariables);
//            System.out.println("Returning");
                    //System.out.println();
                    return true;
                } catch (IllegalArgumentException _) {
                    System.out.println("execStrError");
                }
                return true;
            }else if(match(Tokenizer.Type.PAREN_CLOSE)&&(tokens.get(0).type== Tokenizer.Type.PRINT||tokens.get(0).type== Tokenizer.Type.PUTS)){
                return true;
            }
        }
        curr = oldCur;
        if (match(Tokenizer.Type.VAR_NAME)){
            if(!match(Tokenizer.Type.PAREN_CLOSE)&&(tokens.get(0).type== Tokenizer.Type.PRINT||tokens.get(0).type== Tokenizer.Type.PUTS)) return false;
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
            int oldCur = curr;
            if (!parseNumExpression()) {
                curr = oldCur;
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
        if (!parseBoolTerm()) {
            return false;
        }

        while (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseBoolTerm() {
        if (!parseBoolFactor()) {
            return false;
        }

        while (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolFactor()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseBoolFactor() {
        if (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolFactor()) {
                return false;
            }
            return true;
        }
        if (match(Tokenizer.Type.BOOL_NOT)) {
            if (!parseBoolFactor()) {
                return false;
            }
            return true;
        }


        if (match(Tokenizer.Type.BOOLEAN)) {
            return true;
        }

        int oldCurr = curr;
        if (parseComparisonExpression()) {
            return true;
        }
        curr = oldCurr;

        if (match(Tokenizer.Type.VAR_NAME)) {
            return true;
        }

        if (match(Tokenizer.Type.PAREN_OPEN)) {
            oldCurr = curr;
            if (!parseBoolExpression()) {
                curr = oldCurr;
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
            curr = oldCur;
            return false;
        }
        // Check for a comparison operator
        if (!match(Tokenizer.Type.COMPARISON_OPERATOR)) {
            return false;
        }
        oldCur = curr;
        if (!parseNumExpression()) {
            curr = oldCur;
            return false;
        }

        return true;
    }

    private boolean parseStrExpression() {
        // Parse a str term
        if (!parseStrTerm()) {
            return false;
        }

        // Check for additional str terms
        while (match(Tokenizer.Type.STR_OPERATOR)) {
            if (!parseStrTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseStrTerm() {
        // Check for a str factor
        if (!parseStrFactor()) {
            return false;
        }

        // Check for additional str factors
        while (match(Tokenizer.Type.STR_OPERATOR)) {
            if (!parseStrFactor()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseStrFactor() {
        // Check for a string literal
        if (match(Tokenizer.Type.STRING)) {
            return true;
        }

        // Check for a variable name
        if (match(Tokenizer.Type.VAR_NAME)) {
            return true;
        }

        // Check for a parenthesized str expression
        if (match(Tokenizer.Type.PAREN_OPEN)) {
            int oldCurr = curr;
            if (!parseStrExpression()) {
                curr = oldCurr;
                return false;
            }
            if (!match(Tokenizer.Type.PAREN_CLOSE)) {
                return false;
            }
            return true;
        }

        return false;
    }
    private boolean parseInputExpression(){
        if (match(Tokenizer.Type.INPUT)) {
            return true;
        }
        return false;
    }
}



