public class TypeError extends NovaRuntimeException {
    public TypeError(String message, int line) {
        super(message, line);
    }
}