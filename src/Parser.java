import java.util.Scanner;
import java.util.HashMap;


public class Parser {


    public void main(String[] args) {
        Grammar grammar = new Grammar();
        Scanner scanner = new Scanner(System.in);

        String cmd = scanner.nextLine();
        while (true) {
            CommandReturn retVal = grammar.parseCommand(cmd);
            if (retVal.returnType == ReturnType.ERROR) {
                break;
            }
        }
    }
}
