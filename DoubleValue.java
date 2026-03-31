public class DoubleValue extends Value {
    private final double value;

    public DoubleValue(double value) {
        this.value = value;
    }

    public Object getRaw() {
        return value;
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value != 0.0;
    }

    @Override
    public Value add(Value other) {
        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());
        return new DoubleValue(this.value + other.asDouble());
    }

    @Override
    public Value subtract(Value other) {
        return new DoubleValue(this.value - other.asDouble());
    }

    @Override
    public Value multiply(Value other) {
        return new DoubleValue(this.value * other.asDouble());
    }

    @Override
    public Value divide(Value other) {
        return new DoubleValue(this.value / other.asDouble());
    }

    @Override
    public Value modulo(Value other) {
        return new DoubleValue(this.value % other.asDouble());
    }

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare double with " + other.getTypeName(), 0);
        return new BooleanValue(this.asDouble() > other.asDouble());
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare double with " + other.getTypeName(), 0);
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
    public boolean isNegative() {
        // NaN is not negative — only strictly less than zero counts
        return !Double.isNaN(value) && value < 0;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}