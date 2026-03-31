public class IndexAccessNode extends Node {
    Node array;   // the array expression being indexed
    Node index;   // the index expression

    IndexAccessNode(Node array, Node index) {
        this.array = array;
        this.index = index;
    }
}