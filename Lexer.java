import java.util.*;

public class Lexer {
    String input;
    int pos;
    private int line = 1;

    Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    private char currentChar() {
        if (pos >= input.length()) return '\0';
        return input.charAt(pos);
    }

    private void advance() {
        if (currentChar() == '\n') line++;
        pos++;
    }

    private Token number() {
        int startLine = line;
        StringBuilder num = new StringBuilder();
        boolean isDouble = false;

        while (Character.isDigit(currentChar()) || currentChar() == '.') {
            if (currentChar() == '.') {
                if (isDouble) break;
                isDouble = true;
            }
            num.append(currentChar());
            advance();
        }

        if (!isDouble)
            return new Token(TokenType.NUMBER, num.toString(), startLine);

        return new Token(TokenType.DOUBLE, num.toString(), startLine);
    }

    private Token tripleString() {
        int startLine = line;
        advance(); advance(); advance(); // skip """
        StringBuilder sb = new StringBuilder();

        while (pos < input.length()) {
            if (currentChar() == '"' && pos + 2 < input.length()
                    && input.charAt(pos+1) == '"' && input.charAt(pos+2) == '"') {
                advance(); advance(); advance(); // skip closing """
                return new Token(TokenType.TRIPLE_STRING, sb.toString(), startLine);
            }
            sb.append(currentChar());
            advance();
        }
        throw new LexerException("Unclosed triple-quoted string", startLine);
    }

    // Tokenize interpolated string $"Hello {name}!" as INTERPOLATED_STRING
    // Store the raw template — the Interpreter will evaluate expressions inside {}
    private Token interpolatedString() {
        int startLine = line;
        advance(); // skip opening "
        StringBuilder sb = new StringBuilder();

        while (currentChar() != '"' && currentChar() != '\0') {
            if (currentChar() == '\\') {
                advance();
                switch (currentChar()) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '{':  sb.append('{');  break;
                    default:   sb.append('\\'); sb.append(currentChar());
                }
            } else {
                sb.append(currentChar());
            }
            advance();
        }

        if (currentChar() != '"')
            throw new LexerException("Unclosed interpolated string", startLine);
        advance(); // skip closing "
        return new Token(TokenType.INTERPOLATED_STRING, sb.toString(), startLine);
    }

    private Token string() {
        int startLine = line;
        advance(); // skip opening "
        StringBuilder sb = new StringBuilder();

        while (currentChar() != '"' && currentChar() != '\0') {
            if (currentChar() == '\\') {
                advance();
                switch (currentChar()) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    default:
                        throw new LexerException(
                            "Unknown escape sequence: \\" + currentChar(), line);
                }
            } else {
                sb.append(currentChar());
            }
            advance();
        }

        if (currentChar() != '"')
            throw new LexerException("Unclosed string literal", startLine);

        advance(); // skip closing "
        return new Token(TokenType.STRING, sb.toString(), startLine);
    }

    private Token character() {
        int startLine = line;
        advance(); // skip '

        char c = currentChar();
        advance();

        if (currentChar() != '\'')
            throw new LexerException("Unclosed character literal", startLine);

        advance();
        return new Token(TokenType.CHAR, String.valueOf(c), startLine);
    }

    private Token identifier() {
        int startLine = line;
        StringBuilder word = new StringBuilder();

        while (Character.isLetterOrDigit(currentChar()) || currentChar() == '_') {
            word.append(currentChar());
            advance();
        }

        String w = word.toString();

        switch (w) {
            case "for":      return new Token(TokenType.FOR,      w, startLine);
            case "to":       return new Token(TokenType.TO,       w, startLine);
            case "in":       return new Token(TokenType.IN,       w, startLine);
            case "while":    return new Token(TokenType.WHILE,    w, startLine);
            case "and":      return new Token(TokenType.AND,      w, startLine);
            case "or":       return new Token(TokenType.OR,       w, startLine);
            case "not":      return new Token(TokenType.NOT,      w, startLine);
            case "if":       return new Token(TokenType.IF,       w, startLine);
            case "elif":     return new Token(TokenType.ELIF,     w, startLine);
            case "else":     return new Token(TokenType.ELSE,     w, startLine);
            case "let":      return new Token(TokenType.LET,      w, startLine);
            case "print":    return new Token(TokenType.PRINT,    w, startLine);
            case "fn":       return new Token(TokenType.FN,       w, startLine);
            case "send":     return new Token(TokenType.RETURN,   w, startLine);
            case "use":      return new Token(TokenType.USE,      w, startLine);
            case "as":       return new Token(TokenType.AS,       w, startLine);
            case "true":     return new Token(TokenType.TRUE,     w, startLine);
            case "false":    return new Token(TokenType.FALSE,    w, startLine);
            case "null":     return new Token(TokenType.NULL,     w, startLine);
            case "break":    return new Token(TokenType.BREAK,    w, startLine);
            case "continue": return new Token(TokenType.CONTINUE, w, startLine);
            default:         return new Token(TokenType.IDENTIFIER, w, startLine);
        }
    }

    private void skipComment() {
        while (currentChar() != '\n' && currentChar() != '\0')
            advance();
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currentChar() != '\0') {

            if (Character.isWhitespace(currentChar())) {
                advance();
                continue;
            }

            if (currentChar() == '#') {
                skipComment();
                continue;
            }

            if (Character.isDigit(currentChar())) {
                tokens.add(number());
                continue;
            }

            if (Character.isLetter(currentChar())) {
                tokens.add(identifier());
                continue;
            }

            if (currentChar() == '\'') {
                tokens.add(character());
                continue;
            }

            if (currentChar() == '$' && pos + 1 < input.length() && input.charAt(pos + 1) == '"') {
                advance(); // skip $
                // Check for triple interpolated $"""..."""
                if (pos + 2 < input.length() && input.charAt(pos+1) == '"' && input.charAt(pos+2) == '"') {
                    tokens.add(tripleString()); // triple strings are always literal
                } else {
                    tokens.add(interpolatedString());
                }
                continue;
            }

            if (currentChar() == '"') {
                // Check for triple-quoted string """..."""
                if (pos + 2 < input.length() && input.charAt(pos+1) == '"' && input.charAt(pos+2) == '"') {
                    tokens.add(tripleString());
                } else {
                    tokens.add(string());
                }
                continue;
            }

            int tokenLine = line;
            switch (currentChar()) {
                case '+':
                    advance();
                    if (currentChar() == '+') {
                        advance();
                        tokens.add(new Token(TokenType.PLUS_PLUS, "++", tokenLine));
                    } else if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.PLUS_EQUALS, "+=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.PLUS, "+", tokenLine));
                    }
                    continue;

                case '-':
                    advance();
                    if (currentChar() == '-') {
                        advance();
                        tokens.add(new Token(TokenType.MINUS_MINUS, "--", tokenLine));
                    } else if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.MINUS_EQUALS, "-=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.MINUS, "-", tokenLine));
                    }
                    continue;

                case '*':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.MULTIPLY_EQUALS, "*=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.MULTIPLY, "*", tokenLine));
                    }
                    continue;

                case '/':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.DIVIDE_EQUALS, "/=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.DIVIDE, "/", tokenLine));
                    }
                    continue;

                case '%':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.MODULO_EQUALS, "%=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.MODULO, "%", tokenLine));
                    }
                    continue;

                case '(': tokens.add(new Token(TokenType.LEFT_PAREN,    "(", tokenLine)); break;
                case ')': tokens.add(new Token(TokenType.RIGHT_PAREN,   ")", tokenLine)); break;
                case '{': tokens.add(new Token(TokenType.LEFT_BRACE,    "{", tokenLine)); break;
                case '}': tokens.add(new Token(TokenType.RIGHT_BRACE,   "}", tokenLine)); break;
                case '[': tokens.add(new Token(TokenType.LEFT_BRACKET,  "[", tokenLine)); break;
                case ']': tokens.add(new Token(TokenType.RIGHT_BRACKET, "]", tokenLine)); break;
                case ',': tokens.add(new Token(TokenType.COMMA,  ",", tokenLine)); break;
                case ':': tokens.add(new Token(TokenType.COLON,  ":", tokenLine)); break;
                case '.': tokens.add(new Token(TokenType.DOT,    ".", tokenLine)); break;

                case '>':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.GREATER, ">", tokenLine));
                    }
                    continue;

                case '<':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.LESS_EQUAL, "<=", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.LESS, "<", tokenLine));
                    }
                    continue;

                case '=':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.EQUAL_EQUAL, "==", tokenLine));
                    } else {
                        tokens.add(new Token(TokenType.EQUALS, "=", tokenLine));
                    }
                    continue;

                case '!':
                    advance();
                    if (currentChar() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.NOT_EQUAL, "!=", tokenLine));
                    } else {
                        throw new LexerException("Unexpected character: '!'", tokenLine);
                    }
                    continue;

                default:
                    throw new LexerException(
                        "Unknown character: '" + currentChar() + "'", tokenLine);
            }

            advance();
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}