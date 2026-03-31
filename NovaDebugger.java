import java.util.*;
import java.io.*;

/**
 * NovaDebugger — an interactive step-through debugger for NovaLang.
 *
 * Wraps the Interpreter and pauses before executing each statement,
 * showing the current source line and waiting for a debugger command.
 *
 * Commands:
 *   s / step      Execute the current line and pause at the next
 *   c / continue  Run until the next breakpoint or end of program
 *   n / next      Same as step (alias)
 *   b / break N   Set a breakpoint at line N
 *   rb N          Remove breakpoint at line N
 *   bl            List all breakpoints
 *   p NAME        Print the value of variable NAME
 *   vars          Print all variables in current scope
 *   src           Show source code with current line highlighted
 *   src N         Show N lines of context around current line
 *   stack         Show the call stack
 *   q / quit      Stop the program
 *   h / help      Show this command list
 */
public class NovaDebugger {

    private final String   source;
    private final String[] sourceLines;
    private final List<Node> ast;

    private final Set<Integer> breakpoints = new TreeSet<>();
    private boolean stepping = true;
    private final Scanner console = new Scanner(System.in);

    // Shadow interpreter that hooks into execution
    private final DebugInterpreter interpreter;

    public NovaDebugger(String source, List<Node> ast) {
        this.source = source;
        this.sourceLines = source.split("\n", -1);
        this.ast = ast;
        this.interpreter = new DebugInterpreter(this);
    }

    public void run() {
        printBanner();
        System.out.println("Type 'h' for help. Press Enter to step through the program.\n");
        try {
            interpreter.execute(ast);
            System.out.println("\n[Program finished]");
        } catch (NovaException e) {
            System.err.println("\n" + e.format());
        } catch (DebugQuitException e) {
            System.out.println("\n[Debugger quit]");
        }
    }

    // Called by DebugInterpreter before each statement
    void onStatement(int line) {
        boolean atBreakpoint = breakpoints.contains(line);

        if (!stepping && !atBreakpoint) return;

        if (atBreakpoint) {
            System.out.println("\n*** Breakpoint at line " + line + " ***");
        }

        showCurrentLine(line, 2);
        promptLoop(line);
    }

    private void promptLoop(int currentLine) {
        while (true) {
            System.out.print("nova-debug> ");
            String input = console.hasNextLine() ? console.nextLine().trim() : "q";
            if (input.isEmpty()) input = "s";  // Enter = step

            String[] parts = input.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : "";

            switch (cmd) {

                case "s", "step", "n", "next" -> {
                    stepping = true;
                    return;
                }

                case "c", "continue" -> {
                    stepping = false;
                    return;
                }

                case "b", "break" -> {
                    try {
                        int n = Integer.parseInt(arg.trim());
                        breakpoints.add(n);
                        System.out.println("Breakpoint set at line " + n);
                    } catch (NumberFormatException e) {
                        System.out.println("Usage: b <line number>");
                    }
                }

                case "rb" -> {
                    try {
                        int n = Integer.parseInt(arg.trim());
                        if (breakpoints.remove(n))
                            System.out.println("Breakpoint removed from line " + n);
                        else
                            System.out.println("No breakpoint at line " + n);
                    } catch (NumberFormatException e) {
                        System.out.println("Usage: rb <line number>");
                    }
                }

                case "bl" -> {
                    if (breakpoints.isEmpty())
                        System.out.println("No breakpoints set.");
                    else
                        System.out.println("Breakpoints: " + breakpoints);
                }

                case "p", "print" -> {
                    if (arg.isEmpty()) {
                        System.out.println("Usage: p <variable name>");
                    } else {
                        try {
                            Value v = interpreter.debugGetVar(arg.trim());
                            System.out.println(arg.trim() + " = " + v);
                        } catch (Exception e) {
                            System.out.println("Variable '" + arg.trim() + "' not found in scope");
                        }
                    }
                }

                case "vars" -> {
                    Map<String, Value> vars = interpreter.debugGetAllVars();
                    if (vars.isEmpty()) {
                        System.out.println("No variables in current scope.");
                    } else {
                        System.out.println("Variables in scope:");
                        vars.forEach((k, v) ->
                            System.out.println("  " + k + " = " + v + "  (" + typeName(v) + ")"));
                    }
                }

                case "src" -> {
                    int ctx = 3;
                    if (!arg.isEmpty()) {
                        try { ctx = Integer.parseInt(arg.trim()); }
                        catch (NumberFormatException ignored) {}
                    }
                    showCurrentLine(currentLine, ctx);
                }

                case "stack" -> {
                    List<String> stack = interpreter.debugGetCallStack();
                    if (stack.isEmpty()) {
                        System.out.println("At top level (no function calls active)");
                    } else {
                        System.out.println("Call stack:");
                        for (int i = stack.size() - 1; i >= 0; i--)
                            System.out.println("  " + (stack.size() - i) + ". " + stack.get(i));
                    }
                }

                case "q", "quit", "exit" -> throw new DebugQuitException();

                case "h", "help" -> printHelp();

                default -> System.out.println("Unknown command '" + cmd + "'. Type 'h' for help.");
            }
        }
    }

    private void showCurrentLine(int line, int context) {
        System.out.println();
        int start = Math.max(1, line - context);
        int end   = Math.min(sourceLines.length, line + context);

        for (int i = start; i <= end; i++) {
            String prefix = (i == line) ? "→ " : "  ";
            String lineStr = String.format("%s%3d │ %s", prefix, i,
                             i <= sourceLines.length ? sourceLines[i - 1] : "");
            System.out.println(lineStr);
        }
        System.out.println();
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║   NovaLang Debugger  v1.0    ║");
        System.out.println("╚══════════════════════════════╝");
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              s / step / Enter   Execute current line and pause at next
              c / continue       Run until next breakpoint or program end
              b N                Set breakpoint at line N
              rb N               Remove breakpoint at line N
              bl                 List all breakpoints
              p NAME             Print value of variable NAME
              vars               Print all variables in current scope
              src                Show source around current line
              src N              Show N lines of context
              stack              Show function call stack
              q / quit           Stop the program
              h / help           Show this help
            """);
    }

    private String typeName(Value v) {
        return switch (v) {
            case IntValue        i -> "int";
            case LongValue       l -> "long";
            case BigIntValue     b -> "bigint";
            case DoubleValue     d -> "double";
            case BigDecimalValue b -> "bigdecimal";
            case StringValue     s -> "string";
            case CharValue       c -> "char";
            case BooleanValue    b -> "boolean";
            case NullValue       n -> "null";
            case ArrayValue      a -> "array";
            case MapValue        m -> "map";
            case FunctionValue   f -> "function";
            default                -> "unknown";
        };
    }

    // Sentinel exception to exit the debugger cleanly
    static class DebugQuitException extends RuntimeException {}
}
