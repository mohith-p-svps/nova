public class BreakException extends NovaRuntimeException {
    BreakException(int line) {
        super("'break' used outside of a loop", line);
    }
}