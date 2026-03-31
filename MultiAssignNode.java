import java.util.List;

public class MultiAssignNode extends Node
{
    List<String> names;
    Node value;
    boolean isLet;   // true = declare (let), false = reassign

    MultiAssignNode(List<String> names, Node value, boolean isLet)
    {
        this.names = names;
        this.value = value;
        this.isLet = isLet;
    }
}