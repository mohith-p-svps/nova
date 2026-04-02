import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * NovaCLI — command-line entry point for NovaLang.
 *
 * This is the class that nova.jar points to via MANIFEST.MF.
 * It handles all CLI commands: run, compile, check, format, debug,
 * install, remove, list, info, publish, version, help.
 *
 * For the IDE entry point (BlueJ / code.txt), see Main.java.
 *
 * Usage:
 *   nova run <file>            Run a Nova program
 *   nova compile <file>        Check syntax and report stats
 *   nova check <file>          Alias for compile
 *   nova format <file>         Print formatted code to stdout
 *   nova format <file> --write Format and overwrite file in place
 *   nova debug <file>          Interactive step-through debugger
 *   nova install <url>         Install a package from URL or GitHub
 *   nova install <name> <path> Install a local file as a named package
 *   nova remove <name>         Remove an installed package
 *   nova list                  List all installed packages
 *   nova info <name>           Show details about an installed package
 *   nova publish <file>        Instructions for sharing a package
 *   nova version               Print version information
 *   nova help                  Show this help message
 *   nova <file>                Shorthand for nova run <file>
 */
public class NovaCLI {

    public static void main(String[] args) {

        NovaCore.printBannerIfNew();

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {

            case "run" -> {
                requireArg(args, 2, "nova run <file>");
                runFile(args[1]);
            }

            case "compile" -> {
                requireArg(args, 2, "nova compile <file>");
                compileFile(args[1]);
            }

            case "check" -> {
                requireArg(args, 2, "nova check <file>");
                compileFile(args[1]);
            }

            case "format" -> {
                requireArg(args, 2, "nova format <file> [--write]");
                formatFile(args[1], args.length > 2 && args[2].equals("--write"));
            }

            case "debug" -> {
                requireArg(args, 2, "nova debug <file>");
                debugFile(args[1]);
            }

            case "install"        -> NovaPackageManager.install(args);
            case "remove",
                 "uninstall"      -> NovaPackageManager.remove(args);
            case "list"           -> NovaPackageManager.list(args);
            case "info"           -> NovaPackageManager.info(args);
            case "publish"        -> NovaPackageManager.publish(args);

            case "version",
                 "--version",
                 "-v"             -> System.out.println(NovaCore.FULL_VERSION);

            case "help",
                 "--help",
                 "-h"             -> printHelp();

            default -> {
                // Shorthand: nova myfile.nova or nova code.txt
                if (args[0].endsWith(".nova") || args[0].endsWith(".txt")) {
                    runFile(args[0]);
                } else {
                    System.err.println("Unknown command: '" + args[0] + "'");
                    System.err.println("Run 'nova help' for usage.");
                    System.exit(1);
                }
            }
        }
    }

    // ── run ───────────────────────────────────────────────────────────────────

    private static void runFile(String path) {
        String code = readFile(path);

        try {
            NovaCore.run(code);

        } catch (NovaException e) {
            System.err.println(NovaCore.formatError(e, code));
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

    // ── compile / check ───────────────────────────────────────────────────────

    private static void compileFile(String path) {
        String code = readFile(path);

        try {
            List<Node> ast = NovaCore.parse(code);

            int lines = code.split("\n", -1).length;
            int stmts = ast.size();

            System.out.println("Compiled:   " + path);
            System.out.println("  Version:    " + NovaCore.FULL_VERSION);
            System.out.println("  Lines:      " + lines);
            System.out.println("  Statements: " + stmts);
            System.out.println("  Status:     OK — no syntax errors");

        } catch (NovaException e) {
            System.err.println(NovaCore.formatError(e, code));
            System.exit(1);
        }
    }

    // ── format ────────────────────────────────────────────────────────────────

    private static void formatFile(String path, boolean write) {
        String code = readFile(path);

        try {
            String formatted = NovaFormatter.formatSource(code);

            if (write) {
                try {
                    Files.writeString(Path.of(path), formatted);
                    System.out.println("Formatted: " + path);
                } catch (IOException e) {
                    System.err.println("Error: Could not write '" + path + "': " + e.getMessage());
                    System.exit(1);
                }
            } else {
                System.out.print(formatted);
            }

        } catch (NovaException e) {
            System.err.println(NovaCore.formatError(e, code));
            System.exit(1);
        }
    }

    // ── debug ─────────────────────────────────────────────────────────────────

    private static void debugFile(String path) {
        String code = readFile(path);

        try {
            List<Node> ast = NovaCore.parse(code);
            new NovaDebugger(code, ast).run();

        } catch (NovaException e) {
            System.err.println(NovaCore.formatError(e, code));
            System.exit(1);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String readFile(String path) {
        String code = NovaCore.readFile(path);
        if (code == null) System.exit(1);
        return code;
    }

    private static void requireArg(String[] args, int minLength, String usage) {
        if (args.length < minLength) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }
    }

    // ── help ──────────────────────────────────────────────────────────────────

    private static void printHelp() {
        System.out.println(NovaCore.FULL_VERSION);
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  nova <file>                     Shorthand for nova run <file>");
        System.out.println("  nova run <file>                 Run a Nova program");
        System.out.println("  nova compile <file>             Check syntax and report stats");
        System.out.println("  nova check <file>               Alias for compile");
        System.out.println("  nova format <file>              Print formatted code to stdout");
        System.out.println("  nova format <file> --write      Format and overwrite file");
        System.out.println("  nova debug <file>               Interactive step-through debugger");
        System.out.println();
        System.out.println("  nova install <url>              Install package from URL");
        System.out.println("  nova install <user>/<repo>/<f>  Install package from GitHub");
        System.out.println("  nova install <name> <path>      Install local file as package");
        System.out.println("  nova remove <name>              Remove an installed package");
        System.out.println("  nova list                       List all installed packages");
        System.out.println("  nova info <name>                Show package details");
        System.out.println("  nova publish <file>             Instructions for sharing a package");
        System.out.println();
        System.out.println("  nova version                    Print version information");
        System.out.println("  nova help                       Show this message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  nova run myprogram.nova");
        System.out.println("  nova compile myprogram.nova");
        System.out.println("  nova format myprogram.nova --write");
        System.out.println("  nova debug myprogram.nova");
        System.out.println("  nova install alice/novautils/mathext.nova");
        System.out.println("  nova myprogram.nova");
        System.out.println();
        System.out.println("Documentation: https://github.com/mohith-p-svps/nova");
    }
}