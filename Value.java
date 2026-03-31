public abstract class Value {

    // --- Type conversions ---
    public int asInt() {
        throw new TypeError("Cannot convert " + getTypeName() + " to int", 0);
    }

    public long asLong() {
        return asInt();
    }

    public double asDouble() {
        return asInt();
    }

    public char asChar() {
        throw new TypeError("Cannot convert " + getTypeName() + " to char", 0);
    }

    public boolean asBoolean() {
        throw new TypeError("Cannot convert " + getTypeName() + " to boolean", 0);
    }

    // Returns the NovaLang type name for error messages
    public String getTypeName() {
        if (this instanceof IntValue)        return "int";
        if (this instanceof LongValue)       return "long";
        if (this instanceof DoubleValue)     return "double";
        if (this instanceof BigIntValue)     return "bigint";
        if (this instanceof BigDecimalValue) return "bigdecimal";
        if (this instanceof StringValue)     return "string";
        if (this instanceof CharValue)       return "char";
        if (this instanceof BooleanValue)    return "boolean";
        if (this instanceof ArrayValue)      return "array";
        if (this instanceof MapValue)        return "map";
        if (this instanceof FunctionValue)   return "function";
        if (this instanceof NullValue)       return "null";
        return "unknown";
    }

    public Value and(Value other) {
        return new BooleanValue(this.asBoolean() && other.asBoolean());
    }

    public Value or(Value other) {
        return new BooleanValue(this.asBoolean() || other.asBoolean());
    }

    public Value not() {
        return new BooleanValue(!this.asBoolean());
    }

    // --- Arithmetic operations ---
    public Value add(Value other) {
        throw new TypeError("Operator '+' is not supported for type " + getTypeName(), 0);
    }

    public Value subtract(Value other) {
        throw new TypeError("Operator '-' is not supported for type " + getTypeName(), 0);
    }

    public Value multiply(Value other) {
        throw new TypeError("Operator '*' is not supported for type " + getTypeName(), 0);
    }

    public Value divide(Value other) {
        throw new TypeError("Operator '/' is not supported for type " + getTypeName(), 0);
    }

    public Value modulo(Value other) {
        throw new TypeError("Operator '%' is not supported for type " + getTypeName(), 0);
    }

    // --- Sign check ---
    // Returns true if the value is strictly less than zero
    // Non-numeric types throw TypeError
    public boolean isNegative() {
        throw new TypeError("Cannot check sign of " + getTypeName(), 0);
    }
    public Value greater(Value other) {
        throw new TypeError("Operator '>' is not supported for type " + getTypeName(), 0);
    }

    public Value less(Value other) {
        throw new TypeError("Operator '<' is not supported for type " + getTypeName(), 0);
    }

    public Value equal(Value other) {
        throw new TypeError("Operator '==' is not supported for type " + getTypeName(), 0);
    }
}