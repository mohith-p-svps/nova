import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    
    static final String VERSION = "0.1 - Archimedes";
    
    public static void main(String[] args) {

        String path = "code.txt";

        String code;
        try {
            code = Files.readString(Path.of(path));
        } catch (NoSuchFileException e) {
            System.err.println("Error: 'code.txt' not found. Make sure it is in the same folder as Main.java.");
            return;
        } catch (IOException e) {
            System.err.println("Error: Could not read 'code.txt'\n" + e.getMessage());
            return;
        }

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            List<Node> ast = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

        } catch (NovaException e) {
            // Format: [line N] ExceptionType: message
            //           --> offending line content
            String formatted = e.format();
            int lineNum = e.getLine();
            if (lineNum > 0) {
                String offending = getLine(code, lineNum);
                if (offending != null && !offending.isBlank())
                    formatted += "\n    --> " + offending.strip();
            }
            System.err.println(formatted);
        } catch (StackOverflowError e) {
            System.err.println("RuntimeError: Stack overflow — possible infinite recursion");
        } catch (OutOfMemoryError e) {
            System.err.println("RuntimeError: Out of memory — a value may be too large to compute");
        } catch (Error e) {
            System.err.println("RuntimeError: " + e.getMessage());
        }
    }

    // Extract the Nth line (1-indexed) from source code
    private static String getLine(String code, int lineNum) {
        String[] lines = code.split("\n", -1);
        if (lineNum >= 1 && lineNum <= lines.length)
            return lines[lineNum - 1];
        return null;
    }
}
