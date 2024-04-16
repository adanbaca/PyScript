import java.util.Scanner;

public class Parser2 {

    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print(">> ");
        String input = scan.nextLine();
        while (!input.equals("exit"){
            String[] split  = input.split(" ");
            // process command here

            System.out.print(">>");
            input = scan.nextLine();
        }

    }

}
