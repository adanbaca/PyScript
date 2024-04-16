import java.util.Scanner;


public class Parser {

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String cmd = scanner.nextLine();
            if (cmd.equals("exit")) break;
            CommandReturn retVal = parseCommand(grammar, cmd);
            System.out.println(grammar.globalVariables);
            if (retVal.returnType == ReturnType.PARSE_ERROR || retVal.returnType == ReturnType.COMPILE_ERROR) {
                System.out.println(retVal.msg);
            }
        }

    }
    private static CommandReturn parseCommand(Grammar grammar, String cmd) {
        String[] cmdParts = cmd.split("\\s+");
        if (cmdParts.length == 0) {
            return new CommandReturn(ReturnType.EMPTY_COMMAND, "");
        }
        String firstExpr = cmdParts[0];

        return switch (firstExpr) {
            case "let" -> grammar.validateLet(cmdParts);
            default -> new CommandReturn(ReturnType.PARSE_ERROR, "unable to read command");
        };
    }
}
