import java.util.HashMap;
import java.util.List;

public class Execute {
    private int curr = 0;
    private List<Tokenizer.Token> tokens;
    public Execute() {}

    public HashMap<String, HashMap<String, Object>> executeBoolExpression(
            List<Tokenizer.Token> tokens,
            HashMap<String, HashMap<String, Object>> globalVariables) {
        this.tokens = tokens;
        String varName = tokens.get(1).lexeme;
        curr = 3;

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
        } else if (token.type == Tokenizer.Type.VAR_NAME) {
            curr++;
            String varName = token.lexeme;
            if (!globalVariables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not found: " + varName);
            }
            Object varValue = globalVariables.get(varName).get("val");
            if (!(varValue instanceof Boolean)) {
                throw new IllegalArgumentException("Variable is not of boolean type: " + varName);
            }
            return (boolean) varValue;
        } else if (token.type == Tokenizer.Type.BOOL_OPERATOR && token.lexeme.equals("not")) {
            curr++;
            return !evaluateBoolTerm(globalVariables);
        } else if (token.type == Tokenizer.Type.PAREN_OPEN) {
            curr++;
            boolean result = evaluateBoolExpression(globalVariables);
            if (curr >= tokens.size() || tokens.get(curr).type != Tokenizer.Type.PAREN_CLOSE) {
                throw new IllegalArgumentException("Invalid boolean expression: expected closing parenthesis");
            }
            curr++;
            return result;
        } else {
            throw new IllegalArgumentException("Invalid boolean term: " + token.lexeme);
        }
    }
}