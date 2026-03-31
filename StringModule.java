import java.util.*;

public class StringModule {

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // --- Inspection ---

        // len(s) — length of string
        funcs.put("len", (args, line) -> {
            checkArgs("len", 1, args, line);
            String s = requireString("len", args.get(0), line);
            return new IntValue(s.length());
        });

        // contains(s, sub) — true if sub exists in s
        funcs.put("contains", (args, line) -> {
            checkArgs("contains", 2, args, line);
            String s   = requireString("contains", args.get(0), line);
            String sub = requireString("contains", args.get(1), line);
            return new BooleanValue(s.contains(sub));
        });

        // startsWith(s, sub) — true if s starts with sub
        funcs.put("startsWith", (args, line) -> {
            checkArgs("startsWith", 2, args, line);
            String s   = requireString("startsWith", args.get(0), line);
            String sub = requireString("startsWith", args.get(1), line);
            return new BooleanValue(s.startsWith(sub));
        });

        // endsWith(s, sub) — true if s ends with sub
        funcs.put("endsWith", (args, line) -> {
            checkArgs("endsWith", 2, args, line);
            String s   = requireString("endsWith", args.get(0), line);
            String sub = requireString("endsWith", args.get(1), line);
            return new BooleanValue(s.endsWith(sub));
        });

        // indexOf(s, sub) — index of first occurrence, -1 if not found
        funcs.put("indexOf", (args, line) -> {
            checkArgs("indexOf", 2, args, line);
            String s   = requireString("indexOf", args.get(0), line);
            String sub = requireString("indexOf", args.get(1), line);
            return new IntValue(s.indexOf(sub));
        });

        // --- Transformation ---

        // upper(s) — uppercase
        funcs.put("upper", (args, line) -> {
            checkArgs("upper", 1, args, line);
            String s = requireString("upper", args.get(0), line);
            return new StringValue(s.toUpperCase());
        });

        // lower(s) — lowercase
        funcs.put("lower", (args, line) -> {
            checkArgs("lower", 1, args, line);
            String s = requireString("lower", args.get(0), line);
            return new StringValue(s.toLowerCase());
        });

        // trim(s) — remove leading and trailing whitespace
        funcs.put("trim", (args, line) -> {
            checkArgs("trim", 1, args, line);
            String s = requireString("trim", args.get(0), line);
            return new StringValue(s.trim());
        });

        // replace(s, old, new) — replace all occurrences of old with new
        funcs.put("replace", (args, line) -> {
            checkArgs("replace", 3, args, line);
            String s      = requireString("replace", args.get(0), line);
            String oldSub = requireString("replace", args.get(1), line);
            String newSub = requireString("replace", args.get(2), line);
            return new StringValue(s.replace(oldSub, newSub));
        });

        // reverse(s) — reverse the string
        funcs.put("reverse", (args, line) -> {
            checkArgs("reverse", 1, args, line);
            String s = requireString("reverse", args.get(0), line);
            return new StringValue(new StringBuilder(s).reverse().toString());
        });

        // repeat(s, n) — repeat string n times
        funcs.put("repeat", (args, line) -> {
            checkArgs("repeat", 2, args, line);
            String s = requireString("repeat", args.get(0), line);
            int n = args.get(1).asInt();
            if (n < 0)
                throw new TypeError("repeat() count cannot be negative", line);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++)
                sb.append(s);
            return new StringValue(sb.toString());
        });

        // --- Splitting / Slicing ---

        // slice(s, start, end) — substring from start (inclusive) to end (exclusive)
        funcs.put("slice", (args, line) -> {
            checkArgs("slice", 3, args, line);
            String s  = requireString("slice", args.get(0), line);
            int start = args.get(1).asInt();
            int end   = args.get(2).asInt();

            if (start < 0 || start > s.length())
                throw new NovaIndexOutOfBoundsException(start, s.length(), line);
            if (end < start || end > s.length())
                throw new NovaIndexOutOfBoundsException(end, s.length(), line);

            return new StringValue(s.substring(start, end));
        });

        // split(s, sep) — split into array by separator
        funcs.put("split", (args, line) -> {
            checkArgs("split", 2, args, line);
            String s   = requireString("split", args.get(0), line);
            String sep = requireString("split", args.get(1), line);

            String[] parts = s.split(java.util.regex.Pattern.quote(sep), -1);
            List<Value> elements = new ArrayList<>();
            for (String part : parts)
                elements.add(new StringValue(part));
            return new ArrayValue(elements);
        });

        // charAt(s, i) — character at index as string
        funcs.put("charAt", (args, line) -> {
            checkArgs("charAt", 2, args, line);
            String s = requireString("charAt", args.get(0), line);
            int i    = args.get(1).asInt();

            if (i < 0 || i >= s.length())
                throw new NovaIndexOutOfBoundsException(i, s.length(), line);

            return new StringValue(String.valueOf(s.charAt(i)));
        });

        // padLeft(s, n, ch) — pad string on the left to length n with char ch
        funcs.put("padLeft", (args, line) -> {
            checkArgs("padLeft", 3, args, line);
            String s  = requireString("padLeft", args.get(0), line);
            int n     = args.get(1).asInt();
            String ch = requireString("padLeft", args.get(2), line);
            if (ch.length() != 1)
                throw new TypeError("padLeft() padding character must be exactly 1 character", line);
            if (s.length() >= n) return new StringValue(s);
            StringBuilder sb = new StringBuilder();
            for (int i = s.length(); i < n; i++) sb.append(ch);
            sb.append(s);
            return new StringValue(sb.toString());
        });

        // padRight(s, n, ch) — pad string on the right to length n with char ch
        funcs.put("padRight", (args, line) -> {
            checkArgs("padRight", 3, args, line);
            String s  = requireString("padRight", args.get(0), line);
            int n     = args.get(1).asInt();
            String ch = requireString("padRight", args.get(2), line);
            if (ch.length() != 1)
                throw new TypeError("padRight() padding character must be exactly 1 character", line);
            if (s.length() >= n) return new StringValue(s);
            StringBuilder sb = new StringBuilder(s);
            for (int i = s.length(); i < n; i++) sb.append(ch);
            return new StringValue(sb.toString());
        });

        // isNumeric(s) — true if string can be parsed as a number
        funcs.put("isNumeric", (args, line) -> {
            checkArgs("isNumeric", 1, args, line);
            String s = requireString("isNumeric", args.get(0), line);
            try {
                Double.parseDouble(s.trim());
                return new BooleanValue(true);
            } catch (NumberFormatException e) {
                return new BooleanValue(false);
            }
        });

        return funcs;
    }

    // Helper — validates arg count
    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }

    // Helper — validates arg is a string and returns its raw value
    private static String requireString(String name, Value v, int line) {
        if (!(v instanceof StringValue))
            throw new TypeError(
                name + "() expects a string but got " + v.getTypeName(), line);
        return ((StringValue) v).getRaw();
    }
}