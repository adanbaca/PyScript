import java.text.ParseException;
import java.util.*;


public class Execute {
    private int curr = 0;
    private List<Tokenizer.Token> tokens;
    public Execute() {}
    private boolean ranChain;


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

    private String evaluateStrExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        String result = evaluateStrTerm(globalVariables);

        while (curr < tokens.size() && tokens.get(curr).type == Tokenizer.Type.STR_OPERATOR) {
            curr++;
            String term = evaluateStrTerm(globalVariables);
            result += term;
        }

        return result;
    }

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
    private String evaluateInputExpression(HashMap<String, HashMap<String, Object>> globalVariables) {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    public void executePrintExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        curr = 2;

        String printable = evaluatePrintExpression(globalVariables);
        if (tokens.getFirst().type==Tokenizer.Type.PUTS) System.out.print(printable);
        else System.out.println(printable);


    }
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
    public HashMap<String, HashMap<String, Object>> executeConditionalExpression(
            HashMap<String, HashMap<String, Object>> globalVariables,
            ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalBlockList,
            ArrayList<ArrayList<List<Tokenizer.Token>>> conditionalStmtList,
            boolean loop) throws ParseException {

        boolean result;
        int index = -1;
        ranChain=false;
        ArrayList<List<Tokenizer.Token>> conditions = conditionalStmtList.get(0);
        ArrayList<List<Tokenizer.Token>> blocks = conditionalBlockList.get(0);
        for (List<Tokenizer.Token> condition : conditions){
            if (condition.getFirst().type==Tokenizer.Type.ELSE){
                index = conditions.lastIndexOf(condition);
                ranChain=true;
                blocks = conditionalBlockList.get(index);
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
        if (index>-1){
            Grammar2 nested = new Grammar2();
            nested.addVariables(globalVariables);
            for (int i = 1; i<blocks.size()-1;i++) {
                curr = 0;
                tokens = blocks.get(i);
                nested.addTokens(tokens);
                for (Tokenizer.Token token : tokens) {
                    //System.out.println(token);
                }
                nested.parse();
            }
            //System.out.println(nested.globalVariables);
            if (loop) executeConditionalExpression(nested.globalVariables, conditionalBlockList,conditionalStmtList,loop);
            globalVariables.putAll(nested.globalVariables);
            if (ranChain^loop&&!conditionalBlockList.isEmpty()) conditionalBlockList.remove(0);

        }

        return globalVariables;
    }

    public boolean getRanChain(){
        return ranChain;
    }

}