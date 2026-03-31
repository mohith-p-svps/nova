public class NullValue extends Value {

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public Value add(Value other) {
        throw new TypeError("Cannot use '+' on null", 0);
    }

    @Override
    public Value subtract(Value other) {
        throw new TypeError("Cannot use '-' on null", 0);
    }

    @Override
    public Value multiply(Value other) {
        throw new TypeError("Cannot use '*' on null", 0);
    }

    @Override
    public Value divide(Value other) {
        throw new TypeError("Cannot use '/' on null", 0);
    }

    @Override
    public Value equal(Value other) {
        return new BooleanValue(other instanceof NullValue);
    }

    @Override
    public Value not() {
        return new BooleanValue(true);
    }

    @Override
    public Value and(Value other) {
        return new BooleanValue(false);
    }

    @Override
    public Value or(Value other) {
        return other;
    }
}