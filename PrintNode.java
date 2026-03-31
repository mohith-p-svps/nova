public class PrintNode extends Node
{
    Node value;
    boolean newline;
    
    PrintNode(Node value, boolean newline) {
        this.value = value;
        this.newline = newline;
    }
}