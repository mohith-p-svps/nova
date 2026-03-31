public class ContinueException extends NovaRuntimeException {
    ContinueException(int line) {
        super("'continue' used outside of a loop", line);
    }
}