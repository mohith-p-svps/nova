import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalValue extends Value {

    private final BigDecimal value;
    private static final MathContext MC = new MathContext(50);

    public BigDecimalValue(double val) {
        this.value = new BigDecimal(val, MC);
    }

    public BigDecimalValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getRaw() {
        return value;
    }

    @Override
    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public boolean asBoolean() {
        return value.compareTo(BigDecimal.ZERO) != 0;
    }

    // --- Arithmetic ---

    @Override
    public Value add(Value other) {
        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());
        return new BigDecimalValue(value.add(toBigDecimal(other), MC));
    }

    @Override
    public Value subtract(Value other) {
        return new BigDecimalValue(value.subtract(toBigDecimal(other), MC));
    }

    @Override
    public Value multiply(Value other) {
        return new BigDecimalValue(value.multiply(toBigDecimal(other), MC));
    }

    @Override
    public Value divide(Value other) {
        return new BigDecimalValue(value.divide(toBigDecimal(other), MC));
    }

    @Override
    public Value modulo(Value other) {
        return new BigDecimalValue(value.remainder(toBigDecimal(other), MC));
    }

    private BigDecimal toBigDecimal(Value v) {
        if (v instanceof BigDecimalValue b) return b.value;
        return new BigDecimal(v.asDouble(), MC);
    }

    // --- Comparisons ---

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare bigdecimal with " + other.getTypeName(), 0);
        return new BooleanValue(value.compareTo(toBigDecimal(other)) > 0);
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            throw new TypeError("Cannot compare bigdecimal with " + other.getTypeName(), 0);
        return new BooleanValue(value.compareTo(toBigDecimal(other)) < 0);
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof StringValue || other instanceof BooleanValue
            || other instanceof ArrayValue || other instanceof NullValue)
            return new BooleanValue(false);
        return new BooleanValue(value.compareTo(toBigDecimal(other)) == 0);
    }

    @Override
    public boolean isNegative() { return value.signum() < 0; }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString();
    }
}