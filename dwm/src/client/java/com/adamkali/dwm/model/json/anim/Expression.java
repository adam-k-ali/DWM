package com.adamkali.dwm.model.json.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A parsed animation expression over named float variables. Parse once (codec), then
 * {@link #compile} against an ordered variable list for allocation-free evaluation.
 */
public record Expression(String source, Node root) {
    public static final Codec<Expression> CODEC = Codec.STRING.comapFlatMap(Expression::tryParse, Expression::source);

    /** Evaluates against a value array indexed like the list given to {@link #compile}. */
    public interface Compiled {
        float eval(float[] values);
    }

    public sealed interface Node permits Num, Var, Neg, Bin, Call {
    }

    public record Num(float value) implements Node {
    }

    public record Var(String name) implements Node {
    }

    public record Neg(Node operand) implements Node {
    }

    public record Bin(char op, Node left, Node right) implements Node {
    }

    public record Call(String function, List<Node> args) implements Node {
        public Call {
            args = List.copyOf(args);
        }
    }

    public static Expression parse(String source) {
        return new Expression(source, new ExpressionParser(source).parse());
    }

    static DataResult<Expression> tryParse(String source) {
        try {
            return DataResult.success(parse(source));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }

    /** Variable names this expression reads, in first-use order. */
    public Set<String> variables() {
        Set<String> names = new LinkedHashSet<>();
        collect(root, names);
        return names;
    }

    private static void collect(Node node, Set<String> names) {
        switch (node) {
            case Var v -> names.add(v.name());
            case Neg n -> collect(n.operand(), names);
            case Bin b -> {
                collect(b.left(), names);
                collect(b.right(), names);
            }
            case Call c -> c.args().forEach(arg -> collect(arg, names));
            case Num ignored -> {
            }
        }
    }

    /**
     * @param variableNames order defines the index of each variable in the array passed to
     *                      {@link Compiled#eval}
     * @throws IllegalArgumentException when the expression reads a variable not in the list
     */
    public Compiled compile(List<String> variableNames) {
        return compile(root, variableNames);
    }

    private static Compiled compile(Node node, List<String> names) {
        return switch (node) {
            case Num n -> {
                float value = n.value();
                yield values -> value;
            }
            case Var v -> {
                int index = names.indexOf(v.name());
                if (index < 0) {
                    throw new IllegalArgumentException(
                            "unknown variable '" + v.name() + "'; available: " + names);
                }
                yield values -> values[index];
            }
            case Neg n -> {
                Compiled operand = compile(n.operand(), names);
                yield values -> -operand.eval(values);
            }
            case Bin b -> {
                Compiled l = compile(b.left(), names);
                Compiled r = compile(b.right(), names);
                yield switch (b.op()) {
                    case '+' -> values -> l.eval(values) + r.eval(values);
                    case '-' -> values -> l.eval(values) - r.eval(values);
                    case '*' -> values -> l.eval(values) * r.eval(values);
                    default -> values -> l.eval(values) / r.eval(values);
                };
            }
            case Call c -> compileCall(c, names);
        };
    }

    private static Compiled compileCall(Call call, List<String> names) {
        List<Compiled> a = call.args().stream().map(arg -> compile(arg, names)).toList();
        return switch (call.function()) {
            case "sin" -> {
                Compiled x = a.get(0);
                yield values -> net.minecraft.util.Mth.sin(x.eval(values));
            }
            case "cos" -> {
                Compiled x = a.get(0);
                yield values -> net.minecraft.util.Mth.cos(x.eval(values));
            }
            case "abs" -> {
                Compiled x = a.get(0);
                yield values -> Math.abs(x.eval(values));
            }
            case "min" -> {
                Compiled x = a.get(0);
                Compiled y = a.get(1);
                yield values -> Math.min(x.eval(values), y.eval(values));
            }
            case "max" -> {
                Compiled x = a.get(0);
                Compiled y = a.get(1);
                yield values -> Math.max(x.eval(values), y.eval(values));
            }
            case "clamp" -> {
                Compiled x = a.get(0);
                Compiled lo = a.get(1);
                Compiled hi = a.get(2);
                yield values -> Math.max(lo.eval(values), Math.min(hi.eval(values), x.eval(values)));
            }
            case "lerp" -> {
                Compiled from = a.get(0);
                Compiled to = a.get(1);
                Compiled t = a.get(2);
                yield values -> {
                    float f = from.eval(values);
                    return f + (to.eval(values) - f) * t.eval(values);
                };
            }
            default -> throw new IllegalArgumentException("unknown function '" + call.function() + "'");
        };
    }
}
