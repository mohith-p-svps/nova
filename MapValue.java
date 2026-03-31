import java.util.*;

public class MapValue extends Value {
    // LinkedHashMap preserves insertion order
    private final LinkedHashMap<String, Value> map;

    public MapValue() {
        this.map = new LinkedHashMap<>();
    }

    public MapValue(LinkedHashMap<String, Value> map) {
        this.map = map;
    }

    public LinkedHashMap<String, Value> getRaw() {
        return map;
    }

    // Get value by key — returns NullValue if not found
    public Value get(String key) {
        return map.getOrDefault(key, new NullValue());
    }

    // Set key to value
    public void set(String key, Value value) {
        map.put(key, value);
    }

    // Check if key exists
    public boolean has(String key) {
        return map.containsKey(key);
    }

    // Remove a key
    public void remove(String key) {
        map.remove(key);
    }

    // Number of entries
    public int size() {
        return map.size();
    }

    // Non-empty map is truthy
    @Override
    public boolean asBoolean() {
        return !map.isEmpty();
    }

    // Two maps are equal if they have same keys and values
    @Override
    public Value equal(Value other) {
        if (!(other instanceof MapValue m))
            return new BooleanValue(false);
        if (this.map.size() != m.map.size())
            return new BooleanValue(false);
        for (String key : map.keySet()) {
            if (!m.has(key))
                return new BooleanValue(false);
            if (!map.get(key).equal(m.get(key)).asBoolean())
                return new BooleanValue(false);
        }
        return new BooleanValue(true);
    }

    // Merge two maps: m1 + m2 (m2 values overwrite m1 on conflict)
    @Override
    public Value add(Value other) {
        if (!(other instanceof MapValue m))
            throw new TypeError("Cannot add map and " + other.getTypeName(), 0);
        LinkedHashMap<String, Value> result = new LinkedHashMap<>(this.map);
        result.putAll(m.map);
        return new MapValue(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": ");
            Value v = entry.getValue();
            if (v instanceof StringValue)
                sb.append("\"").append(v).append("\"");
            else
                sb.append(v);
            if (i < map.size() - 1) sb.append(", ");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
}