import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Main — IDE entry point for NovaLang.
 *
 * This class is intended for use inside BlueJ (or any Java IDE).
 * It reads Nova source code from code.txt in the project directory,
 * runs it, and prints output to the IDE terminal.
 *
 * For the full command-line interface, see NovaCLI.java.
 *
 * Usage in BlueJ:
 *   Right-click Main → void main(String[] args) → OK
 */
public class Main {

    // Path to the source file — relative to the project directory
    private static final String SOURCE_FILE = "code.txt";

    public static void main(String[] args) {
        NovaCore.printBannerIfNew();

        String code = NovaCore.readFile(SOURCE_FILE);
        if (code == null) {
            System.err.println(
                    "Could not find '" + SOURCE_FILE + "'.\n" +
                            "Create " + SOURCE_FILE + " in the same folder as Main.java and write your Nova code in it."
            );
            return;
        }

        try {
            NovaCore.run(code);

        } catch (NovaException e) {
            System.err.println(NovaCore.formatError(e, code));
        } catch (StackOverflowError e) {
            System.err.println("RuntimeError: Stack overflow — possible infinite recursion");
        } catch (OutOfMemoryError e) {
            System.err.println("RuntimeError: Out of memory — a value may be too large to compute");
        } catch (Error e) {
            System.err.println("RuntimeError: " + e.getMessage());
        }
    }
}