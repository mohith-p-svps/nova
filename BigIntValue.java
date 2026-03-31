import java.math.*;

public class BigIntValue extends Value {

    private final BigInteger value;

    public BigIntValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getRaw() {
        return value;
    }

    @Override
    public long asLong() {
        return value.longValue();
    }

    @Override
    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public boolean asBoolean() {
        return !value.equals(BigInteger.ZERO);
    }

    // --- Arithmetic ---

    @Override
    public Value add(Value other) {

        if (other instanceof DoubleValue || other instanceof BigDecimalValue) {
            return new BigDecimalValue(value.doubleValue()).add(other);
        }

        if (other instanceof BigIntValue b)
            return new BigIntValue(value.add(b.value));

        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());

        return new BigIntValue(value.add(BigInteger.valueOf(other.asLong())));
    }

    @Override
    public Value subtract(Value other) {

        if (other instanceof DoubleValue || other instanceof BigDecimalValue) {
            return new BigDecimalValue(value.doubleValue()).subtract(other);
        }

        if (other instanceof BigIntValue b)
            return new BigIntValue(value.subtract(b.value));

        return new BigIntValue(value.subtract(BigInteger.valueOf(other.asLong())));
    }

    @Override
    public Value multiply(Value other) {

        if (other instanceof DoubleValue || other instanceof BigDecimalValue) {
            return new BigDecimalValue(value.doubleValue()).multiply(other);
        }

        if (other instanceof BigIntValue b)
            return new BigIntValue(value.multiply(b.value));

        return new BigIntValue(value.multiply(BigInteger.valueOf(other.asLong())));
    }

    @Override
    public Value divide(Value other) {
        return new BigDecimalValue(value.doubleValue()).divide(other);
    }

    @Override
    public Value modulo(Value other) {
        if (other instanceof BigIntValue b)
            return new BigIntValue(value.remainder(b.getRaw()));
        if (other instanceof IntValue || other instanceof LongValue)
            return new BigIntValue(value.remainder(BigInteger.valueOf(other.asLong())));
        return new DoubleValue(this.asDouble() % other.asDouble());
    }

    // --- Comparisons ---

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare bigint with " + other.getTypeName(), 0);
        if (other instanceof BigIntValue b)
            return new BooleanValue(value.compareTo(b.getRaw()) > 0);
        if (other instanceof BigDecimalValue b)
            return new BooleanValue(new java.math.BigDecimal(value).compareTo(b.getRaw()) > 0);
        return new BooleanValue(value.compareTo(BigInteger.valueOf(other.asLong())) > 0);
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare bigint with " + other.getTypeName(), 0);
        if (other instanceof BigIntValue b)
            return new BooleanValue(value.compareTo(b.getRaw()) < 0);
        if (other instanceof BigDecimalValue b)
            return new BooleanValue(new java.math.BigDecimal(value).compareTo(b.getRaw()) < 0);
        return new BooleanValue(value.compareTo(BigInteger.valueOf(other.asLong())) < 0);
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            return new BooleanValue(false);
        if (other instanceof BigIntValue b)
            return new BooleanValue(value.compareTo(b.getRaw()) == 0);
        if (other instanceof BigDecimalValue b)
            return new BooleanValue(new java.math.BigDecimal(value).compareTo(b.getRaw()) == 0);
        return new BooleanValue(value.compareTo(BigInteger.valueOf(other.asLong())) == 0);
    }

    @Override
    public boolean isNegative() { return value.signum() < 0; }

    @Override
    public String toString() {
        return value.toString();
    }
}