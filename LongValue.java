import java.math.*;
public class LongValue extends Value {
    private final long value;

    public LongValue(long value) {
        this.value = value;
    }

    public Object getRaw() {
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
    public int asInt() {
        return (int) value;
    }

    @Override
    public boolean asBoolean() {
        return value != 0;
    }

    @Override
    public Value add(Value other) {

        if (other instanceof DoubleValue || other instanceof BigDecimalValue)
            return new BigDecimalValue(this.asDouble() + other.asDouble());

        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());

        try {
            long result = Math.addExact(this.value, other.asLong());
            return new LongValue(result);
        } catch (ArithmeticException e) {
            return new BigIntValue(
                BigInteger.valueOf(this.value).add(BigInteger.valueOf(other.asLong()))
            );
        }
    }

    @Override
    public Value subtract(Value other) {

        if (other instanceof DoubleValue)
            return new DoubleValue(this.asDouble() - other.asDouble());

        if (other instanceof BigIntValue b)
            return new BigIntValue(BigInteger.valueOf(this.value).subtract(b.getRaw()));

        try {
            long result = Math.subtractExact(this.value, other.asLong());
            return new LongValue(result);
        } catch (ArithmeticException e) {
            return new BigIntValue(
                BigInteger.valueOf(this.value).subtract(BigInteger.valueOf(other.asLong()))
            );
        }
    }

    @Override
    public Value multiply(Value other) {

        if (other instanceof DoubleValue)
            return new DoubleValue(this.asDouble() * other.asDouble());

        try {
            long result = Math.multiplyExact(this.value, other.asLong());
            return new LongValue(result);
        } catch (ArithmeticException e) {
            return new BigIntValue(
                BigInteger.valueOf(this.value).multiply(BigInteger.valueOf(other.asLong()))
            );
        }
    }

    @Override
    public Value divide(Value other) {
        // long / int or long / long = long integer division
        if (other instanceof IntValue || other instanceof LongValue) {
            long divisor = other.asLong();
            if (divisor == 0)
                throw new TypeError("Division by zero", 0);
            return new LongValue(this.value / divisor);
        }
        // long / double = double
        return new DoubleValue(this.asDouble() / other.asDouble());
    }

    @Override
    public Value modulo(Value other) {
        if (other instanceof IntValue || other instanceof LongValue) {
            long divisor = other.asLong();
            if (divisor == 0)
                throw new TypeError("Modulo by zero", 0);
            return new LongValue(this.value % divisor);
        }
        if (other instanceof DoubleValue)
            return new DoubleValue(this.asDouble() % other.asDouble());
        if (other instanceof BigIntValue b)
            return new BigIntValue(BigInteger.valueOf(this.value).remainder(b.getRaw()));
        throw new TypeError("Cannot apply '%' to long and " + other.getTypeName(), 0);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean isNegative() { return value < 0; }

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare long with " + other.getTypeName(), 0);
        return new BooleanValue(this.asDouble() > other.asDouble());
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare long with " + other.getTypeName(), 0);
        return new BooleanValue(this.asDouble() < other.asDouble());
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            return new BooleanValue(false);
        return new BooleanValue(this.asDouble() == other.asDouble());
    }
}