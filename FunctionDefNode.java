import java.util.*;
class FunctionDefNode extends Node {
    String name;
    List<String> params;
    List<Node> body;

    FunctionDefNode(String name, List<String> params, List<Node> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }
}