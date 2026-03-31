import java.util.*;

public class ArraysModule {

    public static Map<String, BuiltinFunction> load() {
        return load(null);
    }

    public static Map<String, BuiltinFunction> load(Interpreter interpreter) {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // len(arr) — number of elements
        funcs.put("len", (args, line) -> {
            checkArgs("len", 1, args, line);
            ArrayValue arr = requireArray("len", args.get(0), line);
            return new IntValue(arr.length());
        });

        // push(arr, val) — append val to end, returns the array
        funcs.put("push", (args, line) -> {
            checkArgs("push", 2, args, line);
            ArrayValue arr = requireArray("push", args.get(0), line);
            arr.getRaw().add(args.get(1));
            return arr;
        });

        // pop(arr) — remove and return last element
        funcs.put("pop", (args, line) -> {
            checkArgs("pop", 1, args, line);
            ArrayValue arr = requireArray("pop", args.get(0), line);
            if (arr.length() == 0)
                throw new NovaRuntimeException("pop() called on empty array", line);
            return arr.getRaw().remove(arr.length() - 1);
        });

        // first(arr) — return first element
        funcs.put("first", (args, line) -> {
            checkArgs("first", 1, args, line);
            ArrayValue arr = requireArray("first", args.get(0), line);
            if (arr.length() == 0)
                throw new NovaRuntimeException("first() called on empty array", line);
            return arr.get(0, line);
        });

        // last(arr) — return last element
        funcs.put("last", (args, line) -> {
            checkArgs("last", 1, args, line);
            ArrayValue arr = requireArray("last", args.get(0), line);
            if (arr.length() == 0)
                throw new NovaRuntimeException("last() called on empty array", line);
            return arr.get(arr.length() - 1, line);
        });

        // contains(arr, val) — true if val exists in arr
        funcs.put("contains", (args, line) -> {
            checkArgs("contains", 2, args, line);
            ArrayValue arr = requireArray("contains", args.get(0), line);
            Value target = args.get(1);
            for (int i = 0; i < arr.length(); i++) {
                try {
                    if (arr.get(i, line).equal(target).asBoolean())
                        return new BooleanValue(true);
                } catch (TypeError e) {
                    throw new TypeError(e.getMessage(), line);
                }
            }
            return new BooleanValue(false);
        });

        // indexOf(arr, val) — index of first match, or -1
        funcs.put("indexOf", (args, line) -> {
            checkArgs("indexOf", 2, args, line);
            ArrayValue arr = requireArray("indexOf", args.get(0), line);
            Value target = args.get(1);
            for (int i = 0; i < arr.length(); i++) {
                try {
                    if (arr.get(i, line).equal(target).asBoolean())
                        return new IntValue(i);
                } catch (TypeError e) {
                    throw new TypeError(e.getMessage(), line);
                }
            }
            return new IntValue(-1);
        });

        // reverse(arr) — returns a new reversed array, original unchanged
        funcs.put("reverse", (args, line) -> {
            checkArgs("reverse", 1, args, line);
            ArrayValue arr = requireArray("reverse", args.get(0), line);
            List<Value> result = new ArrayList<>(arr.getRaw());
            Collections.reverse(result);
            return new ArrayValue(result);
        });

        // slice(arr, start, end) — new array from start (inclusive) to end (exclusive)
        funcs.put("slice", (args, line) -> {
            checkArgs("slice", 3, args, line);
            ArrayValue arr = requireArray("slice", args.get(0), line);
            int start = args.get(1).asInt();
            int end   = args.get(2).asInt();
            int len   = arr.length();

            if (start < 0 || start > len)
                throw new NovaRuntimeException(
                    "slice() start index " + start + " out of bounds for array of length " + len, line);
            if (end < start || end > len)
                throw new NovaRuntimeException(
                    "slice() end index " + end + " out of bounds for array of length " + len, line);

            return new ArrayValue(new ArrayList<>(arr.getRaw().subList(start, end)));
        });

        // join(arr, sep) — concatenate all elements as strings with separator
        funcs.put("join", (args, line) -> {
            checkArgs("join", 2, args, line);
            ArrayValue arr = requireArray("join", args.get(0), line);
            String sep = args.get(1).toString();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length(); i++) {
                sb.append(arr.get(i, line).toString());
                if (i < arr.length() - 1)
                    sb.append(sep);
            }
            return new StringValue(sb.toString());
        });

        // sort(arr) — sort array in ascending order, returns new sorted array
        // Works on arrays of comparable types (int, long, double, string)
        funcs.put("sort", (args, line) -> {
            checkArgs("sort", 1, args, line);
            ArrayValue arr = requireArray("sort", args.get(0), line);
            List<Value> sorted = new ArrayList<>(arr.getRaw());
            sorted.sort((a, b) -> {
                try {
                    if (a.less(b).asBoolean()) return -1;
                    if (a.greater(b).asBoolean()) return 1;
                    return 0;
                } catch (TypeError e) {
                    throw new TypeError("sort() cannot compare " + a.getTypeName()
                        + " with " + b.getTypeName(), line);
                }
            });
            return new ArrayValue(sorted);
        });

        // sortDesc(arr) — sort in descending order, returns new sorted array
        funcs.put("sortDesc", (args, line) -> {
            checkArgs("sortDesc", 1, args, line);
            ArrayValue arr = requireArray("sortDesc", args.get(0), line);
            List<Value> sorted = new ArrayList<>(arr.getRaw());
            sorted.sort((a, b) -> {
                try {
                    if (a.greater(b).asBoolean()) return -1;
                    if (a.less(b).asBoolean()) return 1;
                    return 0;
                } catch (TypeError e) {
                    throw new TypeError("sortDesc() cannot compare " + a.getTypeName()
                        + " with " + b.getTypeName(), line);
                }
            });
            return new ArrayValue(sorted);
        });

        // sum(arr) — sum all numeric elements
        funcs.put("sum", (args, line) -> {
            checkArgs("sum", 1, args, line);
            ArrayValue arr = requireArray("sum", args.get(0), line);
            if (arr.length() == 0) return new IntValue(0);
            Value total = new IntValue(0);
            for (int i = 0; i < arr.length(); i++)
                total = total.add(arr.get(i, line));
            return total;
        });

        // min(arr) — minimum value in array
        funcs.put("min", (args, line) -> {
            checkArgs("min", 1, args, line);
            ArrayValue arr = requireArray("min", args.get(0), line);
            if (arr.length() == 0)
                throw new NovaRuntimeException("min() called on empty array", line);
            Value min = arr.get(0, line);
            for (int i = 1; i < arr.length(); i++) {
                Value v = arr.get(i, line);
                if (v.less(min).asBoolean()) min = v;
            }
            return min;
        });

        // max(arr) — maximum value in array
        funcs.put("max", (args, line) -> {
            checkArgs("max", 1, args, line);
            ArrayValue arr = requireArray("max", args.get(0), line);
            if (arr.length() == 0)
                throw new NovaRuntimeException("max() called on empty array", line);
            Value max = arr.get(0, line);
            for (int i = 1; i < arr.length(); i++) {
                Value v = arr.get(i, line);
                if (v.greater(max).asBoolean()) max = v;
            }
            return max;
        });

        // unique(arr) — remove duplicate elements, preserves order
        funcs.put("unique", (args, line) -> {
            checkArgs("unique", 1, args, line);
            ArrayValue arr = requireArray("unique", args.get(0), line);
            List<Value> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                Value v = arr.get(i, line);
                boolean found = false;
                for (Value existing : result) {
                    try {
                        if (existing.equal(v).asBoolean()) { found = true; break; }
                    } catch (TypeError e) { /* skip */ }
                }
                if (!found) result.add(v);
            }
            return new ArrayValue(result);
        });

        // flatten(arr) — flatten one level of nested arrays
        funcs.put("flatten", (args, line) -> {
            checkArgs("flatten", 1, args, line);
            ArrayValue arr = requireArray("flatten", args.get(0), line);
            List<Value> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                Value v = arr.get(i, line);
                if (v instanceof ArrayValue inner)
                    result.addAll(inner.getRaw());
                else
                    result.add(v);
            }
            return new ArrayValue(result);
        });

        // map(arr, fn) — apply fn to each element, return new array
        funcs.put("map", (args, line) -> {
            checkArgs("map", 2, args, line);
            ArrayValue arr = requireArray("map", args.get(0), line);
            FunctionDefNode fn = requireFunction("map", args.get(1), line);
            List<Value> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                List<Value> fnArgs = new ArrayList<>();
                fnArgs.add(arr.get(i, line));
                result.add(interpreter.callFunction(fn, fnArgs, line));
            }
            return new ArrayValue(result);
        });

        // filter(arr, fn) — keep elements where fn returns true
        funcs.put("filter", (args, line) -> {
            checkArgs("filter", 2, args, line);
            ArrayValue arr = requireArray("filter", args.get(0), line);
            FunctionDefNode fn = requireFunction("filter", args.get(1), line);
            List<Value> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                Value elem = arr.get(i, line);
                List<Value> fnArgs = new ArrayList<>();
                fnArgs.add(elem);
                if (interpreter.callFunction(fn, fnArgs, line).asBoolean())
                    result.add(elem);
            }
            return new ArrayValue(result);
        });

        // reduce(arr, fn, init) — fold array into single value
        // fn receives (accumulator, element)
        funcs.put("reduce", (args, line) -> {
            checkArgs("reduce", 3, args, line);
            ArrayValue arr = requireArray("reduce", args.get(0), line);
            FunctionDefNode fn = requireFunction("reduce", args.get(1), line);
            Value acc = args.get(2);
            for (int i = 0; i < arr.length(); i++) {
                List<Value> fnArgs = new ArrayList<>();
                fnArgs.add(acc);
                fnArgs.add(arr.get(i, line));
                acc = interpreter.callFunction(fn, fnArgs, line);
            }
            return acc;
        });

        return funcs;
    }

    // Helper — validates arg count
    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }

    // Helper — validates first arg is an array
    private static ArrayValue requireArray(String name, Value v, int line) {
        if (!(v instanceof ArrayValue))
            throw new TypeError(
                name + "() expects an array but got " + v.getTypeName(), line);
        return (ArrayValue) v;
    }

    // Helper — validates arg is a FunctionValue
    private static FunctionDefNode requireFunction(String name, Value v, int line) {
        if (!(v instanceof FunctionValue))
            throw new TypeError(
                name + "() expects a function but got " + v.getTypeName(), line);
        return ((FunctionValue) v).func;
    }
}