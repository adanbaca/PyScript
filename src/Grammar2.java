import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;


/**
 * Class: Tokenizer
 * Custom made tokenizer that allows us to tokenize the file
 * and separate key words and parse the language. It is built
 * with an inner class denoting all the valid key words in
 * our language.
 */
class Tokenizer {
    public static int lines = 1;
    private Stack<Tokenizer.Type> bracketStack = new Stack<>();

    /**
     * Represents all the valid key words in our language, including
     * regex pattern matching for types like ints, bools and strings.
     * Recognizes all operators as well.
     */
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
        WHITESPACE("^[\\s\\t]+"),
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


    /**
     * Function: tokenize
     * @param input - A string representing a single line from the code file
     * @return - Returns a list of valid tokens that were in the line
     * Description: Tokenizes the keywords found in a string of input and
     * returns them as an array list of Tokens
     */
    public ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        String line = input;
        int index = 0; // use index for error displaying

        while (!input.isEmpty()) { // loop through each key word in the line
            boolean matched = false;

            for (Type type : Type.values()) { // checking which type each token is

                if (type == Type.EOF){
                    continue;
                }
                Pattern pattern = Pattern.compile(type.pattern);
                Matcher matcher = pattern.matcher(input);
                if (matcher.find() && matcher.start() == 0) {
                    matched = true; // if there is a match, we add it to the tokens list
                    String lexeme = matcher.group();
                    tokens.add(new Token(type, lexeme));
                    input = input.substring(lexeme.length());

                    // error checking to make sure brackets line up
                    handleBrackets(type, index, line);

                    index += lexeme.length();
                    if (type == Type.WHITESPACE) {
                        // Ignore whitespace tokens for the output
                        tokens.remove(tokens.size() - 1);
                    }
                    break;
                }
            }
            // handling unexpected characters or symbols
            if (!matched) {
                String errorMsg = "Unexpected character in input: " + input.charAt(0) + "\n";
                String errorIndicator = makeErrorIndicator(line, index);
                errorMsg += errorIndicator;
                throw new IllegalArgumentException(errorMsg);
            }
        }

        tokens.add(new Token(Type.EOF, ""));  // End of file token
        return tokens;
    }

    /**
     * Function: makeErrorIndicator
     * @param line - the line the error was encountered on
     * @param index - the index of the line the error was encountered on
     * @return - Returns a string containing the line where the error is,
     * including the line number and index of the line, as well as another line
     * under it which points out where on the line the error is
     */
    private String makeErrorIndicator(String line, int index){
        String errorLine = "In line " + lines + ", index " + (index+1) + ": " + line;
        int length = errorLine.substring(0, 21).length();
        String errorIndicator = " ".repeat(Math.max(0, index-1 + length));
        return errorLine + "\n" + errorIndicator + "^";
    }

    /**
     * Function: handleBrackets
     * @param type The tokenizer type to check (should be BRACE_OPEN/CLOSE or
     *             PAREN_OPEN/CLOSE)
     * @param index - The line index for error displaying
     * @param line - The line the unclosed bracket error was found on
     * Description: Uses a stack to determine if brackets/parentheses are handled
     *             correctly.
     */
    public void handleBrackets(Type type, int index, String line){
        switch (type){
            case BRACE_OPEN:
            case PAREN_OPEN:
                bracketStack.push(type);
                break;
            case BRACE_CLOSE:
                if (bracketStack.isEmpty() || bracketStack.peek() != Type.BRACE_OPEN){
                    String errorMsg = "Unmatched closing brace '}' at index " + index + "\n";
                    String errorIndicator = makeErrorIndicator(line, index);
                    errorMsg += errorIndicator;
                    throw new IllegalArgumentException(errorMsg);
                } // if no bracket error, pop off open bracket off stack
                bracketStack.pop();
                break;
            case PAREN_CLOSE:
                if (bracketStack.isEmpty() || bracketStack.peek() != Type.PAREN_OPEN){
                    String errorMsg = "Unmatched closing paren ')' at index " + index + "\n";
                    String errorIndicator = makeErrorIndicator(line, index);
                    errorMsg += errorIndicator;
                    throw new IllegalArgumentException(errorMsg);
                }
                bracketStack.pop();
                break;
        }
    }

    /**
     * Function: checkBrackets
     * @param inputLines - List of Strings, should contain all strings from the
     *                   input file.
     * Description: Should be used to check the brackets stack before parsing the
     *                   grammar. Will throw an error if there is an unclosed
     *                   bracket or parenthesis.
     */
    public void checkBrackets(List<String> inputLines){
        for (String line: inputLines){
            ArrayList<Token> tokens = tokenize(line); // populate bracket stack
            lines++;
        }
        if (!bracketStack.isEmpty()){ // check for remaining unclosed brackets or parents in stack
            Type unclosed = bracketStack.peek();
            String notClosed = "";
            switch (unclosed){
                case PAREN_OPEN -> notClosed = "(";
                case PAREN_CLOSE -> notClosed = ")";
                case BRACE_OPEN -> notClosed = "{";
                case BRACE_CLOSE -> notClosed = "}";
            }
            throw new IllegalArgumentException("Unclosed bracket or parenthesis at end of input: " + notClosed );
        }
    }
}

/**
 * Class: Grammar2
 * Class that implements the rules of our grammar, and enforces them while parsing
 * to detect the functionality of a line. Uses recursive descent parsing to go through
 * our grammar and determine the line type and/or possibly evaluate it.
 */
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

    /**
     * Function: match()
     * @param expected - The expected tokenizer type to check for a match
     * @return - Returns false if at the end of a line, or no match. Returns
     * true if not at the end of a line and there is a match
     */
    private boolean match(Tokenizer.Type expected){
        if (atEnd()){
            return false;
        }
        if (tokens.get(curr).type != expected){
            return false;
        }
        curr++; // advance curr pointer to move further down the line tokens
        return true;
    }

    /**
     * function: addTokens
     * @param newTokens - the newTokens to parse
     * Description: Use every time parsing a new line, will assign the class token
     *                  List attribute to the newly determined list of tokenized
     *                  character.
     */
    public void addTokens(List<Tokenizer.Token> newTokens) {
        tokens = newTokens;
    }

    /**
     * function: addVariables()
     * Adds global variables to the global variables hashMap to allow for
     * the reuse or reassignment of variables that have been previously declared
     * @param newVariables - the new variables declared in the most recent line to add
     *                     to the hashmap
     */
    public void addVariables(HashMap<String, HashMap<String, Object>> newVariables){
        for (String variable : newVariables.keySet()){
            globalVariables.put(variable, newVariables.get(variable));
        }
    }

    /**
     * function: copyList
     * @param orig
     * @return
     * Description: Used to create a deep copy of a list
     */
    private ArrayList<List<Tokenizer.Token>> copyList(ArrayList<List<Tokenizer.Token>> orig){
        ArrayList<List<Tokenizer.Token>> newList = new ArrayList<List<Tokenizer.Token>>();
        for (List<Tokenizer.Token> tokens : orig){
            newList.add(tokens);
        }
        return newList;
    }

    /**
     * function: atEnd()
     * @return - returns if we are at the end of a line or file in the parsing process.
     */
    private boolean atEnd(){
        return curr >= tokens.size() || tokens.get(curr).type == Tokenizer.Type.EOF;
    }

    /**
     * parse()
     * @return - true if parsing was successful, throws an error otherwise
     * @throws ParseException
     * Description: Begins the parsing process, calls parseProgram and continues
     * down the grammar rules.
     */
    public boolean parse() throws ParseException {
        while (!atEnd()){ // continue parsing until at the end of line or file
            if (!parseProgram()){
                throw new ParseException("Syntax Error", 0);
            }
        }
        curr = 0; // reset curr for new line
        return true; // parsing successful
    }

    /**
     * function - parseProgram()
     * @return - returns whether or not parseBlock (and other subsequent calls) succeed
     * Description: goes further down the parsing process by calling parseBlock().
     */
    private boolean parseProgram() {
        return parseBlock();
    }

    /**
     * function: parseBlock()
     * @return - returns if parsing the block succeeded
     * parses a "block" in our language
     */
    private boolean parseBlock(){
        // determine if we are in a if-elif-else chain
        if (!(tokens.getFirst().type==Tokenizer.Type.ELIF)&&!(tokens.getFirst().type==Tokenizer.Type.ELSE)&&
                !(tokens.getFirst().type==Tokenizer.Type.BRACE_CLOSE)&&!inCondBlock){
            condChain = false;
            ranChain = false;
            conditionalStmtList.clear();
            conditionalBlockList.clear();
        }
        // if not, check if we are in a statement and parse
        while (!atEnd()){
            if (!parseStatement()){
                return false;
            }
        }

        // if we are in an if-elif-else chain, begin evaluating
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
                throw new IllegalArgumentException("Illegal argument in condition");
            } catch (ParseException e) {
                throw new IllegalArgumentException("Exception when running conditional block");
            }
        }

        return true;
    }

    /**
     * function: parseStatement()
     * @return - returns if parsing the statement succeeds
     * description: goes deeper down the language by starting to parse a statement
     * in our grammar
     */
    private boolean parseStatement() {
        if (match(Tokenizer.Type.LET)) { // in a let block, parse the variable declaration
            return parseVarDec();

            // in an if-elif-else block, begin parsing and evaluating process
        } else if (match(Tokenizer.Type.IF)||match(Tokenizer.Type.ELIF)||match(Tokenizer.Type.ELSE)) {
            if (tokens.getFirst().type==Tokenizer.Type.ELIF||tokens.getFirst().type==Tokenizer.Type.ELSE){
                condChain = true;
            }else if (!inCondBlock){
                condChain = false;
                ranChain = false;
            }
            return parseCond();
               // in a loop block, use same process to parse conditional, but will loop
        } else if (match(Tokenizer.Type.LOOP)) {
            return parseCond();

            // printing output, parse a print statement
        } else if (match(Tokenizer.Type.PRINT)||match(Tokenizer.Type.PUTS)) {
            if(parsePrint()){
                try {
                    // printing the correct variables
                    if (!inCondBlock) exec.executePrintExpression(tokens, globalVariables);
                    //System.out.println("Returning");
                    return true;
                } catch (IllegalArgumentException _) {
                    throw new IllegalArgumentException("Invalid format for output");
                }
            }
            else return false;

            // in assignment block, parse the variable assignment (not declaration)
        } else if (match(Tokenizer.Type.VAR_NAME)) {
            curr = 0;
            return parseAssign();

            // closing out conditional block
        } else if (match(Tokenizer.Type.BRACE_CLOSE)&&inCondBlock){
            return true;
        }
        return false;
    }

    /**
     * function parseVarDec()
     * @return - true if the variable declaration succeeds, or if the further parsing
     * succeeds
     * Description: parses a variable declaration by ensuring it follows the following structure:
     * let <var_name> = <expression>
     */
    private boolean parseVarDec(){
        if (!match(Tokenizer.Type.VAR_NAME)){
            return false;
        }
        if (!match(Tokenizer.Type.ASSIGNMENT)){
            return false;
        }

        // if at this point, we have an expression to evaluate/parse, begin the process
        return parseExpression();
    }

    /**
     * function: parseCond()
     * @return - returns the success of parsing a conditional block
     * Decsription: parses a conditional block, and maintaining scope of the block
     * with a bracket stack
     */
    private boolean parseCond(){
        if (tokens.get(0).type==Tokenizer.Type.ELSE){
            if (!match(Tokenizer.Type.BRACE_OPEN)){ // ensuring else is followed directly by a bracket
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

    // do we get rid of this?
    private boolean parseLoop(){
        if (!match(Tokenizer.Type.PAREN_OPEN) || !parseExpression() || !match(Tokenizer.Type.PAREN_CLOSE)){
            return false;
        }

        if (!match(Tokenizer.Type.BRACE_OPEN) || !parseExpression() || !match(Tokenizer.Type.PAREN_CLOSE)){
            return false;
        }

        return true;

    }

    /**
     * parsePrint()
     * @return - returns if a print statement is formatted correctly, with parentheses
     * following the print, and an expression inside
     */
    private boolean parsePrint(){
        if (match(Tokenizer.Type.PAREN_OPEN)){ // ensure print followed by paren
            return parseExpression(); // evaluate expression inside the print
        }
        return false;
    }

    /**
     * function: parseAssign()
     * @return - the success of parsing an assignment of a variable
     * Description: parses the assignment of a variable by ensuring it follows the structure:
     * <var_name> = <expression>
     */
    private boolean parseAssign(){
        if (!match(Tokenizer.Type.VAR_NAME)){
            return false;
        }

        if (!match(Tokenizer.Type.ASSIGNMENT)){
            return false;
        }
        return parseExpression(); // parse the expression after the assignment operator
    }

    /**
     * function: parseExpression
     * @return - returns the success of parsing an expression
     * Description: parses an expression by determining what type it is
     */
    private boolean parseExpression(){
        int oldCur = curr;
        if (parseNumExpression()) { // parsing a numeric expression
            if (curr == tokens.size()-1) {
                try {
                    // executing the numeric expression
                    if (!inCondBlock) globalVariables = exec.executeNumExpression(tokens, globalVariables);
                    //System.out.println("Returning");
                    return true;
                } catch (IllegalArgumentException _) {
                    throw new IllegalArgumentException("Illegal argument in integer expression");
                }
            }else if (match(Tokenizer.Type.PAREN_CLOSE)&&tokens.get(0).type== Tokenizer.Type.PRINT){
                return true;
            }
        }
        // resetting the curr pointer to re-parse
        curr = oldCur;
        oldCur = curr;
        // parsing a bool expression
        if (parseBoolExpression()) {
            if (curr == tokens.size() - 1) {
                try {
                    // executing the boolean expression with java
                    if (!inCondBlock) globalVariables = exec.executeBoolExpression(tokens, globalVariables);
                    //            System.out.println("Returning");
                    //System.out.println();
                    return true;
                } catch (IllegalArgumentException _) {
                    throw new IllegalArgumentException("Illegal argument in boolean expression");
                }

                // evaluating the expression inside of a print or block statement
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

        // parsing the expression inside of an input
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
                    throw new IllegalArgumentException("Illegal Argument in input statement");
                }
                //return true;
            }else if (match(Tokenizer.Type.PAREN_CLOSE)&&tokens.get(0).type== Tokenizer.Type.PRINT){
                return true;
            }
        }
        curr = oldCur;
        oldCur = curr;

        // parsing the evaluation of a string expression
        if (parseStrExpression() ){
            if (curr == tokens.size()-1){
                try {
                    if (!inCondBlock) globalVariables = exec.executeStrExpression(tokens, globalVariables);
//            System.out.println("Returning");
                    //System.out.println();
                    return true;
                } catch (IllegalArgumentException _) {
                    throw new IllegalArgumentException("Illegal argument in string expression");
                }
                //return true;
            }else if(match(Tokenizer.Type.PAREN_CLOSE)&&(tokens.get(0).type== Tokenizer.Type.PRINT||tokens.get(0).type== Tokenizer.Type.PUTS)){
                return true;
            }
        }
        curr = oldCur;

        // if none of the above
        if (match(Tokenizer.Type.VAR_NAME)){
            if(!match(Tokenizer.Type.PAREN_CLOSE)&&(tokens.get(0).type== Tokenizer.Type.PRINT||tokens.get(0).type== Tokenizer.Type.PUTS)) return false;
            return true;
        }
        return false;
    }

    /**
     * function: parseNumExpression
     * begins the process for parsing a numeric expression
     * @return - returns the success of parsing the numeric expression
     */
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

    /**
     * function: parseTerm
     * continues the process of parsing a numeric expression
     * @return - the success of parsing
     */
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

    /**
     * function: parseFactor()
     * Determines what factor we are parsing, an int literal, variable, or
     * further expression
     * @return - the success of parsing the factor
     */
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

    /**
     * function: parseBoolExpression
     * begins the parsing of a boolean expression
     * @return - the success of parsing
     */
    private boolean parseBoolExpression() {
        if (!parseBoolTerm()) {
            return false;
        }

        // check for compounded bool operations
        while (match(Tokenizer.Type.BOOL_OPERATOR)) {
            if (!parseBoolTerm()) {
                return false;
            }
        }

        return true;
    }

    /**
     * function parseBoolTerm()
     * continues the process of parsing a boolean expression
     * @return - the success of parsing the boolean expression
     */
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

    /**
     * function parseBoolFactor
     * Checks the tokens from parsing the boolean expression, checking for operators,
     * bool literals, bool variables, etc.
     * @return - the success of parsing the boolean expression
     */
    private boolean parseBoolFactor() {
        if (match(Tokenizer.Type.BOOL_OPERATOR)) { // continue parsing
            if (!parseBoolFactor()) {
                return false;
            }
            return true;
        }
        if (match(Tokenizer.Type.BOOL_NOT)) { // parse bool expr aftr not
            if (!parseBoolFactor()) {
                return false;
            }
            return true;
        }


        if (match(Tokenizer.Type.BOOLEAN)) { // parsing a bool literal
            return true;
        }

        int oldCurr = curr;
        // parsing comparison expressions as bools
        if (parseComparisonExpression()) {
            return true;
        }
        curr = oldCurr;

        // variable representing a bool
        if (match(Tokenizer.Type.VAR_NAME)) {
            return true;
        }

        // parse expression in parens
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

    /**
     * function: parseComparisonExpression
     * parses a comparison expression which will return a bool
     * @return - success of parsing the comparison
     */
    private boolean parseComparisonExpression() {
        // Parse the first operand
        int oldCur = curr;
        // see if any numeric expressions need to be evaluated/parsed
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

    /**
     * function: parseStrExpression
     * begins the process for parsing a string expression
     * @return - the success of parsing the expression
     */
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

    /**
     * function: parseStrTerm
     * continues the process of parsing a string expression
     * @return - the success of parsing
     */
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

    /**
     * function: parseStrFactor
     * completes the final part of parsing a str expression, checking for string
     * literals, var representing strings, or compounded string operation statements
     * @return - the success of parsing
     */
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

    /**
     * function: parseInput expression
     * Parses an input expression
     * @return - the success of parsing the statement as an input expression
     */
    private boolean parseInputExpression(){
        if (match(Tokenizer.Type.INPUT)) {
            return true;
        }
        return false;
    }
}
