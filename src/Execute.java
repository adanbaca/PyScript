import java.text.ParseException;
import java.util.*;

/**
 * class: Execute
 * The class we use to execute the grammar.
 * This is called within Grammar2 after the input
 * is validated.
 */
public class Execute {
    private int curr = 0;
    private List<Tokenizer.Token> tokens;
    public Execute() {}
    private boolean ranChain;

    /**
     * Method: executeBoolExpression - method that handles execution of boolean expressions
     * @param tokens - list of tokens from the parser
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return globalVariables after the necessary values have been updated
     */
    public HashMap<String, HashMap<String, Object>> executeBoolExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        String varName = (tokens.get(0).type == Tokenizer.Type.LET) ? tokens.get(1).lexeme : tokens.get(0).lexeme;
        curr = (tokens.get(0).type == Tokenizer.Type.LET) ? 3 : 2;

        boolean result = evaluateBoolExpression(globalVariables);

        HashMap<String, Object> varData = new HashMap<>();
        varData.put("val", result);
        varData.put("type", "bool");
        globalVariables.put(varName, varData);

        return globalVariables;
    }

    /**
     * Method: evaluateBoolExpression - helper method that actually handles evaluation
     *          of the expression
     * @param globalVariables- hashmap of variables, their types and values, that is maintained
     *                         over the entire program
     * @return result - boolean value correspoding to expression
     */
    private boolean evaluateBoolExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        boolean result = evaluateBoolTerm(globalVariables);

        while (curr < tokens.size() && tokens.get(curr).type == Tokenizer.Type.BOOL_OPERATOR) {
            String operator = tokens.get(curr).lexeme;
            curr++;

            boolean term = evaluateBoolTerm(globalVariables);

            if (operator.equals("and")) {
                result = result && term;
            } else if (operator.equals("or")) {
                result = result || term;
            } else {
                throw new IllegalArgumentException("Invalid boolean operator: " + operator);
            }
        }

        return result;
    }

    /**
     * Method: evaluateBoolTerm - helper method that evaluates a single term in an expression
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                          over the entire program
     * @return result - boolean corresponding to value of the term
     */
    private boolean evaluateBoolTerm(HashMap<String, HashMap<String, Object>> globalVariables) {
        if (curr >= tokens.size()) {
            throw new IllegalArgumentException("Invalid boolean expression: unexpected end of expression");
        }

        Tokenizer.Token token = tokens.get(curr);

        if (token.type == Tokenizer.Type.BOOLEAN) {
            curr++;
            return Boolean.parseBoolean(token.lexeme);
        } else if (token.type == Tokenizer.Type.VAR_NAME&&(token.type == Tokenizer.Type.VAR_NAME && globalVariables.get(token.lexeme).get("type").equals("bool"))) {
            curr++;
            String varName = token.lexeme;
            if (!globalVariables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not found: " + varName);
            }
            Object varValue = globalVariables.get(varName).get("val");

            return (boolean) varValue;
        } else if (token.type == Tokenizer.Type.BOOL_NOT) {
            curr++;
            return !evaluateBoolTerm(globalVariables);
        } else if (token.type == Tokenizer.Type.PAREN_OPEN) {
            curr++;
            boolean result = evaluateBoolExpression(globalVariables);
            /*if (curr >= tokens.size() || tokens.get(curr).type != Tokenizer.Type.PAREN_CLOSE) {
                throw new IllegalArgumentException("Invalid boolean expression: expected closing parenthesis");
            }*/
            curr++;
            return result;
        } else if (token.type == Tokenizer.Type.INT || token.type == Tokenizer.Type.VAR_NAME) {
            return evaluateComparisonExpression(globalVariables);
        } else {
            throw new IllegalArgumentException("Invalid boolean term: " + token.lexeme);
        }
    }

    /**
     * Method: evaluateComparisonExpression - method that handles evalutation of numerical comparison with a
     *         boolean result
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                         over the entire program
     * @return boolean corresponding to the value of the comparison expression
     */
    private boolean evaluateComparisonExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        int leftOperand = evaluateNumExpression(globalVariables);
        while(tokens.get(curr).type==Tokenizer.Type.PAREN_CLOSE){
            curr++;
        }
        if (curr >= tokens.size() || tokens.get(curr).type != Tokenizer.Type.COMPARISON_OPERATOR) {
            throw new IllegalArgumentException("Invalid comparison expression: expected comparison operator");
        }

        String operator = tokens.get(curr).lexeme;
        curr++;

        int rightOperand = evaluateNumExpression(globalVariables);

        switch (operator) {
            case "<":
                return leftOperand < rightOperand;
            case ">":
                return leftOperand > rightOperand;
            case "<=":
                return leftOperand <= rightOperand;
            case ">=":
                return leftOperand >= rightOperand;
            case "==":
                return leftOperand == rightOperand;
            case "!=":
                return leftOperand != rightOperand;
            default:
                throw new IllegalArgumentException("Invalid comparison operator: " + operator);
        }
    }

    /**
     * Method: executeNumExpression - method that handles execution of integer expressions
     * @param tokens - list of tokens from the parser
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return globalVariables after the necessary values have been updated
     */
    public HashMap<String, HashMap<String, Object>> executeNumExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        String varName = (tokens.get(0).type == Tokenizer.Type.LET) ? tokens.get(1).lexeme : tokens.get(0).lexeme;
        curr = (tokens.get(0).type == Tokenizer.Type.LET) ? 3 : 2;

        int result = evaluateNumExpression(globalVariables);

        HashMap<String, Object> varData = new HashMap<>();
        varData.put("val", result);
        varData.put("type", "int");
        globalVariables.put(varName, varData);

        return globalVariables;
    }
    /**
     * Method: evaluateNumExpression - helper method that actually handles evaluation
     *          of the expression
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return result - integer value corresponding to result of the expression
     */
    private int evaluateNumExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        int result = evaluateNumTerm(globalVariables);

        while (curr < tokens.size() && tokens.get(curr).type == Tokenizer.Type.NUM_OPERATOR) {
            String operator = tokens.get(curr).lexeme;
            curr++;

            int term = evaluateNumTerm(globalVariables);

            switch (operator) {
                case "+":
                    result += term;
                    break;
                case "-":
                    result -= term;
                    break;
                case "*":
                    result *= term;
                    break;
                case "/":
                    result /= term;
                    break;
                case "%":
                    result %= term;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid numeric operator: " + operator);
            }
        }

        return result;
    }

    /**
     * Method: evaluateNumTerm -  helper method that evaluates a single term in an expression
     * @param globalVariables- hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return result - an integer corresponding to the value of the term
     */
    private int evaluateNumTerm(HashMap<String, HashMap<String, Object>> globalVariables) {
        if (curr >= tokens.size()) {
            throw new IllegalArgumentException("Invalid numeric expression: unexpected end of expression");
        }

        Tokenizer.Token token = tokens.get(curr);

        if (token.type == Tokenizer.Type.INT) {
            curr++;
            return Integer.parseInt(token.lexeme);
        } else if (token.type == Tokenizer.Type.VAR_NAME) {
            curr++;
            String varName = token.lexeme;
            if (!globalVariables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not found: " + varName);
            }
            Object varValue = globalVariables.get(varName).get("val");
            if (!(varValue instanceof Integer)) {
                throw new IllegalArgumentException("Variable is not of integer type: " + varName);
            }
            return (int) varValue;
        } else if (token.type == Tokenizer.Type.PAREN_OPEN) {
            curr++;
            int result = evaluateNumExpression(globalVariables);
            /*if (curr >= tokens.size() || tokens.get(curr).type != Tokenizer.Type.PAREN_CLOSE) {
                throw new IllegalArgumentException("Invalid numeric expression: expected closing parenthesis");
            }*/
            curr++;
            return result;
        } else {
            throw new IllegalArgumentException("Invalid numeric term: " + token.lexeme);
        }
    }

    /**
     * Method: executeStrExpression - method that handles execution of string expressions
     * @param tokens - list of tokens from the parser
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return globalVariables after the necessary values have been updated
     */
    public HashMap<String, HashMap<String, Object>> executeStrExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        String varName = (tokens.get(0).type == Tokenizer.Type.LET) ? tokens.get(1).lexeme : tokens.get(0).lexeme;
        curr = (tokens.get(0).type == Tokenizer.Type.LET) ? 3 : 2;

        String result = evaluateStrExpression(globalVariables);

        HashMap<String, Object> varData = new HashMap<>();
        varData.put("val", result);
        varData.put("type", "string");
        globalVariables.put(varName, varData);

        return globalVariables;
    }

    /**
     * Method: evaluateStrExpression -helper method that actually handles evaluation
     *         of the expression
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return result - string after necessary concatenation
     */
    private String evaluateStrExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        String result = evaluateStrTerm(globalVariables);

        while (curr < tokens.size() && tokens.get(curr).type == Tokenizer.Type.STR_OPERATOR) {
            curr++;
            String term = evaluateStrTerm(globalVariables);
            result += term;
        }

        return result;
    }

    /**
     * Method: evaluateStrTerm - helper method that evaluates a single term in an expression
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return result - string corresponding to term in the expression
     */
    private String evaluateStrTerm(HashMap<String, HashMap<String, Object>> globalVariables) {
        if (curr >= tokens.size()) {
            throw new IllegalArgumentException("Invalid string expression: unexpected end of expression");
        }

        Tokenizer.Token token = tokens.get(curr);

        if (token.type == Tokenizer.Type.STRING) {
            curr++;
            return token.lexeme.substring(1, token.lexeme.length() - 1);
        } else if (token.type == Tokenizer.Type.VAR_NAME) {
            curr++;
            String varName = token.lexeme;
            if (!globalVariables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not found: " + varName);
            }
            Object varValue = globalVariables.get(varName).get("val");
            if (!(varValue instanceof String)) {
                throw new IllegalArgumentException("Variable is not of string type: " + varName);
            }
            return (String) varValue;
        } else if (token.type == Tokenizer.Type.PAREN_OPEN) {
            curr++;
            String result = evaluateStrExpression(globalVariables);
            /*if (curr >= tokens.size() || tokens.get(curr).type != Tokenizer.Type.PAREN_CLOSE) {
                throw new IllegalArgumentException("Invalid string expression: expected closing parenthesis");
            }*/
            curr++;
            return result;
        } else {
            throw new IllegalArgumentException("Invalid string term: " + token.lexeme);
        }
    }

    /**
     * Method: executeInputExpression - method that handles execution of input statements
     * @param tokens - list of tokens from the parser
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return globalVariables after the necessary values have been updated
     */
    public HashMap<String, HashMap<String, Object>> executeInputExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        String varName = (tokens.get(0).type == Tokenizer.Type.LET) ? tokens.get(1).lexeme : tokens.get(0).lexeme;
        curr = (tokens.get(0).type == Tokenizer.Type.LET) ? 3 : 2;

        String result = evaluateInputExpression(globalVariables);

        HashMap<String, Object> varData = new HashMap<>();

        String type = (tokens.get(curr).lexeme.startsWith("s")) ? "string" : (tokens.get(curr).lexeme.startsWith("b")) ? "bool" : "int";

        varData.put("val", (type.equals("int") ? Integer.valueOf(result): (type.equals("bool") ? Boolean.valueOf(result): result)));
        varData.put("type", type);
        globalVariables.put(varName, varData);

        return globalVariables;
    }

    /**
     * Method: evaluateInputExpression - helper method that takes console input in
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *          over the entire program
     * @return String corresponding to the next line from the scanner
     */
    private String evaluateInputExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    /**
     * Method: executePrintExpression - method that executes printing/putting of expressions
     * @param tokens - list of tokens from the parser
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     */
    public void executePrintExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        curr = 2;

        String printable = evaluatePrintExpression(globalVariables);
        if (tokens.getFirst().type==Tokenizer.Type.PRINT) System.out.print(printable);
        else System.out.println(printable);


    }

    /**
     * Method: evaluatePrintExpression - method that evaluates expression to be printed by calling
     *         upon previous methods
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @return result - Simplified value to be printed
     */
    private String evaluatePrintExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        if (curr >= tokens.size()) {
            throw new IllegalArgumentException("Print statement missing an expression.");
        }

        Tokenizer.Token token = tokens.get(curr);
        String result;

        if (token.type == Tokenizer.Type.STRING || token.type == Tokenizer.Type.VAR_NAME && globalVariables.get(token.lexeme).get("type").equals("string")) {
            result = evaluateStrExpression(globalVariables);
        } else if (token.type == Tokenizer.Type.INT || (token.type == Tokenizer.Type.VAR_NAME && globalVariables.get(token.lexeme).get("type").equals("int"))) {
            int oldCur=curr;
            try {
                result = (evaluateComparisonExpression(globalVariables)) ? "True": "False";
            } catch (IllegalArgumentException e) {
                // If comparison fails, evaluate as a numeric expression
                curr = oldCur;  // Reset curr to re-evaluate this token as a numeric expression
                result = Integer.toString(evaluateNumExpression(globalVariables));
            }
        } else if (token.type == Tokenizer.Type.BOOLEAN || token.type == Tokenizer.Type.BOOL_OPERATOR || (token.type == Tokenizer.Type.VAR_NAME && globalVariables.get(token.lexeme).get("type").equals("bool"))) {
            result = (evaluateBoolExpression(globalVariables)) ? "True": "False";
        }else if(token.type == Tokenizer.Type.INPUT) {
            result = evaluateInputExpression(globalVariables);
        }else {
            throw new IllegalArgumentException("Unsupported expression type for print: " + token.type);
        }

        if (curr < tokens.size() && tokens.get(curr).type == Tokenizer.Type.PAREN_CLOSE) {
            curr++;
        } /*else {
            throw new IllegalArgumentException("Expected closing parenthesis after print expression");
        }*/

        return result;
    }

    /**
     * Method: executeConditionalExpression - method that handles execution of conditional blocks and
     *         loops. This is done by evaluating the conditional and running the block as needed. For
     *         loops the method calls itself as many times as the loop runs.
     * @param globalVariables - hashmap of variables, their types and values, that is maintained
     *                        over the entire program
     * @param conditionalBlockList - ArrayList where each position is an entire block corresponding
     *                             to a conditional or a loop
     * @param conditionalStmtList - ArrayList where each position is a 'level' of nesting for conditionals
     *                            for example the outermost statement would be at index 0 while each inner
     *                            condition would be at the next index every time it is nested.
     * @param loop - boolean flag denoting whether it is currently running a loop or not
     * @return globalVariables after the necessary values have been updated
     * @throws ParseException - exception to be caught in parser.
     */
    public HashMap<String, HashMap<String, Object>> executeConditionalExpression(
            HashMap<String, HashMap<String, Object>> globalVariables,
            ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalBlockList,
            ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalStmtList,
            boolean loop) throws ParseException {

        boolean result;
        int index = -1;
        ranChain=false; //No statement in the conditional chain has been run
        ArrayList<List<Tokenizer.Token>> conditions = conditionalStmtList.get(0);
        ArrayList<List<Tokenizer.Token>> blocks = conditionalBlockList.get(0);
        for (List<Tokenizer.Token> condition : conditions){
            //loops through each condition checking if it is true or 'else'
            if (condition.getFirst().type==Tokenizer.Type.ELSE){
                index = conditions.lastIndexOf(condition);
                ranChain=true;
                blocks = conditionalBlockList.get(index); //retrieves appropriate block
                break;
            }
            curr=0;
            tokens=condition;
            result = evaluateBoolExpression(globalVariables);
            if (result){
                index = conditions.lastIndexOf(condition);
                ranChain=true;
                blocks = conditionalBlockList.get(index);
                break;
            }
        }
        if (ranChain^loop) conditionalStmtList.remove(0);
        //removes condition after it has been run or if a loop is done
        if (index>-1){
            //if it was run
            Grammar2 nested = new Grammar2(); //new Grammar2 object to parse the block
            nested.addVariables(globalVariables);
            for (int i = 1; i<blocks.size()-1;i++) {
                //Adds and parses tokens
                curr = 0;
                tokens = blocks.get(i);
                nested.addTokens(tokens);
                //for (Tokenizer.Token token : tokens) {
                    //System.out.println(token);
                //}
                nested.parse();
            }
            //System.out.println(nested.globalVariables);
            if (loop) executeConditionalExpression(nested.globalVariables, conditionalBlockList,conditionalStmtList,loop);
            // Recursively executes while loops
            globalVariables.putAll(nested.globalVariables);
            // Updates variables
            if (ranChain^loop&&!conditionalBlockList.isEmpty()) conditionalBlockList.remove(0);
            // Removes block from list after it has been used.

        }

        return globalVariables;
    }

    /**
     * Method: getRanChain - returns whether a conditional chain has been run
     * @return ranChain
     */
    public boolean getRanChain(){
        return ranChain;
    }

}