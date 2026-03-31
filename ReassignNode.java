public class ReassignNode extends Node {
    String name;
    Node value;

    ReassignNode(String name, Node value) {
        this.name = name;
        this.value = value;
    }
}