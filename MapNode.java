import java.util.*;

public class MapNode extends Node {
    List<String> keys;
    List<Node> values;

    MapNode(List<String> keys, List<Node> values) {
        this.keys   = keys;
        this.values = values;
    }
}