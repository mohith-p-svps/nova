import java.util.*;

/**
 * DebugInterpreter — extends Interpreter to call back into NovaDebugger
 * before each statement, enabling step-through debugging.
 *
 * It also exposes methods so the debugger can inspect scope state.
 */
public class DebugInterpreter extends Interpreter {

    private final NovaDebugger debugger;
    private final List<String> callStack = new ArrayList<>();

    public DebugInterpreter(NovaDebugger debugger) {
        super();
        this.debugger = debugger;
    }

    // ── Hook: called before each statement in execute() ───────────────────────

    @Override
    protected void beforeStatement(Node node) {
        if (node.line > 0) {
            debugger.onStatement(node.line);
        }
    }

    // ── Hook: called when entering/leaving a function ─────────────────────────

    @Override
    protected void onFunctionEnter(String name) {
        callStack.add(name + "()");
    }

    @Override
    protected void onFunctionExit(String name) {
        if (!callStack.isEmpty()) callStack.remove(callStack.size() - 1);
    }

    // ── Scope inspection for the debugger ────────────────────────────────────

    public Value debugGetVar(String name) {
        return currentScope().get(name, 0);
    }

    public Map<String, Value> debugGetAllVars() {
        return currentScope().getAllVariables();
    }

    public List<String> debugGetCallStack() {
        return Collections.unmodifiableList(callStack);
    }
}
