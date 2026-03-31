import java.util.*;

public class ForInNode extends Node {
    String varName;   // loop variable e.g. "fruit"
    Node iterable;    // the array expression
    List<Node> body;

    ForInNode(String varName, Node iterable, List<Node> body) {
        this.varName  = varName;
        this.iterable = iterable;
        this.body     = body;
    }
}