public class AssignmentNode extends Node
{
    String name;
    Node value;
    
    AssignmentNode(String name, Node value) {
        this.name = name;
        this.value = value;
    }
}