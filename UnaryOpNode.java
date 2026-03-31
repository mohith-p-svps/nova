class UnaryOpNode extends Node {
    Token op;
    Node expr;

    UnaryOpNode(Token op, Node expr) {
        this.op = op;
        this.expr = expr;
    }
}