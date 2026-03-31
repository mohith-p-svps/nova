import java.util.*;
class WhileNode extends Node {
    Node condition;
    List<Node> body;

    WhileNode(Node condition, List<Node> body) {
        this.condition = condition;
        this.body = body;
    }
}