public class ReturnOutsideFunctionException extends NovaRuntimeException {
    public ReturnOutsideFunctionException(int line) {
        super("'send' can only be used inside a function", line);
    }
}