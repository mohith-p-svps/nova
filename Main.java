import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {

    static final String VERSION = "0.1 - Archimedes";

    public static void main(String[] args) {

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {

            case "run":
                if (args.length < 2) {
                    System.err.println("Usage: nova run <file.nova>");
                    System.exit(1);
                }
                runFile(args[1]);
                break;

            case "compile":
                if (args.length < 2) {
                    System.err.println("Usage: nova compile <file.nova>");
                    System.exit(1);
                }
                compileFile(args[1]);
                break;

            case "check":
                if (args.length < 2) {
                    System.err.println("Usage: nova check <file.nova>");
                    System.exit(1);
                }
                checkFile(args[1]);
                break;

            case "version":
            case "--version":
            case "-v":
                System.out.println("NovaLang " + VERSION);
                break;

            case "help":
            case "--help":
            case "-h":
                printHelp();
                break;

            default:
                // Allow: nova code.txt  (shorthand for nova run code.txt)
                if (args[0].endsWith(".nova") || args[0].endsWith(".txt")) {
                    runFile(args[0]);
                } else {
                    System.err.println("Unknown command: '" + args[0] + "'");
                    System.err.println("Run 'nova help' for usage.");
                    System.exit(1);
                }
        }
    }

    // ── run ──────────────────────────────────────────────────────────────────
    private static void runFile(String path) {
        String code = readFile(path);
        if (code == null) return;

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            List<Node> ast = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

        } catch (NovaException e) {
            printError(e, code);
            System.exit(1);
        } catch (StackOverflowError e) {
            System.err.println("RuntimeError: Stack overflow — possible infinite recursion");
            System.exit(1);
        } catch (OutOfMemoryError e) {
            System.err.println("RuntimeError: Out of memory — a value may be too large to compute");
            System.exit(1);
        } catch (Error e) {
            System.err.println("RuntimeError: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── compile ───────────────────────────────────────────────────────────────
    // Lex and parse only — reports syntax errors or confirms clean.
    private static void compileFile(String path) {
        String code = readFile(path);
        if (code == null) return;

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            List<Node> ast = parser.parse();

            int lines = code.split("\n", -1).length;
            int nodes = ast.size();

            System.out.println("Compiled: " + path);
            System.out.println("  Lines:      " + lines);
            System.out.println("  Statements: " + nodes);
            System.out.println("  Status:     OK — no syntax errors");

        } catch (NovaException e) {
            printError(e, code);
            System.exit(1);
        }
    }

    // ── check ─────────────────────────────────────────────────────────────────
    private static void checkFile(String path) {
        compileFile(path);
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private static String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (NoSuchFileException e) {
            System.err.println("Error: File not found: '" + path + "'");
            System.exit(1);
            return null;
        } catch (IOException e) {
            System.err.println("Error: Could not read '" + path + "'\n" + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    private static void printError(NovaException e, String code) {
        String formatted = e.format();
        int lineNum = e.getLine();
        if (lineNum > 0) {
            String offending = getLine(code, lineNum);
            if (offending != null && !offending.isBlank())
                formatted += "\n    --> " + offending.strip();
        }
        System.err.println(formatted);
    }

    private static String getLine(String code, int lineNum) {
        String[] lines = code.split("\n", -1);
        if (lineNum >= 1 && lineNum <= lines.length)
            return lines[lineNum - 1];
        return null;
    }

    private static void printHelp() {
        System.out.println("NovaLang " + VERSION);
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  nova run <file>       Run a Nova program");
        System.out.println("  nova compile <file>   Check syntax and report stats");
        System.out.println("  nova check <file>     Alias for compile");
        System.out.println("  nova <file>           Shorthand for nova run <file>");
        System.out.println("  nova version          Print version number");
        System.out.println("  nova help             Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  nova run code.nova");
        System.out.println("  nova compile myprogram.nova");
        System.out.println("  nova code.txt");
    }
}