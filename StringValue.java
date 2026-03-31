public class StringValue extends Value {
    private final String value;

    public StringValue(String value) {
        this.value = value;
    }

    public String getRaw() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return !value.isEmpty();
    }

    @Override
    public Value add(Value other) {
        return new StringValue(this.value + other.toString());
    }

    @Override
    public Value multiply(Value other) {
        int times = other.asInt();
        if (times < 0)
            throw new TypeError("Cannot repeat a string a negative number of times", 0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++)
            sb.append(this.value);
        return new StringValue(sb.toString());
    }

    @Override
    public Value equal(Value other) {
        if (other instanceof StringValue s)
            return new BooleanValue(this.value.equals(s.value));
        return new BooleanValue(false);
    }

    @Override
    public Value greater(Value other) {
        if (other instanceof StringValue s)
            return new BooleanValue(this.value.compareTo(s.value) > 0);
        throw new TypeError(
            "Cannot compare string with " + other.getTypeName(), 0);
    }

    @Override
    public Value less(Value other) {
        if (other instanceof StringValue s)
            return new BooleanValue(this.value.compareTo(s.value) < 0);
        throw new TypeError(
            "Cannot compare string with " + other.getTypeName(), 0);
    }

    public int length() {
        return value.length();
    }

    @Override
    public String toString() {
        return value;
    }
}