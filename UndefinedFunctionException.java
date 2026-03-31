public class UndefinedFunctionException extends NovaRuntimeException {
    public UndefinedFunctionException(String name, int line) {
        super("Undefined function: '" + name + "'", line);
    }
}