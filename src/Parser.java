import java.util.Scanner;
import java.util.HashMap;


public class Parser {

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String cmd = scanner.nextLine();
            CommandReturn retVal = grammar.parseCommand(cmd);
            System.out.println(grammar.globalVariables);
            if (retVal.returnType == ReturnType.PARSE_ERROR || retVal.returnType == ReturnType.COMPILE_ERROR) {
                System.out.println(retVal.msg);
            }
        }
    }
}
