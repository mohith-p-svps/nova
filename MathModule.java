import java.util.*;
import java.math.*;

public class MathModule {

    private static final MathContext MC = new MathContext(50);

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // sqrt(x) — square root
        // int/long/double: uses Math.sqrt
        // BigInt/BigDecimal: uses BigDecimal.sqrt with 50 digit precision
        funcs.put("sqrt", (args, line) -> {
            checkArgs("sqrt", 1, args, line);
            Value x = args.get(0);
            if (x.isNegative())
                throw new TypeError("sqrt() requires a non-negative number", line);
            if (x instanceof BigIntValue b)
                return new BigDecimalValue(new BigDecimal(b.getRaw(), MC).sqrt(MC));
            if (x instanceof BigDecimalValue b)
                return new BigDecimalValue(b.getRaw().sqrt(MC));
            return new DoubleValue(Math.sqrt(x.asDouble()));
        });

        // pow(base, exp) — exponentiation
        // int/long base with int exp: uses BigInteger for exact results
        // anything with double exp: uses Math.pow
        // BigDecimal base: uses BigDecimal.pow
        funcs.put("pow", (args, line) -> {
            checkArgs("pow", 2, args, line);
            Value base = args.get(0);
            Value exp  = args.get(1);

            try {
                // BigDecimal base
                if (base instanceof BigDecimalValue b) {
                    if (exp instanceof IntValue i)
                        return new BigDecimalValue(b.getRaw().pow(i.asInt(), MC));
                    return new BigDecimalValue(
                        BigDecimal.valueOf(Math.pow(b.getRaw().doubleValue(), exp.asDouble())));
                }

                // BigInt base
                if (base instanceof BigIntValue b) {
                    if (exp instanceof IntValue i && !exp.isNegative())
                        return new BigIntValue(b.getRaw().pow(i.asInt()));
                    return new DoubleValue(Math.pow(b.getRaw().doubleValue(), exp.asDouble()));
                }

                // int base with int exp — stay exact
                if (base instanceof IntValue && exp instanceof IntValue) {
                    int b = base.asInt();
                    int e = exp.asInt();
                    if (exp.isNegative()) return new DoubleValue(Math.pow(b, e));
                    BigInteger result = BigInteger.valueOf(b).pow(e);
                    try {
                        return new IntValue(result.intValueExact());
                    } catch (ArithmeticException ex) {
                        try {
                            return new LongValue(result.longValueExact());
                        } catch (ArithmeticException ex2) {
                            return new BigIntValue(result);
                        }
                    }
                }

                // long base with int exp
                if ((base instanceof LongValue || base instanceof IntValue)
                    && exp instanceof IntValue) {
                    int e = exp.asInt();
                    if (exp.isNegative()) return new DoubleValue(Math.pow(base.asLong(), e));
                    BigInteger result = BigInteger.valueOf(base.asLong()).pow(e);
                    try {
                        return new LongValue(result.longValueExact());
                    } catch (ArithmeticException ex) {
                        return new BigIntValue(result);
                    }
                }

                // fallback: double only — double input means double precision output
                // if result overflows to Infinity, return it as-is
                double result = Math.pow(base.asDouble(), exp.asDouble());
                return new DoubleValue(result);

            } catch (ArithmeticException ex) {
                throw new NovaRuntimeException("pow() arithmetic error: " + ex.getMessage(), line);
            }
        });

        // abs(x) — absolute value, preserves type exactly
        funcs.put("abs", (args, line) -> {
            checkArgs("abs", 1, args, line);
            Value x = args.get(0);
            if (x instanceof IntValue)
                return new IntValue(Math.abs(x.asInt()));
            if (x instanceof LongValue)
                return new LongValue(Math.abs(x.asLong()));
            if (x instanceof BigIntValue b)
                return new BigIntValue(b.getRaw().abs());
            if (x instanceof BigDecimalValue b)
                return new BigDecimalValue(b.getRaw().abs());
            return new DoubleValue(Math.abs(x.asDouble()));
        });

        // floor(x) — round down
        // int/long/BigInt: already whole, return as-is
        // double/BigDecimal: floor then return as long or BigInteger
        funcs.put("floor", (args, line) -> {
            checkArgs("floor", 1, args, line);
            Value x = args.get(0);
            if (x instanceof IntValue || x instanceof LongValue || x instanceof BigIntValue)
                return x; // already an integer
            if (x instanceof BigDecimalValue b)
                return new BigIntValue(b.getRaw().setScale(0, RoundingMode.FLOOR).toBigInteger());
            return new LongValue((long) Math.floor(x.asDouble()));
        });

        // ceil(x) — round up
        funcs.put("ceil", (args, line) -> {
            checkArgs("ceil", 1, args, line);
            Value x = args.get(0);
            if (x instanceof IntValue || x instanceof LongValue || x instanceof BigIntValue)
                return x;
            if (x instanceof BigDecimalValue b)
                return new BigIntValue(b.getRaw().setScale(0, RoundingMode.CEILING).toBigInteger());
            return new LongValue((long) Math.ceil(x.asDouble()));
        });

        // round(x) — round to nearest
        funcs.put("round", (args, line) -> {
            checkArgs("round", 1, args, line);
            Value x = args.get(0);
            if (x instanceof IntValue || x instanceof LongValue || x instanceof BigIntValue)
                return x;
            if (x instanceof BigDecimalValue b)
                return new BigIntValue(b.getRaw().setScale(0, RoundingMode.HALF_UP).toBigInteger());
            return new LongValue(Math.round(x.asDouble()));
        });

        funcs.put("log", (args, line) -> {
            checkArgs("log", 1, args, line);
            Value x = args.get(0);
            if (x.isNegative() || x.equal(new IntValue(0)).asBoolean())
                throw new TypeError("log() requires a positive number", line);
            return new DoubleValue(Math.log(x.asDouble()));
        });

        funcs.put("log10", (args, line) -> {
            checkArgs("log10", 1, args, line);
            Value x = args.get(0);
            if (x.isNegative() || x.equal(new IntValue(0)).asBoolean())
                throw new TypeError("log10() requires a positive number", line);
            return new DoubleValue(Math.log10(x.asDouble()));
        });

        // sin / cos / tan — always double (transcendental)
        funcs.put("sin", (args, line) -> {
            checkArgs("sin", 1, args, line);
            return new DoubleValue(Math.sin(args.get(0).asDouble()));
        });

        funcs.put("cos", (args, line) -> {
            checkArgs("cos", 1, args, line);
            return new DoubleValue(Math.cos(args.get(0).asDouble()));
        });

        funcs.put("tan", (args, line) -> {
            checkArgs("tan", 1, args, line);
            return new DoubleValue(Math.tan(args.get(0).asDouble()));
        });

        // max(a, b) — larger value, preserves type
        funcs.put("max", (args, line) -> {
            checkArgs("max", 2, args, line);
            Value a = args.get(0);
            Value b = args.get(1);
            return a.greater(b).asBoolean() ? a : b;
        });

        // min(a, b) — smaller value, preserves type
        funcs.put("min", (args, line) -> {
            checkArgs("min", 2, args, line);
            Value a = args.get(0);
            Value b = args.get(1);
            return a.less(b).asBoolean() ? a : b;
        });

        // clamp(x, min, max) — constrain value to range [min, max]
        funcs.put("clamp", (args, line) -> {
            checkArgs("clamp", 3, args, line);
            Value x   = args.get(0);
            Value min = args.get(1);
            Value max = args.get(2);
            if (x.less(min).asBoolean()) return min;
            if (x.greater(max).asBoolean()) return max;
            return x;
        });

        // pi() — returns pi as double
        funcs.put("pi", (args, line) -> {
            checkArgs("pi", 0, args, line);
            return new DoubleValue(Math.PI);
        });

        // e() — returns Euler's number as double
        funcs.put("e", (args, line) -> {
            checkArgs("e", 0, args, line);
            return new DoubleValue(Math.E);
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }
}