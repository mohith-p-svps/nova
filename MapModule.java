import java.util.*;

public class MapModule {

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // get(m, key) — get value by key, returns null if not found
        funcs.put("get", (args, line) -> {
            checkArgs("get", 2, args, line);
            MapValue m = requireMap("get", args.get(0), line);
            String key = args.get(1).toString();
            return m.get(key);
        });

        // set(m, key, value) — set key to value
        funcs.put("set", (args, line) -> {
            checkArgs("set", 3, args, line);
            MapValue m = requireMap("set", args.get(0), line);
            String key = args.get(1).toString();
            m.set(key, args.get(2));
            return m;
        });

        // has(m, key) — true if key exists
        funcs.put("has", (args, line) -> {
            checkArgs("has", 2, args, line);
            MapValue m = requireMap("has", args.get(0), line);
            String key = args.get(1).toString();
            return new BooleanValue(m.has(key));
        });

        // remove(m, key) — delete a key, returns removed value or null
        funcs.put("remove", (args, line) -> {
            checkArgs("remove", 2, args, line);
            MapValue m = requireMap("remove", args.get(0), line);
            String key = args.get(1).toString();
            Value old = m.get(key);
            m.remove(key);
            return old;
        });

        // keys(m) — returns ArrayValue of all keys
        funcs.put("keys", (args, line) -> {
            checkArgs("keys", 1, args, line);
            MapValue m = requireMap("keys", args.get(0), line);
            List<Value> keys = new ArrayList<>();
            for (String k : m.getRaw().keySet())
                keys.add(new StringValue(k));
            return new ArrayValue(keys);
        });

        // values(m) — returns ArrayValue of all values
        funcs.put("values", (args, line) -> {
            checkArgs("values", 1, args, line);
            MapValue m = requireMap("values", args.get(0), line);
            List<Value> values = new ArrayList<>();
            for (Value v : m.getRaw().values())
                values.add(v);
            return new ArrayValue(values);
        });

        // size(m) — number of entries
        funcs.put("size", (args, line) -> {
            checkArgs("size", 1, args, line);
            MapValue m = requireMap("size", args.get(0), line);
            return new IntValue(m.size());
        });

        // clear(m) — remove all entries, returns the empty map
        funcs.put("clear", (args, line) -> {
            checkArgs("clear", 1, args, line);
            MapValue m = requireMap("clear", args.get(0), line);
            m.getRaw().clear();
            return m;
        });

        // merge(m1, m2) — returns new map with both combined, m2 wins on conflict
        funcs.put("merge", (args, line) -> {
            checkArgs("merge", 2, args, line);
            MapValue m1 = requireMap("merge", args.get(0), line);
            MapValue m2 = requireMap("merge", args.get(1), line);
            return m1.add(m2);
        });

        // entries(m) — returns array of ["key", value] pairs
        funcs.put("entries", (args, line) -> {
            checkArgs("entries", 1, args, line);
            MapValue m = requireMap("entries", args.get(0), line);
            List<Value> entries = new ArrayList<>();
            for (Map.Entry<String, Value> entry : m.getRaw().entrySet()) {
                List<Value> pair = new ArrayList<>();
                pair.add(new StringValue(entry.getKey()));
                pair.add(entry.getValue());
                entries.add(new ArrayValue(pair));
            }
            return new ArrayValue(entries);
        });

        // toArray(m) — convert map to array of ["key", value] pairs (alias for entries)
        funcs.put("toArray", (args, line) -> {
            checkArgs("toArray", 1, args, line);
            MapValue m = requireMap("toArray", args.get(0), line);
            List<Value> entries = new ArrayList<>();
            for (Map.Entry<String, Value> entry : m.getRaw().entrySet()) {
                List<Value> pair = new ArrayList<>();
                pair.add(new StringValue(entry.getKey()));
                pair.add(entry.getValue());
                entries.add(new ArrayValue(pair));
            }
            return new ArrayValue(entries);
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }

    private static MapValue requireMap(String name, Value v, int line) {
        if (!(v instanceof MapValue))
            throw new TypeError(
                name + "() expects a map but got " + v.getTypeName(), line);
        return (MapValue) v;
    }
}