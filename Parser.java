import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int pos;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token currentToken() {
        return (pos < tokens.size()) ? tokens.get(pos) : new Token(TokenType.EOF, "");
    }

    private Token peek() {
        return (pos + 1 < tokens.size()) ? tokens.get(pos + 1) : new Token(TokenType.EOF, "");
    }

    private void advance() {
        pos++;
    }

    private int line() {
        return currentToken().line;
    }

    // Helper: set line on a node and return it
    private <T extends Node> T at(T node, int line) {
        node.line = line;
        return node;
    }

    private void expect(TokenType type, String description) {
        if (currentToken().type != type)
            throw new ParseException("Expected " + description
                + " but got '" + currentToken().value + "'", line());
    }

    // Apply zero or more [index] accesses to any expression node
    private Node applyPostfixIndex(Node node, int startLine) {
        while (currentToken().type == TokenType.LEFT_BRACKET) {
            advance(); // skip [
            Node index = condition();
            expect(TokenType.RIGHT_BRACKET, "']' to close index access");
            advance(); // skip ]
            node = at(new IndexAccessNode(node, index), startLine);
        }
        return node;
    }

    private Node factor() {
        Token token = currentToken();

        if (token.type == TokenType.MINUS) {
            advance();
            return at(new UnaryOpNode(token, factor()), token.line);
        }

        if (token.type == TokenType.NUMBER) {
            advance();
            return at(new NumberNode(token.value), token.line);
        }

        if (token.type == TokenType.DOUBLE) {
            advance();
            return at(new DoubleNode(token.value), token.line);
        }

        if (token.type == TokenType.CHAR) {
            advance();
            return at(new CharNode(token.value.charAt(0)), token.line);
        }

        if (token.type == TokenType.STRING) {
            advance();
            return at(new StringNode(token.value), token.line);
        }

        if (token.type == TokenType.TRIPLE_STRING) {
            advance();
            return at(new TripleStringNode(token.value), token.line);
        }

        if (token.type == TokenType.INTERPOLATED_STRING) {
            advance();
            return at(new InterpolatedStringNode(token.value), token.line);
        }

        if (token.type == TokenType.TRUE) {
            advance();
            return at(new BooleanNode(true), token.line);
        }

        if (token.type == TokenType.FALSE) {
            advance();
            return at(new BooleanNode(false), token.line);
        }

        if (token.type == TokenType.NULL) {
            advance();
            return at(new NullNode(), token.line);
        }

        // Map literal: {"key": value, ...}
        if (token.type == TokenType.LEFT_BRACE) {
            int startLine = token.line;
            advance(); // skip {

            List<String> keys   = new ArrayList<>();
            List<Node>   values = new ArrayList<>();

            if (currentToken().type != TokenType.RIGHT_BRACE) {
                while (true) {
                    // Key must be a string literal
                    if (currentToken().type != TokenType.STRING)
                        throw new ParseException(
                            "Map key must be a string literal, got '"
                            + currentToken().value + "'", line());
                    keys.add(currentToken().value);
                    advance(); // skip key

                    expect(TokenType.COLON, "':' after map key");
                    advance(); // skip :

                    values.add(condition());

                    if (currentToken().type == TokenType.COMMA) {
                        advance();
                    } else break;
                }
            }

            expect(TokenType.RIGHT_BRACE, "'}' to close map literal");
            advance(); // skip }
            return at(new MapNode(keys, values), startLine);
        }

        // Array literal: [expr, expr, ...]
        if (token.type == TokenType.LEFT_BRACKET) {
            int startLine = token.line;
            advance(); // skip [
            List<Node> elements = new ArrayList<>();

            if (currentToken().type != TokenType.RIGHT_BRACKET) {
                while (true) {
                    elements.add(condition());
                    if (currentToken().type == TokenType.COMMA) {
                        advance();
                    } else break;
                }
            }

            expect(TokenType.RIGHT_BRACKET, "']' to close array literal");
            advance(); // skip ]
            return at(new ArrayNode(elements), startLine);
        }

        if (token.type == TokenType.IDENTIFIER) {
            Token next = peek();

            // Module call: moduleName.funcName(args)
            if (next.type == TokenType.DOT) {
                String moduleName = token.value;
                int startLine = token.line;
                advance(); // skip moduleName
                advance(); // skip .

                String funcName = currentToken().value;
                advance(); // skip funcName

                expect(TokenType.LEFT_PAREN, "'(' after module function name");
                advance(); // skip (

                List<Node> args = new ArrayList<>();
                if (currentToken().type != TokenType.RIGHT_PAREN) {
                    while (true) {
                        args.add(condition());
                        if (currentToken().type == TokenType.COMMA) {
                            advance();
                        } else break;
                    }
                }

                expect(TokenType.RIGHT_PAREN, "')' to close module function call");
                advance(); // skip )
                Node moduleCall = at(new ModuleCallNode(moduleName, funcName, args), startLine);
                return applyPostfixIndex(moduleCall, startLine);
            }

            // Function call: name(args)
            if (next.type == TokenType.LEFT_PAREN) {
                String name = token.value;
                int startLine = token.line;
                advance(); // name
                advance(); // (

                List<Node> args = new ArrayList<>();
                if (currentToken().type != TokenType.RIGHT_PAREN) {
                    while (true) {
                        args.add(condition());
                        if (currentToken().type == TokenType.COMMA) {
                            advance();
                        } else break;
                    }
                }

                expect(TokenType.RIGHT_PAREN, "')' to close function call");
                advance(); // )
                Node funcCall = at(new FunctionCallNode(name, args), startLine);
                return applyPostfixIndex(funcCall, startLine);
            }

            // Index access: arr[i] (possibly chained)
            if (next.type == TokenType.LEFT_BRACKET) {
                int startLine = token.line;
                Node arrayNode = at(new VariableNode(token.value), token.line);
                advance(); // skip identifier
                return applyPostfixIndex(arrayNode, startLine);
            }

            advance();
            return at(new VariableNode(token.value), token.line);
        }

        if (token.type == TokenType.LEFT_PAREN) {
            int startLine = token.line;
            advance();
            Node node = condition();
            expect(TokenType.RIGHT_PAREN, "')'");
            advance();
            return applyPostfixIndex(node, startLine);
        }

        throw new ParseException("Unexpected token: '" + token.value + "'", token.line);
    }

    private Node term() {
        Node node = factor();

        while (currentToken().type == TokenType.MULTIPLY ||
               currentToken().type == TokenType.DIVIDE   ||
               currentToken().type == TokenType.MODULO) {
            Token op = currentToken();
            advance();
            node = at(new BinaryOpNode(node, op, factor()), op.line);
        }

        return node;
    }

    private Node expression() {
        Node node = term();

        while (currentToken().type == TokenType.PLUS ||
               currentToken().type == TokenType.MINUS) {
            Token op = currentToken();
            advance();
            node = at(new BinaryOpNode(node, op, term()), op.line);
        }

        return node;
    }

    private Node comparison() {
        Node node = expression();

        while (Set.of(
            TokenType.GREATER, TokenType.LESS,
            TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
            TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL
        ).contains(currentToken().type)) {
            Token op = currentToken();
            advance();
            node = at(new BinaryOpNode(node, op, expression()), op.line);
        }

        return node;
    }

    private Node condition() {
        if (currentToken().type == TokenType.NOT) {
            Token op = currentToken();
            advance();
            return at(new UnaryOpNode(op, condition()), op.line);
        }

        Node node = comparison();

        while (currentToken().type == TokenType.AND ||
               currentToken().type == TokenType.OR) {
            Token op = currentToken();
            advance();
            node = at(new BinaryOpNode(node, op, condition()), op.line);
        }

        return node;
    }

    private List<Node> block() {
        expect(TokenType.LEFT_BRACE, "'{'");
        advance(); // {

        List<Node> nodes = new ArrayList<>();
        while (currentToken().type != TokenType.RIGHT_BRACE) {
            if (currentToken().type == TokenType.EOF)
                throw new ParseException("Unclosed block — expected '}'", line());
            nodes.add(statement());
        }

        advance(); // }
        return nodes;
    }

    private Node parseFunction() {
        int startLine = line();
        advance(); // fn

        String name = currentToken().value;
        advance(); // name

        expect(TokenType.LEFT_PAREN, "'(' after function name");
        advance(); // (

        List<String> params = new ArrayList<>();
        if (currentToken().type != TokenType.RIGHT_PAREN) {
            while (true) {
                params.add(currentToken().value);
                advance();
                if (currentToken().type == TokenType.COMMA) advance();
                else break;
            }
        }

        expect(TokenType.RIGHT_PAREN, "')' to close parameter list");
        advance(); // )

        return at(new FunctionDefNode(name, params, block()), startLine);
    }

    private Node statement() {
        Token token = currentToken();

        if (token.type == TokenType.IF)     return parseIf();
        if (token.type == TokenType.WHILE)  return parseWhile();
        if (token.type == TokenType.FOR)    return parseFor();
        if (token.type == TokenType.FN)     return parseFunction();

        if (token.type == TokenType.BREAK) {
            advance();
            return at(new BreakNode(), token.line);
        }

        if (token.type == TokenType.CONTINUE) {
            advance();
            return at(new ContinueNode(), token.line);
        }

        if (token.type == TokenType.USE) {
            int startLine = token.line;
            advance(); // skip use
            String moduleName = currentToken().value;
            advance(); // skip module name

            // Optional alias: use math as m
            String alias = null;
            if (currentToken().type == TokenType.AS) {
                advance(); // skip as
                alias = currentToken().value;
                advance(); // skip alias name
            }

            return at(new UseNode(moduleName, alias), startLine);
        }

        if (token.type == TokenType.RETURN) {
            int startLine = token.line;
            advance();
            return at(new ReturnNode(condition()), startLine);
        }

        if (token.type == TokenType.LET) {
            int startLine = token.line;
            advance();

            String name = currentToken().value;
            advance();

            // let a, b, c = value  — multi-declaration
            if (currentToken().type == TokenType.COMMA) {
                List<String> names = new ArrayList<>();
                names.add(name);
                while (currentToken().type == TokenType.COMMA) {
                    advance(); // skip ,
                    names.add(currentToken().value);
                    advance(); // skip identifier
                }
                expect(TokenType.EQUALS, "'=' after variable names in let");
                advance(); // skip =
                return at(new MultiAssignNode(names, condition(), true), startLine);
            }

            // let arr[i] = value
            if (currentToken().type == TokenType.LEFT_BRACKET) {
                advance(); // skip [
                Node index = condition();
                expect(TokenType.RIGHT_BRACKET, "']'");
                advance(); // skip ]
                expect(TokenType.EQUALS, "'='");
                advance(); // skip =
                return at(new IndexAssignNode(name, index, condition()), startLine);
            }

            expect(TokenType.EQUALS, "'=' after variable name in let");
            advance();
            return at(new AssignmentNode(name, condition()), startLine);
        }

        // Reassignment: x = expr  or  arr[i] = expr  or  x += expr  or  x++
        if (token.type == TokenType.IDENTIFIER) {
            Token next = peek();

            // arr[i] = expr
            if (next.type == TokenType.LEFT_BRACKET) {
                String name = token.value;
                int startLine = token.line;
                advance(); // skip identifier
                advance(); // skip [
                Node index = condition();
                expect(TokenType.RIGHT_BRACKET, "']'");
                advance(); // skip ]

                if (currentToken().type == TokenType.EQUALS) {
                    advance(); // skip =
                    return at(new IndexAssignNode(name, index, condition()), startLine);
                }

                // Not an assignment — backtrack and fall through
                pos -= 3;
                return condition();
            }

            // x = expr  or  a, b, c = expr
            if (next.type == TokenType.EQUALS) {
                String name = token.value;
                int startLine = token.line;
                advance(); // skip identifier
                advance(); // skip =
                return at(new ReassignNode(name, condition()), startLine);
            }

            // a, b, c = expr  — multi-reassignment
            if (next.type == TokenType.COMMA) {
                List<String> names = new ArrayList<>();
                names.add(token.value);
                int startLine = token.line;
                advance(); // skip first identifier
                while (currentToken().type == TokenType.COMMA) {
                    advance(); // skip ,
                    names.add(currentToken().value);
                    advance(); // skip identifier
                }
                if (currentToken().type == TokenType.EQUALS) {
                    advance(); // skip =
                    return at(new MultiAssignNode(names, condition(), false), startLine);
                }
                // Not a multi-assign — not supported, fall through to error
                throw new ParseException("Expected '=' after variable list", startLine);
            }

            // x += expr, x -= expr, x *= expr, x /= expr, x %= expr
            if (next.type == TokenType.PLUS_EQUALS  || next.type == TokenType.MINUS_EQUALS ||
                next.type == TokenType.MULTIPLY_EQUALS || next.type == TokenType.DIVIDE_EQUALS ||
                next.type == TokenType.MODULO_EQUALS) {
                String name = token.value;
                int startLine = token.line;
                advance(); // skip identifier
                Token op = currentToken();
                advance(); // skip operator
                return at(new CompoundAssignNode(name, op, condition()), startLine);
            }

            // x++ or x--
            if (next.type == TokenType.PLUS_PLUS || next.type == TokenType.MINUS_MINUS) {
                String name = token.value;
                int startLine = token.line;
                advance(); // skip identifier
                boolean inc = currentToken().type == TokenType.PLUS_PLUS;
                advance(); // skip ++ or --
                return at(new IncrementNode(name, inc), startLine);
            }
        }

        if (token.type == TokenType.PRINT) {
            int startLine = token.line;
            advance();
            Node expr = condition();

            boolean newline = false;
            if (currentToken().type == TokenType.COMMA) {
                advance();
                if (currentToken().type == TokenType.TRUE) {
                    newline = true;
                    advance();
                } else if (currentToken().type == TokenType.FALSE) {
                    newline = false;
                    advance();
                } else {
                    throw new ParseException(
                        "Expected 'true' or 'false' after ',' in print statement", line());
                }
            }

            return at(new PrintNode(expr, newline), startLine);
        }

        return condition();
    }

    private Node parseIf() {
        int startLine = line();
        advance();

        Node cond = condition();
        List<Node> body = block();
        IfNode node = at(new IfNode(cond, body), startLine);

        while (currentToken().type == TokenType.ELIF) {
            advance();
            node.elifConditions.add(condition());
            node.elifBodies.add(block());
        }

        if (currentToken().type == TokenType.ELSE) {
            advance();
            node.elseBody = block();
        }

        return node;
    }

    private Node parseWhile() {
        int startLine = line();
        advance();
        return at(new WhileNode(condition(), block()), startLine);
    }

    private Node parseFor() {
        int startLine = line();
        advance(); // skip 'for'

        String var = currentToken().value;
        advance(); // skip variable name

        // for x in arr { } — for-in loop
        if (currentToken().type == TokenType.IN) {
            advance(); // skip 'in'
            Node iterable = condition();
            return at(new ForInNode(var, iterable, block()), startLine);
        }

        // for x = start to end { } — range loop
        expect(TokenType.EQUALS, "'=' or 'in' in for loop");
        advance(); // skip =

        Node start = condition();

        if (currentToken().type != TokenType.TO)
            throw new ParseException("Expected 'to' in for loop", line());
        advance(); // skip 'to'

        Node end = condition();
        return at(new ForNode(var, start, end, block()), startLine);
    }

    public List<Node> parse() {
        List<Node> nodes = new ArrayList<>();
        while (currentToken().type != TokenType.EOF) {
            nodes.add(statement());
        }
        return nodes;
    }
}