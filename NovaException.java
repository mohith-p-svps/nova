public abstract class NovaException extends RuntimeException {
    private final int line;

    public NovaException(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    // Formatted error output: [line N] ExceptionType: message
    public String format() {
        return "[line " + line + "] " + getClass().getSimpleName() + ": " + getMessage();
    }
}