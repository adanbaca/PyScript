import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class TestGrammar2 {

    public static void main(String[] args) throws ParseException{
        Tokenizer tokenizer = new Tokenizer();
        String filePath = "./src/Program2.txt"; // File path
        List<Tokenizer.Token> tokens;
        Grammar2 grammar = new Grammar2();
        // Using try-with-resources to automatically close the BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(grammar.curr);
                // Process each line
                tokens = tokenizer.tokenize(line);
                grammar.addTokens(tokens);
                for (Tokenizer.Token token : tokens) {
                    //System.out.println(token);
                }
                grammar.parse();
               //System.out.println(grammar.globalVariables);

            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

}

/**
 * String filePath = "example.txt"; // File path
 *
 *         // Using try-with-resources to automatically close the BufferedReader
 *         try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
 *             String line;
 *             while ((line = reader.readLine()) != null) {
 *                 // Process each line
 *                 if (line.contains("error")) {
 *                     // Do something with lines that contain "error"
 *                     System.out.println("Error found: " + line);
 *                 }
 *             }
 *         } catch (IOException e) {
 *             System.err.println("Error reading the file: " + e.getMessage());
 *         }
 */


/**
 * Tokenizer tokenizer = new Tokenizer();
 *         String input = "let x = \"a\" + b";
 *         ArrayList<Token> tokens = tokenizer.tokenize(input);
 *         Grammar2 grammar = new Grammar2(tokens);
 *
 *         for (Token token : tokens) {
 *             System.out.println(token);
 *         }
 *         grammar.parse();
 */

/*
private static String readFile(String file) {
        //Reads text file in and returns string with all the text
        StringBuilder text = new StringBuilder();
        try (FileReader fileReader = new FileReader(file)) {
            int character;
            while ((character = fileReader.read()) != -1) {
                text.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
 */