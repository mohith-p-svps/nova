public class UseNode extends Node {
    String moduleName;
    String alias; // null if no alias given, e.g. "use math as m" → alias = "m"

    UseNode(String moduleName, String alias) {
        this.moduleName = moduleName;
        this.alias = alias;
    }

    // Convenience — the name to register under (alias if given, otherwise module name)
    public String registerAs() {
        return alias != null ? alias : moduleName;
    }
}