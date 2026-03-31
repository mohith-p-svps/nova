public class IncrementNode extends Node {
    String name;
    boolean increment; // true = ++, false = --

    IncrementNode(String name, boolean increment) {
        this.name      = name;
        this.increment = increment;
    }
}