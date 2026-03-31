import java.util.*;
class ForNode extends Node {
    String varName;
    Node start;
    Node end;
    List<Node> body;

    ForNode(String varName, Node start, Node end, List<Node> body) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.body = body;
    }
}