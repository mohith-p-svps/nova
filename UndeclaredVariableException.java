public class UndeclaredVariableException extends NovaRuntimeException {
    public UndeclaredVariableException(String name, int line) {
        super("Variable '" + name + "' has not been declared. Use 'let' to declare it first.", line);
    }
}