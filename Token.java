public class Token
{
    TokenType type;
    String value;
    int line;

    Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    // Convenience constructor for EOF and synthetic tokens (line 0)
    Token(TokenType type, String value) {
        this(type, value, 0);
    }

    public String toString() {
        return type + "(" + value + ")@" + line;
    }
}