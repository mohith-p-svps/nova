import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * NovaFormatter — pretty-prints a Nova source file with consistent style:
 *   - 4-space indentation
 *   - Spaces around operators
 *   - Single blank line between top-level function definitions
 *   - No trailing whitespace
 *   - Consistent spacing in function calls, array literals, map literals
 *
 * Usage (via nova CLI):
 *   nova format myprogram.nova          — prints formatted code to stdout
 *   nova format myprogram.nova --write  — overwrites the file in place
 */
public class NovaFormatter {

    private final List<Node> ast;
    private final StringBuilder out = new StringBuilder();
    private int indent = 0;
    private boolean lastWasFnDef = false;

    public NovaFormatter(List<Node> ast) {
        this.ast = ast;
    }

    public String format() {
        for (int i = 0; i < ast.size(); i++) {
            Node node = ast.get(i);
            boolean isFn = node instanceof FunctionDefNode;

            // Blank line before function definitions (except the very first)
            if (isFn && i > 0) out.append("\n");

            formatNode(node);
            out.append("\n");

            lastWasFnDef = isFn;
        }
        // Remove trailing blank lines, keep exactly one trailing newline
        String result = out.toString().replaceAll("\\n{3,}", "\n\n").stripTrailing();
        return result + "\n";
    }

    // ── Node dispatcher ───────────────────────────────────────────────────────

    private void formatNode(Node node) {
        if      (node instanceof AssignmentNode  an)  formatAssignment(an);
        else if (node instanceof MultiAssignNode man)  formatMultiAssign(man);
        else if (node instanceof ReassignNode    rn)  formatReassign(rn);
        else if (node instanceof CompoundAssignNode ca) formatCompound(ca);
        else if (node instanceof IncrementNode   inc) formatIncrement(inc);
        else if (node instanceof PrintNode       pn)  formatPrint(pn);
        else if (node instanceof IfNode          ifn) formatIf(ifn);
        else if (node instanceof WhileNode       wn)  formatWhile(wn);
        else if (node instanceof ForNode         fn)  formatFor(fn);
        else if (node instanceof ForInNode       fin) formatForIn(fin);
        else if (node instanceof FunctionDefNode fdn) formatFunctionDef(fdn);
        else if (node instanceof ReturnNode      ret) formatReturn(ret);
        else if (node instanceof BreakNode       brk) { pad().append("break"); }
        else if (node instanceof ContinueNode    cnt) { pad().append("continue"); }
        else if (node instanceof UseNode         un)  formatUse(un);
        else if (node instanceof IndexAssignNode ian) formatIndexAssign(ian);
        else                                          pad().append(expr(node));
    }

    // ── Statements ────────────────────────────────────────────────────────────

    private void formatAssignment(AssignmentNode a) {
        pad().append("let ").append(a.name).append(" = ").append(expr(a.value));
    }

    private void formatMultiAssign(MultiAssignNode m) {
        pad();
        if (m.isLet) out.append("let ");
        out.append(String.join(", ", m.names));
        out.append(" = ").append(expr(m.value));
    }

    private void formatReassign(ReassignNode r) {
        pad().append(r.name).append(" = ").append(expr(r.value));
    }

    private void formatCompound(CompoundAssignNode c) {
        String op = switch (c.op.type) {
            case PLUS_EQUALS     -> "+=";
            case MINUS_EQUALS    -> "-=";
            case MULTIPLY_EQUALS -> "*=";
            case DIVIDE_EQUALS   -> "/=";
            case MODULO_EQUALS   -> "%=";
            default              -> "?=";
        };
        pad().append(c.name).append(" ").append(op).append(" ").append(expr(c.value));
    }

    private void formatIncrement(IncrementNode i) {
        pad().append(i.name).append(i.increment ? "++" : "--");
    }

    private void formatPrint(PrintNode p) {
        pad().append("print ").append(expr(p.value)).append(", ").append(p.newline);
    }

    private void formatReturn(ReturnNode r) {
        pad().append("send ").append(expr(r.value));
    }

    private void formatUse(UseNode u) {
        pad().append("use ").append(u.moduleName);
        if (u.alias != null) out.append(" as ").append(u.alias);
    }

    private void formatIndexAssign(IndexAssignNode ian) {
        pad().append(ian.arrayName).append("[").append(expr(ian.index)).append("] = ").append(expr(ian.value));
    }

    // ── Control flow ──────────────────────────────────────────────────────────

    private void formatIf(IfNode node) {
        pad().append("if ").append(expr(node.condition)).append(" {");
        formatBlock(node.body);
        pad().append("}");

        for (int i = 0; i < node.elifConditions.size(); i++) {
            out.append(" elif ").append(expr(node.elifConditions.get(i))).append(" {");
            formatBlock(node.elifBodies.get(i));
            pad().append("}");
        }

        if (node.elseBody != null && !node.elseBody.isEmpty()) {
            out.append(" else {");
            formatBlock(node.elseBody);
            pad().append("}");
        }
    }

    private void formatWhile(WhileNode node) {
        pad().append("while ").append(expr(node.condition)).append(" {");
        formatBlock(node.body);
        pad().append("}");
    }

    private void formatFor(ForNode node) {
        pad().append("for ").append(node.varName)
             .append(" = ").append(expr(node.start))
             .append(" to ").append(expr(node.end)).append(" {");
        formatBlock(node.body);
        pad().append("}");
    }

    private void formatForIn(ForInNode node) {
        pad().append("for ").append(node.varName)
             .append(" in ").append(expr(node.iterable)).append(" {");
        formatBlock(node.body);
        pad().append("}");
    }

    private void formatFunctionDef(FunctionDefNode node) {
        pad().append("fn ").append(node.name)
             .append("(").append(String.join(", ", node.params)).append(") {");
        formatBlock(node.body);
        pad().append("}");
    }

    private void formatBlock(List<Node> body) {
        out.append("\n");
        indent++;
        for (Node n : body) {
            formatNode(n);
            out.append("\n");
        }
        indent--;
    }

    // ── Expression formatter ──────────────────────────────────────────────────

    private String expr(Node node) {
        if (node instanceof NumberNode       n) return n.value;
        if (node instanceof DoubleNode       n) return n.raw;
        if (node instanceof CharNode         n) return "'" + n.value + "'";
        if (node instanceof BooleanNode      n) return String.valueOf(n.value);
        if (node instanceof NullNode         n) return "null";
        if (node instanceof VariableNode     n) return n.name;

        if (node instanceof StringNode n) {
            return "\"" + n.value.replace("\\", "\\\\").replace("\"", "\\\"")
                                 .replace("\n", "\\n").replace("\t", "\\t") + "\"";
        }

        if (node instanceof TripleStringNode n) {
            return "\"\"\"" + n.value + "\"\"\"";
        }

        if (node instanceof InterpolatedStringNode n) {
            return "$\"" + n.template + "\"";
        }

        if (node instanceof UnaryOpNode n) {
            String op = n.op.value.equals("-") ? "-" : "not ";
            return op + expr(n.expr);
        }

        if (node instanceof BinaryOpNode n) {
            String op = switch (n.op.type) {
                case PLUS          -> "+";
                case MINUS         -> "-";
                case MULTIPLY      -> "*";
                case DIVIDE        -> "/";
                case MODULO        -> "%";
                case EQUAL_EQUAL   -> "==";
                case NOT_EQUAL     -> "!=";
                case GREATER       -> ">";
                case LESS          -> "<";
                case GREATER_EQUAL -> ">=";
                case LESS_EQUAL    -> "<=";
                case AND           -> "and";
                case OR            -> "or";
                default            -> n.op.value;
            };
            String l = expr(n.left);
            String r = expr(n.right);
            return l + " " + op + " " + r;
        }

        if (node instanceof ArrayNode n) {
            if (n.elements.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < n.elements.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(expr(n.elements.get(i)));
            }
            return sb.append("]").toString();
        }

        if (node instanceof MapNode n) {
            if (n.keys.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < n.keys.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(n.keys.get(i)).append("\": ")
                  .append(expr(n.values.get(i)));
            }
            return sb.append("}").toString();
        }

        if (node instanceof FunctionCallNode n) {
            StringBuilder sb = new StringBuilder(n.name).append("(");
            for (int i = 0; i < n.args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(expr(n.args.get(i)));
            }
            return sb.append(")").toString();
        }

        if (node instanceof ModuleCallNode n) {
            StringBuilder sb = new StringBuilder(n.moduleName).append(".")
                                                              .append(n.funcName).append("(");
            for (int i = 0; i < n.args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(expr(n.args.get(i)));
            }
            return sb.append(")").toString();
        }

        if (node instanceof IndexAccessNode n) {
            return expr(n.array) + "[" + expr(n.index) + "]";
        }

        return "/* ? */";
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private StringBuilder pad() {
        out.append("    ".repeat(indent));
        return out;
    }

    // ── Static entry point ────────────────────────────────────────────────────

    public static String formatSource(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<Node> ast = parser.parse();
        return new NovaFormatter(ast).format();
    }
}