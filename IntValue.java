import java.math.*;
public class IntValue extends Value {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    public Object getRaw() {
        return value;
    }

    @Override
    public int asInt() {
        return value;
    }

    @Override
    public long asLong() {
        return value;
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value != 0;
    }

    // --- Arithmetic ---

    @Override
    public Value add(Value other) {

        if (other instanceof IntValue i) {
            try {
                return new IntValue(Math.addExact(this.value, i.value));
            } catch (ArithmeticException e) {
                long temp = (long)this.value + i.value;

                if (temp <= Long.MAX_VALUE && temp >= Long.MIN_VALUE)
                    return new LongValue(temp);

                return new BigIntValue(BigInteger.valueOf(this.value).add(BigInteger.valueOf(i.value)));
            }
        }

        if (other instanceof LongValue l) {
            return new LongValue((long)this.value + l.asLong());
        }

        if (other instanceof DoubleValue d) {
            return new DoubleValue(this.value + d.asDouble());
        }

        if (other instanceof BigIntValue b) {
            return new BigIntValue(BigInteger.valueOf(this.value).add(b.getRaw()));
        }

        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());

        throw new TypeError("Cannot add " + other.getTypeName() + " to int", 0);
    }

    @Override
    public Value subtract(Value other) {

        if (other instanceof IntValue i) {
            try {
                return new IntValue(Math.subtractExact(this.value, i.value));
            } catch (ArithmeticException e) {
                return new LongValue((long)this.value - i.value);
            }
        }

        if (other instanceof LongValue l) {
            return new LongValue((long)this.value - l.asLong());
        }

        if (other instanceof DoubleValue d) {
            return new DoubleValue(this.value - d.asDouble());
        }
        
        if (other instanceof BigIntValue b) {
            return new BigIntValue(BigInteger.valueOf(this.value).subtract(b.getRaw()));
        }

        if (other instanceof BigDecimalValue b) {
            return new BigDecimalValue(new java.math.BigDecimal(this.value).subtract(b.getRaw(), new java.math.MathContext(50)));
        }

        throw new TypeError("Cannot subtract " + other.getTypeName() + " from int", 0);
    }

    @Override
    public Value multiply(Value other) {

        if (other instanceof IntValue i) {
            try {
                return new IntValue(Math.multiplyExact(this.value, i.value));
            } catch (ArithmeticException e) {
                return new LongValue((long)this.value * i.value);
            }
        }

        if (other instanceof LongValue l) {
            return new LongValue((long)this.value * l.asLong());
        }

        if (other instanceof DoubleValue d) {
            return new DoubleValue(this.value * d.asDouble());
        }
        
        if (other instanceof BigIntValue b) {
            return new BigIntValue(BigInteger.valueOf(this.value).multiply(b.getRaw()));
        }

        throw new TypeError("Cannot multiply int by " + other.getTypeName(), 0);
    }

    @Override
    public Value divide(Value other) {
        // int / int = int (integer division, like most languages)
        if (other instanceof IntValue i) {
            if (i.value == 0)
                throw new TypeError("Division by zero", 0);
            return new IntValue(this.value / i.value);
        }
        // int / long = long integer division
        if (other instanceof LongValue l) {
            if (l.asLong() == 0)
                throw new TypeError("Division by zero", 0);
            return new LongValue(this.asLong() / l.asLong());
        }
        // int / double = double
        return new DoubleValue(this.asDouble() / other.asDouble());
    }

    @Override
    public Value modulo(Value other) {
        if (other instanceof IntValue i) {
            if (i.value == 0)
                throw new TypeError("Modulo by zero", 0);
            return new IntValue(this.value % i.value);
        }
        if (other instanceof LongValue l) {
            if (l.asLong() == 0)
                throw new TypeError("Modulo by zero", 0);
            return new LongValue(this.asLong() % l.asLong());
        }
        if (other instanceof DoubleValue)
            return new DoubleValue(this.asDouble() % other.asDouble());
        if (other instanceof BigIntValue b)
            return new BigIntValue(BigInteger.valueOf(this.value).remainder(b.getRaw()));
        throw new TypeError("Cannot apply '%' to int and " + other.getTypeName(), 0);
    }

    @Override
    public boolean isNegative() { return value < 0; }

    // --- Comparisons (return int 1/0 for now) ---

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare int with " + other.getTypeName(), 0);
        return new BooleanValue(this.asDouble() > other.asDouble());
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare int with " + other.getTypeName(), 0);
        return new BooleanValue(this.asDouble() < other.asDouble());
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            return new BooleanValue(false);
        return new BooleanValue(this.asDouble() == other.asDouble());
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}