import java.util.*;
import java.io.*;

public class OsModule {

    private static final long START_TIME = System.currentTimeMillis();
    private static final Random RANDOM = new Random();

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // ============================================================
        // INPUT / OUTPUT
        // ============================================================

        // input(prompt) — print prompt and read a line from user
        funcs.put("input", (args, line) -> {
            checkArgs("input", 1, args, line);
            System.out.print(args.get(0).toString());
            try {
                Scanner scanner = new Scanner(System.in);
                return new StringValue(scanner.nextLine());
            } catch (Exception e) {
                throw new NovaRuntimeException("input() failed to read from console", line);
            }
        });

        // clear() — clear the terminal screen
        funcs.put("clear", (args, line) -> {
            checkArgs("clear", 0, args, line);
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    new ProcessBuilder("cmd", "/c", "cls")
                        .inheritIO().start().waitFor();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            } catch (Exception e) {
                throw new NovaRuntimeException("clear() failed", line);
            }
            return new NullValue();
        });

        // ============================================================
        // PROGRAM CONTROL
        // ============================================================

        // exit() — terminate with code 0
        funcs.put("exit", (args, line) -> {
            if (args.size() == 0) {
                System.exit(0);
            } else if (args.size() == 1) {
                System.exit(args.get(0).asInt());
            } else {
                throw new ArgumentException("exit", 0, args.size(), line);
            }
            return new NullValue();
        });

        // sleep(ms) — pause for N milliseconds
        funcs.put("sleep", (args, line) -> {
            checkArgs("sleep", 1, args, line);
            long ms = args.get(0).asLong();
            if (ms < 0)
                throw new TypeError("sleep() duration cannot be negative", line);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new NullValue();
        });

        // ============================================================
        // TIME
        // ============================================================

        // time() — current epoch milliseconds
        funcs.put("time", (args, line) -> {
            checkArgs("time", 0, args, line);
            return new LongValue(System.currentTimeMillis());
        });

        // clock() — milliseconds since program started
        funcs.put("clock", (args, line) -> {
            checkArgs("clock", 0, args, line);
            return new LongValue(System.currentTimeMillis() - START_TIME);
        });

        // ============================================================
        // ENVIRONMENT
        // ============================================================

        // env(key) — read environment variable, NullValue if not set
        funcs.put("env", (args, line) -> {
            checkArgs("env", 1, args, line);
            String key = args.get(0).toString();
            String val = System.getenv(key);
            return val != null ? new StringValue(val) : new NullValue();
        });

        // platform() — OS name
        funcs.put("platform", (args, line) -> {
            checkArgs("platform", 0, args, line);
            return new StringValue(System.getProperty("os.name"));
        });

        // username() — current system username
        funcs.put("username", (args, line) -> {
            checkArgs("username", 0, args, line);
            return new StringValue(System.getProperty("user.name"));
        });

        // homedir() — home directory path
        funcs.put("homedir", (args, line) -> {
            checkArgs("homedir", 0, args, line);
            return new StringValue(System.getProperty("user.home"));
        });

        // workdir() — current working directory
        funcs.put("workdir", (args, line) -> {
            checkArgs("workdir", 0, args, line);
            return new StringValue(System.getProperty("user.dir"));
        });

        // separator() — file path separator (/ or \)
        funcs.put("separator", (args, line) -> {
            checkArgs("separator", 0, args, line);
            return new StringValue(File.separator);
        });

        // ============================================================
        // SYSTEM INFO
        // ============================================================

        // javaVersion() — Java version running Nova
        funcs.put("javaVersion", (args, line) -> {
            checkArgs("javaVersion", 0, args, line);
            return new StringValue(System.getProperty("java.version"));
        });

        // totalMemory() — JVM total memory in bytes
        funcs.put("totalMemory", (args, line) -> {
            checkArgs("totalMemory", 0, args, line);
            return new LongValue(Runtime.getRuntime().totalMemory());
        });

        // freeMemory() — JVM free memory in bytes
        funcs.put("freeMemory", (args, line) -> {
            checkArgs("freeMemory", 0, args, line);
            return new LongValue(Runtime.getRuntime().freeMemory());
        });

        // cpuCount() — number of available CPU cores
        funcs.put("cpuCount", (args, line) -> {
            checkArgs("cpuCount", 0, args, line);
            return new IntValue(Runtime.getRuntime().availableProcessors());
        });

        // ============================================================
        // RANDOM
        // ============================================================

        // random() — random double between 0.0 and 1.0
        funcs.put("random", (args, line) -> {
            checkArgs("random", 0, args, line);
            return new DoubleValue(RANDOM.nextDouble());
        });

        // randomInt(min, max) — random int between min and max inclusive
        funcs.put("randomInt", (args, line) -> {
            checkArgs("randomInt", 2, args, line);
            int min = args.get(0).asInt();
            int max = args.get(1).asInt();
            if (min > max)
                throw new NovaRuntimeException(
                    "randomInt() min (" + min + ") cannot be greater than max (" + max + ")", line);
            return new IntValue(min + RANDOM.nextInt(max - min + 1));
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }
}