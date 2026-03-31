import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * NovaCore — shared constants and utilities used by both entry points:
 *
 *   Main.java    — IDE entry point (BlueJ, reads code.txt)
 *   NovaCLI.java — Command-line entry point (nova run / compile / ...)
 *
 * Version naming follows the scientist series:
 *   0.1  Archimedes     0.8  Heisenberg      0.15  Oppenheimer
 *   0.2  Bohr           0.9  Isaac Asimov    0.16  Pascal
 *   0.3  Curie          0.10 Joule           0.17  Quine
 *   0.4  Darwin         0.11 Kepler          0.18  Rutherford
 *   0.5  Einstein       0.12 Lovelace        0.19  Schrödinger
 *   0.6  Feynman        0.13 Mendel          0.20  Turing
 *   0.7  Gödel          0.14 Newton
 */
public class NovaCore {

    public static final String VERSION  = "0.1";
    public static final String CODENAME = "Archimedes";
    public static final String FULL_VERSION = "NovaLang " + VERSION + " - " + CODENAME;

    // ── Pipeline ──────────────────────────────────────────────────────────────

    /**
     * Lex, parse, and execute a source string.
     * Throws NovaException on language errors; lets JVM errors propagate.
     */
    public static void run(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        List<Node> ast = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.execute(ast);
    }

    /**
     * Lex and parse only — does not execute.
     * Returns the AST on success, throws NovaException on syntax errors.
     */
    public static List<Node> parse(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        return new Parser(tokens).parse();
    }

    // ── File I/O ──────────────────────────────────────────────────────────────

    /**
     * Read a file and return its contents.
     * Prints an error message and returns null if the file cannot be read.
     * Callers should check for null before proceeding.
     */
    public static String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (NoSuchFileException e) {
            System.err.println("Error: File not found: '" + path + "'");
            return null;
        } catch (IOException e) {
            System.err.println("Error: Could not read '" + path + "': " + e.getMessage());
            return null;
        }
    }

    // ── Error formatting ──────────────────────────────────────────────────────

    /**
     * Format a NovaException into a user-friendly error message.
     * Includes the offending source line when available.
     */
    public static String formatError(NovaException e, String code) {
        String formatted = e.format();
        int lineNum = e.getLine();
        if (lineNum > 0) {
            String offending = getLine(code, lineNum);
            if (offending != null && !offending.isBlank())
                formatted += "\n    --> " + offending.strip();
        }
        return formatted;
    }

    /**
     * Extract a single line (1-indexed) from a source string.
     * Returns null if the line number is out of range.
     */
    public static String getLine(String code, int lineNum) {
        String[] lines = code.split("\n", -1);
        if (lineNum >= 1 && lineNum <= lines.length)
            return lines[lineNum - 1];
        return null;
    }
}
