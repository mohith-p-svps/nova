import java.util.*;

public class FunctionValue extends Value {
    public final FunctionDefNode func;

    public FunctionValue(FunctionDefNode func) {
        this.func = func;
    }

    @Override
    public boolean asBoolean() {
        return true; // functions are always truthy
    }

    @Override
    public Value equal(Value other) {
        if (!(other instanceof FunctionValue f)) return new BooleanValue(false);
        return new BooleanValue(this.func == f.func); // identity comparison
    }

    @Override
    public String toString() {
        return "<fn " + func.name + ">";
    }
}