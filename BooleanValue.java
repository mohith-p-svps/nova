public class BooleanValue extends Value {
    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public Object getRaw() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof BooleanValue b)
            return new BooleanValue(this.value == b.value);
        return new BooleanValue(false);
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }
}