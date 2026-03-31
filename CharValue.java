public class CharValue extends Value {
    private final char value;

    public CharValue(char value) {
        this.value = value;
    }

    public Object getRaw() {
        return value;
    }

    @Override
    public char asChar() {
        return value;
    }

    @Override
    public int asInt() {
        return value; // ASCII
    }

    @Override
    public boolean asBoolean() {
        return value != '\0';
    }

    @Override
    public Value add(Value other) {
        if (other instanceof StringValue s)
            return new StringValue(this.toString() + s.getRaw());
        return new IntValue(this.asInt() + other.asInt());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}