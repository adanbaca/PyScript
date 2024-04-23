import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * class: TestGrammar2
 * The class we use to execute the grammar and parse.
 * In order to use, just adjust the .txt filename in the
 * filePath variable in main, and run.
 */
public class TestGrammar2 {

    public static void main(String[] args) throws ParseException{
        Tokenizer tokenizer = new Tokenizer();
        String filePath = "./src/Program2.txt"; // File path
        ArrayList<Tokenizer.Token> tokens;
        Grammar2 grammar = new Grammar2();
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        // check the brackets, error check before running
        tokenizer.checkBrackets(lines);

        // begin parsing process once error checking has passed
        for (String line: lines){
            tokens = tokenizer.tokenize(line);
            grammar.addTokens(tokens);
            grammar.parse();
        }
    }
}