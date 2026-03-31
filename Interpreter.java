import java.util.*;
import java.math.*;

public class Interpreter {

    private Scope globalScope = new Scope();
    private Map<String, FunctionDefNode> functions = new HashMap<>();
    private Map<String, Map<String, BuiltinFunction>> modules = new HashMap<>();

    private Value evaluate(Node node, Scope scope) {

        if (node instanceof NumberNode n) {
            try {
                return new IntValue(Integer.parseInt(n.value));
            } catch (Exception e1) {
                try {
                    return new LongValue(Long.parseLong(n.value));
                } catch (Exception e2) {
                    return new BigIntValue(new BigInteger(n.value));
                }
            }
        }

        if (node instanceof DoubleNode d) {
            String digits = d.raw.replace(".", "").replace("-", "");
            int sigDigits = digits.replaceFirst("^0+", "").length();
            if (sigDigits > 15) {
                return new BigDecimalValue(new java.math.BigDecimal(d.raw));
            }
            return new DoubleValue(Double.parseDouble(d.raw));
        }

        if (node instanceof CharNode c)
            return new CharValue(c.value);

        if (node instanceof StringNode s)
            return new StringValue(s.value);

        if (node instanceof TripleStringNode t)
            return new StringValue(t.value);

        if (node instanceof InterpolatedStringNode interp)
            return evaluateInterpolation(interp.template, scope, interp.line);

        if (node instanceof BooleanNode b)
            return new BooleanValue(b.value);

        if (node instanceof NullNode)
            return new NullValue();

        if (node instanceof ArrayNode a) {
            List<Value> elements = new ArrayList<>();
            for (Node elem : a.elements)
                elements.add(evaluate(elem, scope));
            return new ArrayValue(elements);
        }

        // Map literal
        if (node instanceof MapNode m) {
            MapValue map = new MapValue();
            for (int i = 0; i < m.keys.size(); i++)
                map.set(m.keys.get(i), evaluate(m.values.get(i), scope));
            return map;
        }

        // Index access: arr[i] or map["key"]
        if (node instanceof IndexAccessNode ia) {
            Value container = evaluate(ia.array, scope);

            if (container instanceof ArrayValue arr) {
                int index = evaluate(ia.index, scope).asInt();
                return arr.get(index, ia.line);
            }

            if (container instanceof MapValue map) {
                String key = evaluate(ia.index, scope).toString();
                return map.get(key);
            }

            throw new TypeError(
                "Cannot index into " + container.getTypeName() + ", expected array or map",
                ia.line);
        }

        if (node instanceof VariableNode v) {
            return scope.get(v.name, v.line);
        }

        if (node instanceof UnaryOpNode u) {
            Value val = evaluate(u.expr, scope);
            try {
                if (u.op.type == TokenType.NOT)
                    return val.not();
                if (u.op.type == TokenType.MINUS)
                    return negate(val, u.line);
            } catch (TypeError e) {
                throw new TypeError(e.getMessage(), u.line);
            }
            throw new NovaRuntimeException("Unknown unary operator: " + u.op.value, u.line);
        }

        if (node instanceof BinaryOpNode b) {
            Value l = evaluate(b.left, scope);
            Value r = evaluate(b.right, scope);

            try {
                switch (b.op.type) {
                    case PLUS:          return l.add(r);
                    case MINUS:         return l.subtract(r);
                    case MULTIPLY:      return l.multiply(r);
                    case DIVIDE:        return l.divide(r);
                    case MODULO:        return l.modulo(r);
                    case GREATER:       return l.greater(r);
                    case LESS:          return l.less(r);
                    case EQUAL_EQUAL:   return l.equal(r);
                    case GREATER_EQUAL: return l.greater(r).or(l.equal(r));
                    case LESS_EQUAL:    return l.less(r).or(l.equal(r));
                    case NOT_EQUAL:     return l.equal(r).not();
                    case AND:           return l.and(r);
                    case OR:            return l.or(r);
                    default:
                        throw new NovaRuntimeException(
                            "Unknown operator: " + b.op.value, b.line);
                }
            } catch (TypeError e) {
                // Re-throw with correct line from the expression node
                throw new TypeError(e.getMessage(), b.line);
            }
        }

        if (node instanceof ModuleCallNode m) {
            Map<String, BuiltinFunction> module = modules.get(m.moduleName);
            if (module == null)
                throw new NovaRuntimeException(
                    "Module '" + m.moduleName + "' is not loaded. Add 'use " + m.moduleName + "' at the top.", m.line);
            BuiltinFunction func = module.get(m.funcName);
            if (func == null)
                throw new NovaRuntimeException(
                    "Function '" + m.funcName + "' not found in module '" + m.moduleName + "'", m.line);
            List<Value> args = new ArrayList<>();
            for (Node arg : m.args)
                args.add(evaluate(arg, scope));
            return func.call(args, m.line);
        }

        if (node instanceof FunctionCallNode f) {
            // First check if the name holds a FunctionValue in scope
            FunctionDefNode func = null;
            try {
                Value v = scope.get(f.name, f.line);
                if (v instanceof FunctionValue fv)
                    func = fv.func;
                else if (!(v instanceof NullValue))
                    throw new TypeError(
                        "'" + f.name + "' is not a function, got " + v.getTypeName(), f.line);
            } catch (UndefinedVariableException e) {
                // Not in scope — try the functions map
                func = functions.get(f.name);
            }

            if (func == null)
                throw new UndefinedFunctionException(f.name, f.line);

            if (f.args.size() != func.params.size())
                throw new ArgumentException(f.name, func.params.size(), f.args.size(), f.line);

            List<Value> argVals = new ArrayList<>();
            for (Node arg : f.args)
                argVals.add(evaluate(arg, scope));

            return callFunction(func, argVals, f.line);
        }

        throw new NovaRuntimeException(
            "Unknown expression node: " + node.getClass().getSimpleName(), node.line);
    }

    // Negate a value using its own type — avoids routing through IntValue(0).subtract()
    private Value negate(Value val, int line) {
        if (val instanceof IntValue)
            return new IntValue(-val.asInt());
        if (val instanceof LongValue)
            return new LongValue(-val.asLong());
        if (val instanceof DoubleValue)
            return new DoubleValue(-val.asDouble());
        if (val instanceof BigIntValue b)
            return new BigIntValue(b.getRaw().negate());
        if (val instanceof BigDecimalValue b)
            return new BigDecimalValue(b.getRaw().negate());
        throw new TypeError("Cannot negate " + val.getTypeName(), line);
    }

    // Call a function with pre-evaluated argument values
    public Value callFunction(FunctionDefNode func, List<Value> argVals, int line) {
        if (argVals.size() != func.params.size())
            throw new ArgumentException(func.name, func.params.size(), argVals.size(), line);

        Scope funcScope = new Scope();
        for (int i = 0; i < func.params.size(); i++)
            funcScope.define(func.params.get(i), argVals.get(i));

        Value result = new NullValue();
        try {
            executeBlock(func.body, funcScope, false);
        } catch (ReturnException r) {
            result = r.value;
        } catch (BreakException | ContinueException e) {
            throw new NovaRuntimeException(e.getMessage(), e.getLine());
        }
        return result;
    }

    // Evaluate string interpolation — replaces {expr} with evaluated values
    private Value evaluateInterpolation(String template, Scope scope, int line) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            if (template.charAt(i) == '{') {
                // find closing }
                int end = template.indexOf('}', i + 1);
                if (end == -1)
                    throw new NovaRuntimeException("Unclosed '{' in interpolated string", line);
                String exprSrc = template.substring(i + 1, end);
                // lex and parse the expression
                Lexer exprLexer = new Lexer(exprSrc);
                Parser exprParser = new Parser(exprLexer.tokenize());
                List<Node> exprAst = exprParser.parse();
                if (!exprAst.isEmpty())
                    result.append(evaluate(exprAst.get(0), scope).toString());
                i = end + 1;
            } else {
                result.append(template.charAt(i));
                i++;
            }
        }
        return new StringValue(result.toString());
    }

    public void execute(List<Node> nodes) {
        executeBlock(nodes, globalScope, true);
    }

    private void executeBlock(List<Node> nodes, Scope scope, boolean isTopLevel) {

        for (Node node : nodes) {

            // let x = value
            if (node instanceof AssignmentNode a) {
                scope.define(a.name, evaluate(a.value, scope));
            }

            // let a, b, c = value  or  a, b, c = value
            else if (node instanceof MultiAssignNode ma) {
                Value val = evaluate(ma.value, scope);
                for (String name : ma.names) {
                    if (ma.isLet) scope.define(name, val);
                    else          scope.assign(name, val, ma.line);
                }
            }

            // x = value
            else if (node instanceof ReassignNode r) {
                scope.assign(r.name, evaluate(r.value, scope), r.line);
            }

            // x += expr, x -= expr, x *= expr, x /= expr, x %= expr
            else if (node instanceof CompoundAssignNode ca) {
                Value current = scope.get(ca.name, ca.line);
                Value rhs = evaluate(ca.value, scope);
                Value result;
                switch (ca.op.type) {
                    case PLUS_EQUALS:     result = current.add(rhs);      break;
                    case MINUS_EQUALS:    result = current.subtract(rhs);  break;
                    case MULTIPLY_EQUALS: result = current.multiply(rhs);  break;
                    case DIVIDE_EQUALS:   result = current.divide(rhs);    break;
                    case MODULO_EQUALS:   result = current.modulo(rhs);    break;
                    default: throw new NovaRuntimeException("Unknown compound operator", ca.line);
                }
                scope.assign(ca.name, result, ca.line);
            }

            // x++ or x--
            else if (node instanceof IncrementNode inc) {
                Value current = scope.get(inc.name, inc.line);
                Value updated = inc.increment
                    ? current.add(new IntValue(1))
                    : current.subtract(new IntValue(1));
                scope.assign(inc.name, updated, inc.line);
            }

            // arr[i] = value  or  map["key"] = value
            else if (node instanceof IndexAssignNode ia) {
                Value container = scope.get(ia.arrayName, ia.line);

                if (container instanceof ArrayValue arr) {
                    int index = evaluate(ia.index, scope).asInt();
                    arr.set(index, evaluate(ia.value, scope), ia.line);
                } else if (container instanceof MapValue map) {
                    String key = evaluate(ia.index, scope).toString();
                    map.set(key, evaluate(ia.value, scope));
                } else {
                    throw new TypeError(
                        "Cannot index into " + container.getTypeName() + ", expected array or map",
                        ia.line);
                }
            }

            else if (node instanceof PrintNode p) {
                String output = evaluate(p.value, scope).toString();
                if (p.newline) System.out.println(output);
                else           System.out.print(output);
            }

            else if (node instanceof IfNode i) {
                if (evaluate(i.condition, scope).asBoolean()) {
                    executeBlock(i.body, new Scope(scope), false);
                } else {
                    boolean executed = false;
                    for (int idx = 0; idx < i.elifConditions.size(); idx++) {
                        if (evaluate(i.elifConditions.get(idx), scope).asBoolean()) {
                            executeBlock(i.elifBodies.get(idx), new Scope(scope), false);
                            executed = true;
                            break;
                        }
                    }
                    if (!executed && i.elseBody != null)
                        executeBlock(i.elseBody, new Scope(scope), false);
                }
            }

            else if (node instanceof WhileNode w) {
                outer:
                while (evaluate(w.condition, scope).asBoolean()) {
                    try {
                        executeBlock(w.body, new Scope(scope), false);
                    } catch (BreakException e) {
                        break outer;
                    } catch (ContinueException e) {
                        continue outer;
                    }
                }
            }

            else if (node instanceof ForNode f) {
                Value start = evaluate(f.start, scope);
                Value end   = evaluate(f.end, scope);

                Scope forScope = new Scope(scope);
                forScope.define(f.varName, start);

                if (start.less(end).asBoolean()) {
                    outer:
                    while (forScope.get(f.varName, f.line).less(end).asBoolean()) {
                        try {
                            executeBlock(f.body, new Scope(forScope), false);
                        } catch (BreakException e) {
                            break outer;
                        } catch (ContinueException e) {
                            // still increment before continuing
                        }
                        Value current = forScope.get(f.varName, f.line);
                        forScope.assign(f.varName, current.add(new IntValue(1)), f.line);
                    }
                } else {
                    outer:
                    while (forScope.get(f.varName, f.line).greater(end).asBoolean()) {
                        try {
                            executeBlock(f.body, new Scope(forScope), false);
                        } catch (BreakException e) {
                            break outer;
                        } catch (ContinueException e) {
                            // still decrement before continuing
                        }
                        Value current = forScope.get(f.varName, f.line);
                        forScope.assign(f.varName, current.subtract(new IntValue(1)), f.line);
                    }
                }
            }

            else if (node instanceof ForInNode fi) {
                Value iterVal = evaluate(fi.iterable, scope);
                if (!(iterVal instanceof ArrayValue arr))
                    throw new TypeError(
                        "for-in loop requires an array, got " + iterVal.getTypeName(), fi.line);

                outer:
                for (int i = 0; i < arr.length(); i++) {
                    Scope iterScope = new Scope(scope);
                    iterScope.define(fi.varName, arr.get(i, fi.line));
                    try {
                        executeBlock(fi.body, iterScope, false);
                    } catch (BreakException e) {
                        break outer;
                    } catch (ContinueException e) {
                        continue outer;
                    }
                }
            }

            else if (node instanceof BreakNode b) {
                throw new BreakException(b.line);
            }

            else if (node instanceof ContinueNode c) {
                throw new ContinueException(c.line);
            }

            else if (node instanceof UseNode u) {
                switch (u.moduleName) {
                    case "math":
                        modules.put(u.registerAs(), MathModule.load());
                        break;
                    case "arrays":
                        modules.put(u.registerAs(), ArraysModule.load(this));
                        break;
                    case "string":
                        modules.put(u.registerAs(), StringModule.load());
                        break;
                    case "os":
                        modules.put(u.registerAs(), OsModule.load());
                        break;
                    case "convert":
                        modules.put(u.registerAs(), ConvertModule.load());
                        break;
                    case "map":
                        modules.put(u.registerAs(), MapModule.load());
                        break;
                    case "json":
                        modules.put(u.registerAs(), JsonModule.load());
                        break;
                    case "net":
                        modules.put(u.registerAs(), NetModule.load());
                        break;
                    case "file":
                        modules.put(u.registerAs(), FileModule.load());
                        break;
                    case "datetime":
                        modules.put(u.registerAs(), DateTimeModule.load());
                        break;
                    default:
                        throw new NovaRuntimeException(
                            "Unknown module: '" + u.moduleName + "'", u.line);
                }
            }

            else if (node instanceof FunctionDefNode f) {
                functions.put(f.name, f);
                // Also store as a FunctionValue in scope so it can be passed around
                scope.define(f.name, new FunctionValue(f));
            }

            else if (node instanceof FunctionCallNode f) {
                evaluate(f, scope);
            }

            else if (node instanceof ModuleCallNode m) {
                evaluate(m, scope);
            }

            else if (node instanceof ReturnNode r) {
                if (isTopLevel)
                    throw new ReturnOutsideFunctionException(r.line);
                throw new ReturnException(evaluate(r.value, scope));
            }

            else {
                throw new NovaRuntimeException(
                    "Unknown statement: " + node.getClass().getSimpleName(), node.line);
            }
        }
    }
}