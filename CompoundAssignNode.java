public class CompoundAssignNode extends Node {
    String name;
    Token op;    // the operator token (+=, -=, etc.)
    Node value;

    CompoundAssignNode(String name, Token op, Node value) {
        this.name  = name;
        this.op    = op;
        this.value = value;
    }
}