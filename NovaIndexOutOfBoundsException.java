public class NovaIndexOutOfBoundsException extends NovaRuntimeException {
    public NovaIndexOutOfBoundsException(int index, int length, int line) {
        super("Index " + index + " is out of bounds for length " + length, line);
    }
}