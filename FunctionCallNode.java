import java.util.*;
class FunctionCallNode extends Node {
    String name;
    List<Node> args;

    FunctionCallNode(String name, List<Node> args) {
        this.name = name;
        this.args = args;
    }
}