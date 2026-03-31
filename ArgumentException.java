public class ArgumentException extends NovaRuntimeException {
    public ArgumentException(String funcName, int expected, int got, int line) {
        super("Function '" + funcName + "' expects " + expected
            + " argument" + (expected == 1 ? "" : "s")
            + " but got " + got, line);
    }
}