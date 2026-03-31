import java.util.*;

public class ArrayValue extends Value {
    private final List<Value> elements;

    public ArrayValue(List<Value> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public List<Value> getRaw() {
        return elements;
    }

    // line is passed in so the exception carries the right source location
    public Value get(int index, int line) {
        if (index < 0 || index >= elements.size())
            throw new NovaIndexOutOfBoundsException(index, elements.size(), line);
        return elements.get(index);
    }

    public void set(int index, Value value, int line) {
        if (index < 0 || index >= elements.size())
            throw new NovaIndexOutOfBoundsException(index, elements.size(), line);
        elements.set(index, value);
    }

    public int length() {
        return elements.size();
    }

    @Override
    public boolean asBoolean() {
        return !elements.isEmpty();
    }

    @Override
    public Value equal(Value other) {
        if (!(other instanceof ArrayValue a))
            return new BooleanValue(false);
        if (this.elements.size() != a.elements.size())
            return new BooleanValue(false);
        for (int i = 0; i < elements.size(); i++) {
            if (!elements.get(i).equal(a.elements.get(i)).asBoolean())
                return new BooleanValue(false);
        }
        return new BooleanValue(true);
    }

    @Override
    public Value add(Value other) {
        if (!(other instanceof ArrayValue a))
            throw new TypeError(
                "Cannot concatenate array with " + other.getTypeName(), 0);
        List<Value> result = new ArrayList<>(elements);
        result.addAll(a.elements);
        return new ArrayValue(result);
    }

    @Override
    public Value multiply(Value other) {
        int times = other.asInt();
        if (times < 0)
            throw new TypeError("Cannot repeat an array a negative number of times", 0);
        List<Value> result = new ArrayList<>();
        for (int i = 0; i < times; i++)
            result.addAll(elements);
        return new ArrayValue(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            Value v = elements.get(i);
            if (v instanceof StringValue)
                sb.append("\"").append(v).append("\"");
            else
                sb.append(v);
            if (i < elements.size() - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}