public class IndexAssignNode extends Node {
    String arrayName;  // name of the array variable
    Node index;        // the index expression
    Node value;        // the value to assign

    IndexAssignNode(String arrayName, Node index, Node value) {
        this.arrayName = arrayName;
        this.index = index;
        this.value = value;
    }
}