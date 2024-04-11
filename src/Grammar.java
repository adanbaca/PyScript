import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {

    private Stack<String> blockStack = new Stack<String>();
    public HashMap<String, HashMap<String, Object>> globalVariables= new HashMap<String, HashMap<String, Object>>();

    public CommandReturn parseCommand(String cmd) {
        String[] cmdParts = cmd.split("\\s+");
        if (cmdParts.length == 0) {
            return new CommandReturn(ReturnType.EMPTY_COMMAND, "");
        }
        String firstExpr = cmdParts[0];

        return switch (firstExpr) {
            case "let" -> validateLet(cmdParts);
            default -> new CommandReturn(ReturnType.PARSE_ERROR, "unable to read command");
        };
    }

    /**
     * validates a let statement and assigns the variable
     * @param cmdParts is a let statement split by whitespace
     *                 Allowed expressions:
     *                 let x = <int>
     *                 let x = <int> + <int>
     *                 let x = <string>
     *                 let x = <bool>
     *                 let x = not <bool>
     *                 let x = <bool> (and/or) <bool>
     *                 let x = <int var> + <int>
     *                 let x = <int var> + <int var>
     *                 let x = <bool var> + <bool>
     *                 let x = <bool var> + <bool var>
     * @return
     */
    // take everything to right of "=" and evaluate it
    // store it into globalVariables
    //  ex:
    //  let x = 5
    //  globalVariables = {"x": {"type": "int", "val": 5}}
    private CommandReturn validateLet(String[] cmdParts) {
        if (cmdParts.length < 4) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "invalid let statement");
        }

        String variableName = cmdParts[1];
        if (variableName.equals("true") || variableName.equals("false")) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "variable name cannot be 'true' or 'false'");
        }
        String equalSign = cmdParts[2];

        if (!equalSign.equals("=")) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "missing equal sign in let statement");
        }

        String expression = String.join(" ", Arrays.copyOfRange(cmdParts, 3, cmdParts.length));

        // Define regex patterns for variable name and expression
        Pattern variableNamePattern = Pattern.compile("^[a-zA-Z]\\w*$");
        Pattern intExpressionPattern = Pattern.compile("^(?:\\(\\s*(?:\\d+|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\d+|[a-zA-Z]\\w*))*\\s*\\)|\\d+|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\(\\s*(?:\\d+|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\d+|[a-zA-Z]\\w*))*\\s*\\)|\\d+|[a-zA-Z]\\w*))*$");
        Pattern stringExpressionPattern = Pattern.compile("^\"[^\"]*\"$");
        Pattern boolExpressionPattern = Pattern.compile("^(?:true|false|[a-zA-Z]\\w*|not\\s+(?:true|false|[a-zA-Z]\\w*))(?:\\s+(?:and|or)\\s+(?:true|false|[a-zA-Z]\\w*|not\\s+(?:true|false|[a-zA-Z]\\w*)))*$");
        Matcher variableNameMatcher = variableNamePattern.matcher(variableName);

        if (!variableNameMatcher.matches()) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "invalid variable name");
        }

        Object result;
        if (boolExpressionPattern.matcher(expression).matches()) {
            result = evaluateBoolExpression(expression);
            if (result instanceof Boolean) {
                HashMap<String, Object> variableInfo = new HashMap<>();
                variableInfo.put("type", "bool");
                variableInfo.put("val", result);
                globalVariables.put(variableName, variableInfo);
                return new CommandReturn(ReturnType.SUCCESS, "variable assigned successfully");
            } else {
                return new CommandReturn(ReturnType.PARSE_ERROR, "expression must evaluate to a boolean");
            }

        } else if (intExpressionPattern.matcher(expression).matches()) {
            result = evaluateExpression(expression);
            if (result instanceof Integer) {
                HashMap<String, Object> variableInfo = new HashMap<>();
                variableInfo.put("type", "int");
                variableInfo.put("val", result);
                globalVariables.put(variableName, variableInfo);
                return new CommandReturn(ReturnType.SUCCESS, "variable assigned successfully");
            } else {
                return new CommandReturn(ReturnType.PARSE_ERROR, "expression must evaluate to an integer");
            }

        } else if (stringExpressionPattern.matcher(expression).matches()) {
            result = expression.substring(1, expression.length() - 1);
            HashMap<String, Object> variableInfo = new HashMap<>();
            variableInfo.put("type", "string");
            variableInfo.put("val", result);
            globalVariables.put(variableName, variableInfo);
            return new CommandReturn(ReturnType.SUCCESS, "variable assigned successfully");

        } else {
            return new CommandReturn(ReturnType.PARSE_ERROR, "invalid expression");
        }
    }

    private Object evaluateExpression(String expression) {
        // Remove any whitespace from the expression
        expression = expression.trim();

        // Check if the expression is a single variable
        if (globalVariables.containsKey(expression)) {
            HashMap<String, Object> variableInfo = globalVariables.get(expression);
            if (variableInfo.get("type").equals("int")) {
                return variableInfo.get("val");
            } else {
                throw new IllegalArgumentException("Variable '" + expression + "' is not of type integer");
            }
        }

        // Create a stack for operands and a stack for operators
        Stack<Integer> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();

        // Iterate through each character in the expression
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '(') {
                // If the character is an opening parenthesis, push it to the operators stack
                operators.push(ch);
            } else if (Character.isDigit(ch)) {
                // If the character is a digit, parse the entire number and push it to the operands stack
                int num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + (expression.charAt(i) - '0');
                    i++;
                }
                i--;
                operands.push(num);
            } else if (Character.isLetter(ch)) {
                // If the character is a letter, parse the entire variable name
                StringBuilder variableName = new StringBuilder();
                while (i < expression.length() && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) {
                    variableName.append(expression.charAt(i));
                    i++;
                }
                i--;
                String varName = variableName.toString();
                // Check if the variable exists in globalVariables
                if (globalVariables.containsKey(varName)) {
                    HashMap<String, Object> variableInfo = globalVariables.get(varName);
                    if (variableInfo.get("type").equals("int")) {
                        operands.push((Integer) variableInfo.get("val"));
                    } else {
                        throw new IllegalArgumentException("Variable '" + varName + "' is not of type integer");
                    }
                } else {
                    throw new IllegalArgumentException("Variable '" + varName + "' is not defined");
                }
            } else if (ch == ')') {
                // If the character is a closing parenthesis, evaluate the expression within the parentheses
                while (!operators.isEmpty() && operators.peek() != '(') {
                    evalu