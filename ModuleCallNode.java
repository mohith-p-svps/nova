import java.util.*;

public class ModuleCallNode extends Node {
    String moduleName;
    String funcName;
    List<Node> args;

    ModuleCallNode(String moduleName, String funcName, List<Node> args) {
        this.moduleName = moduleName;
        this.funcName = funcName;
        this.args = args;
    }
}