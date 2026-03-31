import java.util.List;

@FunctionalInterface
public interface BuiltinFunction {
    Value call(List<Value> args, int line);
}