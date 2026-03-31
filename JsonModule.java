import java.util.*;

public class JsonModule {

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // parse(s) — parse JSON string into Nova values
        funcs.put("parse", (args, line) -> {
            checkArgs("parse", 1, args, line);
            if (!(args.get(0) instanceof StringValue s))
                throw new TypeError("json.parse() expects a string", line);
            try {
                return parseValue(s.getRaw().trim(), new int[]{0}, line);
            } catch (NovaException e) {
                throw e;
            } catch (Exception e) {
                throw new NovaRuntimeException("json.parse() failed: " + e.getMessage(), line);
            }
        });

        // stringify(v) — convert Nova value to JSON string
        funcs.put("stringify", (args, line) -> {
            checkArgs("stringify", 1, args, line);
            return new StringValue(toJson(args.get(0)));
        });

        return funcs;
    }

    private static Value parseValue(String json, int[] pos, int line) {
        skipWhitespace(json, pos);
        if (pos[0] >= json.length())
            throw new NovaRuntimeException("json.parse() unexpected end of input", line);

        char c = json.charAt(pos[0]);
        if (c == '{') return parseObject(json, pos, line);
        if (c == '[') return parseArray(json, pos, line);
        if (c == '"') return parseString(json, pos, line);
        if (c == 't') return parseTrue(json, pos, line);
        if (c == 'f') return parseFalse(json, pos, line);
        if (c == 'n') return parseNull(json, pos, line);
        if (c == '-' || Character.isDigit(c)) return parseNumber(json, pos, line);
        throw new NovaRuntimeException("json.parse() unexpected character: '" + c + "'", line);
    }

    private static MapValue parseObject(String json, int[] pos, int line) {
        pos[0]++;
        MapValue map = new MapValue();
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == '}') { pos[0]++; return map; }

        while (pos[0] < json.length()) {
            skipWhitespace(json, pos);
            if (json.charAt(pos[0]) != '"')
                throw new NovaRuntimeException("json.parse() expected string key", line);
            String key = ((StringValue) parseString(json, pos, line)).getRaw();
            skipWhitespace(json, pos);
            if (pos[0] >= json.length() || json.charAt(pos[0]) != ':')
                throw new NovaRuntimeException("json.parse() expected ':' after key", line);
            pos[0]++;
            map.set(key, parseValue(json, pos, line));
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) break;
            char next = json.charAt(pos[0]);
            if (next == '}') { pos[0]++; break; }
            if (next == ',') { pos[0]++; continue; }
            throw new NovaRuntimeException("json.parse() expected ',' or '}' in object", line);
        }
        return map;
    }

    private static ArrayValue parseArray(String json, int[] pos, int line) {
        pos[0]++;
        List<Value> elements = new ArrayList<>();
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == ']') { pos[0]++; return new ArrayValue(elements); }

        while (pos[0] < json.length()) {
            elements.add(parseValue(json, pos, line));
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) break;
            char next = json.charAt(pos[0]);
            if (next == ']') { pos[0]++; break; }
            if (next == ',') { pos[0]++; continue; }
            throw new NovaRuntimeException("json.parse() expected ',' or ']' in array", line);
        }
        return new ArrayValue(elements);
    }

    private static StringValue parseString(String json, int[] pos, int line) {
        pos[0]++;
        StringBuilder sb = new StringBuilder();
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '"') { pos[0]++; break; }
            if (c == '\\') {
                pos[0]++;
                if (pos[0] >= json.length()) break;
                switch (json.charAt(pos[0])) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(json.charAt(pos[0]));
                }
            } else {
                sb.append(c);
            }
            pos[0]++;
        }
        return new StringValue(sb.toString());
    }

    private static Value parseNumber(String json, int[] pos, int line) {
        int start = pos[0];
        boolean isDouble = false;
        if (pos[0] < json.length() && json.charAt(pos[0]) == '-') pos[0]++;
        while (pos[0] < json.length() && Character.isDigit(json.charAt(pos[0]))) pos[0]++;
        if (pos[0] < json.length() && json.charAt(pos[0]) == '.') {
            isDouble = true; pos[0]++;
            while (pos[0] < json.length() && Character.isDigit(json.charAt(pos[0]))) pos[0]++;
        }
        if (pos[0] < json.length() && (json.charAt(pos[0]) == 'e' || json.charAt(pos[0]) == 'E')) {
            isDouble = true; pos[0]++;
            if (pos[0] < json.length() && (json.charAt(pos[0]) == '+' || json.charAt(pos[0]) == '-')) pos[0]++;
            while (pos[0] < json.length() && Character.isDigit(json.charAt(pos[0]))) pos[0]++;
        }
        String num = json.substring(start, pos[0]);
        if (isDouble) return new DoubleValue(Double.parseDouble(num));
        try { return new IntValue(Integer.parseInt(num)); }
        catch (NumberFormatException e1) {
            try { return new LongValue(Long.parseLong(num)); }
            catch (NumberFormatException e2) { return new BigIntValue(new java.math.BigInteger(num)); }
        }
    }

    private static BooleanValue parseTrue(String json, int[] pos, int line) {
        if (json.startsWith("true", pos[0])) { pos[0] += 4; return new BooleanValue(true); }
        throw new NovaRuntimeException("json.parse() invalid token", line);
    }

    private static BooleanValue parseFalse(String json, int[] pos, int line) {
        if (json.startsWith("false", pos[0])) { pos[0] += 5; return new BooleanValue(false); }
        throw new NovaRuntimeException("json.parse() invalid token", line);
    }

    private static NullValue parseNull(String json, int[] pos, int line) {
        if (json.startsWith("null", pos[0])) { pos[0] += 4; return new NullValue(); }
        throw new NovaRuntimeException("json.parse() invalid token", line);
    }

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) pos[0]++;
    }

    private static String toJson(Value v) {
        if (v instanceof NullValue)       return "null";
        if (v instanceof BooleanValue)    return v.asBoolean() ? "true" : "false";
        if (v instanceof StringValue s)   return "\"" + s.getRaw().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
        if (v instanceof CharValue)       return "\"" + v.toString() + "\"";
        if (v instanceof IntValue || v instanceof LongValue || v instanceof BigIntValue) return v.toString();
        if (v instanceof DoubleValue || v instanceof BigDecimalValue) return v.toString();

        if (v instanceof ArrayValue a) {
            StringBuilder sb = new StringBuilder("[");
            List<Value> elems = a.getRaw();
            for (int i = 0; i < elems.size(); i++) {
                sb.append(toJson(elems.get(i)));
                if (i < elems.size() - 1) sb.append(", ");
            }
            return sb.append("]").toString();
        }

        if (v instanceof MapValue m) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Value> entry : m.getRaw().entrySet()) {
                sb.append("\"").append(entry.getKey()).append("\": ").append(toJson(entry.getValue()));
                if (i < m.size() - 1) sb.append(", ");
                i++;
            }
            return sb.append("}").toString();
        }

        return "null";
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }
}