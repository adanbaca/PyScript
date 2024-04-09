import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {

    Stack<String> blockStack = new Stack<String>();
    HashMap<String, Object> globalVariables= new HashMap<String, Object>();

    public CommandReturn parseCommand(String cmd) {
        String[] cmdParts = cmd.split("\\s+");
        if (cmdParts.length == 0) {
            return new CommandReturn(ReturnType.ERROR, "empty command");
        }
        String firstExpr = cmdParts[0];

        return switch (firstExpr) {
            case "let" -> validateLet(cmdParts);
            default -> new CommandReturn(ReturnType.ERROR, "unable to read command");
        };
    }

    private CommandReturn validateLet(String[] cmdParts) {
        return new CommandReturn(ReturnType.SUCCESS, "success");
    }

    private Object evaluateExpression(String val1, String val2, String op) {
        // should evaluate val1 <op> val2
        return val1 + val2;
    }

}
