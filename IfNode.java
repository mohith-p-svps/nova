import java.util.*;
public class IfNode extends Node
{
    Node condition;
    List<Node> body;
    
    List<Node> elifConditions = new ArrayList<>();
    List<List<Node>> elifBodies = new ArrayList<>();
    
    List<Node> elseBody;
    
    IfNode(Node condition, List<Node> body) {
        this.condition = condition;
        this.body = body;
    }
}