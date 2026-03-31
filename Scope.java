import java.util.*;

public class Scope {
    private final Map<String, Value> vars = new HashMap<>();
    private final Scope parent;

    public Scope() {
        this.parent = null;
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }

    // Define a NEW variable always in THIS scope (used by let)
    public void define(String name, Value value) {
        vars.put(name, value);
    }

    // Get a variable — walk up the chain until found
    public Value get(String name, int line) {
        if (vars.containsKey(name))
            return vars.get(name);
        if (parent != null)
            return parent.get(name, line);
        throw new UndefinedVariableException(name, line);
    }

    // Check if a variable exists anywhere in the chain
    public boolean has(String name) {
        if (vars.containsKey(name)) return true;
        if (parent != null) return parent.has(name);
        return false;
    }

    // Assign to an EXISTING variable — walk up and update where found
    public void assign(String name, Value value, int line) {
        if (vars.containsKey(name)) {
            vars.put(name, value);
            return;
        }
        if (parent != null && parent.has(name)) {
            parent.assign(name, value, line);
            return;
        }
        throw new UndeclaredVariableException(name, line);
    }

    // Returns all variables visible in this scope chain (for debugger)
    public Map<String, Value> getAllVariables() {
        Map<String, Value> all = new LinkedHashMap<>();
        if (parent != null) all.putAll(parent.getAllVariables());
        all.putAll(vars);   // local vars override parent
        return all;
    }

}