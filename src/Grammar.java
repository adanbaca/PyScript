import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {

    private Stack<String> blockStack = new Stack<String>();
    private HashMap<String, HashMap<String, Object>> globalVariables= new HashMap<String, HashMap<String, Object>>();

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
     * @param cmdParts ex: ["let", "x", "=", "5"]
     *                 ex: ["let", "x", "=", "5", "+", "6"]
     *                 ex: ["let", "x", "=", "\"5\"", "+", "\"6\""]
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
        String equalSign = cmdParts[2];
        if (!equalSign.equals("=")) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "missing equal sign in let statement");
        }
        String expression = String.join(" ", Arrays.copyOfRange(cmdParts, 3, cmdParts.length));

        Pattern variableNamePattern = Pattern.compile("^[a-zA-Z]\\w*$");
        Pattern expressionPattern = Pattern.compile("^(?:\\(\\s*(?:\\d+|\"[^\"]*\"|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\d+|\"[^\"]*\"|[a-zA-Z]\\w*))*\\s*\\)|\\d+|\"[^\"]*\"|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\(\\s*(?:\\d+|\"[^\"]*\"|[a-zA-Z]\\w*)(?:\\s*[+\\-*/%]\\s*(?:\\d+|\"[^\"]*\"|[a-zA-Z]\\w*))*\\s*\\)|\\d+|\"[^\"]*\"|[a-zA-Z]\\w*))*$");

        Matcher variableNameMatcher = variableNamePattern.matcher(variableName);
        Matcher expressionMatcher = expressionPattern.matcher(expression);

        if (!variableNameMatcher.matches()) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "invalid variable name");
        }

        if (!expressionMatcher.matches()) {
            return new CommandReturn(ReturnType.PARSE_ERROR, "invalid expression");
        }

        Object result = evaluateExpression(expression);
        String variableType = result.getClass().getSimpleName();
        HashMap<String, Object> variableInfo = new HashMap<>();
        variableInfo.put("type", variableType);
        variableInfo.put("val", result);
        globalVariables.put(variableName, variableInfo);
        System.out.println(globalVariables.get("x"));
        System.out.println(globalVariables.get("x").get("type"));
        System.out.println(globalVariables.get("x").get("val"));

        return new CommandReturn(ReturnType.SUCCESS, "variable assigned successfully");
    }

    private Object evaluateExpression(String expression) {
        // Remove any whitespace from the expression
        expression = expression.replaceAll("\\s+", "");

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
            } else if (ch == ')') {
                // If the character is a closing parenthesis, evaluate the expression within the parentheses
                while (!operators.isEmpty() && operators.peek() != '(') {
                    int num2 = operands.pop();
                    int num1 = operands.pop();
                    char op = operators.pop();
                    operands.push(applyOperator(num1, num2, op));
                }
                // Pop the opening parenthesis from the operators stack
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop();
                }
            } else {
                // If the character is an operator, evaluate the expression based on operator precedence
                while (!operators.isEmpty() && precedence(ch) <= precedence(operators.peek())) {
                    int num2 = operands.pop();
                    int num1 = operands.pop();
                    char op = operators.pop();
                    operands.push(applyOperator(num1, num2, op));
                }
                operators.push(ch);
            }
        }

        // Evaluate any remaining operators and operands
        while (!operators.isEmpty()) {
            int num2 = operands.pop();
            int num1 = operands.pop();
            char op = operators.pop();
            operands.push(applyOperator(num1, num2, op));
        }

        // The final result is the top element in the operands stack
        return operands.pop();
    }

    private int precedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/', '%' -> 2;
            default -> 0;
        };
    }

    private int applyOperator(int num1, int num2, char operator) {
        return switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> num1 / num2;
            case '%' -> num1 % num2;
            default -> 0;
        };
    }

}
