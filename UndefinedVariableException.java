public class UndefinedVariableException extends NovaRuntimeException {
    public UndefinedVariableException(String name, int line) {
        super("Undefined variable: '" + name + "'", line);
    }
}