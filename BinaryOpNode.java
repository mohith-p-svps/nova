public class BinaryOpNode extends Node
{
    Node left;
    Token op;
    Node right;
    
    BinaryOpNode(Node left, Token op, Node right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}