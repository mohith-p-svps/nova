import java.util.*;
import java.math.*;

public class ConvertModule {

    private static final MathContext MC = new MathContext(50);

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // ============================================================
        // TYPE CONVERSION
        // ============================================================

        // toInt(x) — convert to IntValue
        // string: parse, double: truncate, bool: true=1/false=0,
        // char: ASCII value, long/BigInt: narrow
        funcs.put("toInt", (args, line) -> {
            checkArgs("toInt", 1, args, line);
            Value x = args.get(0);

            if (x instanceof IntValue)
                return x;

            if (x instanceof LongValue l) {
                long v = l.asLong();
                if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE)
                    throw new TypeError(
                        "toInt() value " + v + " is too large for int", line);
                return new IntValue((int) v);
            }

            if (x instanceof DoubleValue)
                return new IntValue((int) x.asDouble());

            if (x instanceof BigIntValue b) {
                try {
                    return new IntValue(b.getRaw().intValueExact());
                } catch (ArithmeticException e) {
                    throw new TypeError(
                        "toInt() BigInteger value is too large for int", line);
                }
            }

            if (x instanceof BigDecimalValue b) {
                try {
                    return new IntValue(b.getRaw().intValueExact());
                } catch (ArithmeticException e) {
                    throw new TypeError(
                        "toInt() BigDecimal value cannot be converted exactly to int", line);
                }
            }

            if (x instanceof BooleanValue)
                return new IntValue(x.asBoolean() ? 1 : 0);

            if (x instanceof CharValue)
                return new IntValue(x.asInt()); // ASCII value

            if (x instanceof StringValue s) {
                try {
                    return new IntValue(Integer.parseInt(s.getRaw().trim()));
                } catch (NumberFormatException e) {
                    throw new TypeError(
                        "toInt() cannot parse \"" + s.getRaw() + "\" as int", line);
                }
            }

            throw new TypeError(
                "toInt() cannot convert " + x.getTypeName() + " to int", line);
        });

        // toLong(x) — convert to LongValue
        funcs.put("toLong", (args, line) -> {
            checkArgs("toLong", 1, args, line);
            Value x = args.get(0);

            if (x instanceof LongValue)
                return x;

            if (x instanceof IntValue)
                return new LongValue(x.asLong());

            if (x instanceof DoubleValue)
                return new LongValue((long) x.asDouble());

            if (x instanceof BigIntValue b) {
                try {
                    return new LongValue(b.getRaw().longValueExact());
                } catch (ArithmeticException e) {
                    throw new TypeError(
                        "toLong() BigInteger value is too large for long", line);
                }
            }

            if (x instanceof BigDecimalValue b) {
                try {
                    return new LongValue(b.getRaw().longValueExact());
                } catch (ArithmeticException e) {
                    throw new TypeError(
                        "toLong() BigDecimal value cannot be converted exactly to long", line);
                }
            }

            if (x instanceof BooleanValue)
                return new LongValue(x.asBoolean() ? 1L : 0L);

            if (x instanceof CharValue)
                return new LongValue((long) x.asInt());

            if (x instanceof StringValue s) {
                try {
                    return new LongValue(Long.parseLong(s.getRaw().trim()));
                } catch (NumberFormatException e) {
                    throw new TypeError(
                        "toLong() cannot parse \"" + s.getRaw() + "\" as long", line);
                }
            }

            throw new TypeError(
                "toLong() cannot convert " + x.getTypeName() + " to long", line);
        });

        // toDouble(x) — convert to DoubleValue
        funcs.put("toDouble", (args, line) -> {
            checkArgs("toDouble", 1, args, line);
            Value x = args.get(0);

            if (x instanceof DoubleValue)
                return x;

            if (x instanceof IntValue || x instanceof LongValue)
                return new DoubleValue(x.asDouble());

            if (x instanceof BigIntValue b)
                return new DoubleValue(b.getRaw().doubleValue());

            if (x instanceof BigDecimalValue b)
                return new DoubleValue(b.getRaw().doubleValue());

            if (x instanceof BooleanValue)
                return new DoubleValue(x.asBoolean() ? 1.0 : 0.0);

            if (x instanceof CharValue)
                return new DoubleValue((double) x.asInt());

            if (x instanceof StringValue s) {
                try {
                    return new DoubleValue(Double.parseDouble(s.getRaw().trim()));
                } catch (NumberFormatException e) {
                    throw new TypeError(
                        "toDouble() cannot parse \"" + s.getRaw() + "\" as double", line);
                }
            }

            throw new TypeError(
                "toDouble() cannot convert " + x.getTypeName() + " to double", line);
        });

        // toBigInteger(x) — convert to BigIntValue
        funcs.put("toBigInteger", (args, line) -> {
            checkArgs("toBigInteger", 1, args, line);
            Value x = args.get(0);

            if (x instanceof BigIntValue)
                return x;

            if (x instanceof IntValue || x instanceof LongValue)
                return new BigIntValue(BigInteger.valueOf(x.asLong()));

            if (x instanceof DoubleValue) {
                if (x.isNegative() || !Double.isFinite(x.asDouble()))
                    throw new TypeError(
                        "toBigInteger() cannot convert non-finite double", line);
                return new BigIntValue(BigDecimal.valueOf(x.asDouble()).toBigInteger());
            }

            if (x instanceof BigDecimalValue b) {
                try {
                    return new BigIntValue(b.getRaw().toBigIntegerExact());
                } catch (ArithmeticException e) {
                    throw new TypeError(
                        "toBigInteger() BigDecimal has fractional part — use toDouble() first", line);
                }
            }

            if (x instanceof StringValue s) {
                try {
                    return new BigIntValue(new BigInteger(s.getRaw().trim()));
                } catch (NumberFormatException e) {
                    throw new TypeError(
                        "toBigInteger() cannot parse \"" + s.getRaw() + "\" as BigInteger", line);
                }
            }

            throw new TypeError(
                "toBigInteger() cannot convert " + x.getTypeName() + " to BigInteger", line);
        });

        // toBigDecimal(x) — convert to BigDecimalValue
        funcs.put("toBigDecimal", (args, line) -> {
            checkArgs("toBigDecimal", 1, args, line);
            Value x = args.get(0);

            if (x instanceof BigDecimalValue)
                return x;

            if (x instanceof IntValue || x instanceof LongValue)
                return new BigDecimalValue(new BigDecimal(x.asLong(), MC));

            if (x instanceof DoubleValue)
                return new BigDecimalValue(new BigDecimal(x.asDouble(), MC));

            if (x instanceof BigIntValue b)
                return new BigDecimalValue(new BigDecimal(b.getRaw(), MC));

            if (x instanceof StringValue s) {
                try {
                    return new BigDecimalValue(new BigDecimal(s.getRaw().trim(), MC));
                } catch (NumberFormatException e) {
                    throw new TypeError(
                        "toBigDecimal() cannot parse \"" + s.getRaw() + "\" as BigDecimal", line);
                }
            }

            throw new TypeError(
                "toBigDecimal() cannot convert " + x.getTypeName() + " to BigDecimal", line);
        });

        // toString(x) — convert any value to StringValue
        funcs.put("toString", (args, line) -> {
            checkArgs("toString", 1, args, line);
            return new StringValue(args.get(0).toString());
        });

        // toChar(x) — convert int (ASCII) to CharValue
        funcs.put("toChar", (args, line) -> {
            checkArgs("toChar", 1, args, line);
            Value x = args.get(0);

            if (x instanceof CharValue)
                return x;

            if (x instanceof StringValue s) {
                String raw = s.getRaw();
                if (raw.length() != 1)
                    throw new TypeError(
                        "toChar() string must be exactly 1 character long", line);
                return new CharValue(raw.charAt(0));
            }

            int code = x.asInt();
            if (code < 0 || code > 65535)
                throw new TypeError(
                    "toChar() value " + code + " is outside valid char range (0-65535)", line);
            return new CharValue((char) code);
        });

        // toBool(x) — convert to BooleanValue
        // 0 → false, non-zero → true
        // "" → false, non-empty → true
        // null → false
        funcs.put("toBool", (args, line) -> {
            checkArgs("toBool", 1, args, line);
            Value x = args.get(0);

            if (x instanceof BooleanValue)
                return x;

            if (x instanceof NullValue)
                return new BooleanValue(false);

            if (x instanceof StringValue s) {
                String raw = s.getRaw().trim().toLowerCase();
                if (raw.equals("true"))  return new BooleanValue(true);
                if (raw.equals("false")) return new BooleanValue(false);
                // non-empty = true, empty = false
                return new BooleanValue(!raw.isEmpty());
            }

            // For all numeric types: 0 = false, non-zero = true
            return new BooleanValue(x.asBoolean());
        });

        // ============================================================
        // TYPE CHECKING
        // ============================================================

        funcs.put("isInt", (args, line) -> {
            checkArgs("isInt", 1, args, line);
            return new BooleanValue(args.get(0) instanceof IntValue);
        });

        funcs.put("isLong", (args, line) -> {
            checkArgs("isLong", 1, args, line);
            return new BooleanValue(args.get(0) instanceof LongValue);
        });

        funcs.put("isDouble", (args, line) -> {
            checkArgs("isDouble", 1, args, line);
            return new BooleanValue(args.get(0) instanceof DoubleValue);
        });

        funcs.put("isBigInteger", (args, line) -> {
            checkArgs("isBigInteger", 1, args, line);
            return new BooleanValue(args.get(0) instanceof BigIntValue);
        });

        funcs.put("isBigDecimal", (args, line) -> {
            checkArgs("isBigDecimal", 1, args, line);
            return new BooleanValue(args.get(0) instanceof BigDecimalValue);
        });

        funcs.put("isString", (args, line) -> {
            checkArgs("isString", 1, args, line);
            return new BooleanValue(args.get(0) instanceof StringValue);
        });

        funcs.put("isChar", (args, line) -> {
            checkArgs("isChar", 1, args, line);
            return new BooleanValue(args.get(0) instanceof CharValue);
        });

        funcs.put("isBool", (args, line) -> {
            checkArgs("isBool", 1, args, line);
            return new BooleanValue(args.get(0) instanceof BooleanValue);
        });

        funcs.put("isNull", (args, line) -> {
            checkArgs("isNull", 1, args, line);
            return new BooleanValue(args.get(0) instanceof NullValue);
        });

        funcs.put("isArray", (args, line) -> {
            checkArgs("isArray", 1, args, line);
            return new BooleanValue(args.get(0) instanceof ArrayValue);
        });

        funcs.put("isMap", (args, line) -> {
            checkArgs("isMap", 1, args, line);
            return new BooleanValue(args.get(0) instanceof MapValue);
        });

        funcs.put("isFunction", (args, line) -> {
            checkArgs("isFunction", 1, args, line);
            return new BooleanValue(args.get(0) instanceof FunctionValue);
        });

        // typeOf(x) — returns type name as string
        funcs.put("typeOf", (args, line) -> {
            checkArgs("typeOf", 1, args, line);
            return new StringValue(args.get(0).getTypeName());
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }
}