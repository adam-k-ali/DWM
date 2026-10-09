package com.adamkali.dwm.model.json.anim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Recursive-descent parser: {@code + - * /}, unary minus, parentheses, numbers, variables, the
 * constants {@code pi} and {@code deg} (180/pi), and the functions below.
 */
final class ExpressionParser {
    private static final Map<String, Float> CONSTANTS = Map.of(
            "pi", (float) Math.PI,
            "deg", (float) (180.0 / Math.PI)
    );
    private static final Map<String, Integer> ARITY = Map.of(
            "sin", 1, "cos", 1, "abs", 1, "min", 2, "max", 2, "clamp", 3, "lerp", 3
    );

    private final String source;
    private int pos;

    ExpressionParser(String source) {
        this.source = source;
    }

    Expression.Node parse() {
        Expression.Node node = expression();
        skipSpaces();
        if (pos < source.length()) {
            throw error("unexpected '" + source.charAt(pos) + "'");
        }
        return node;
    }

    private Expression.Node expression() {
        Expression.Node left = term();
        while (true) {
            skipSpaces();
            if (accept('+')) {
                left = new Expression.Bin('+', left, term());
            } else if (accept('-')) {
                left = new Expression.Bin('-', left, term());
            } else {
                return left;
            }
        }
    }

    private Expression.Node term() {
        Expression.Node left = unary();
        while (true) {
            skipSpaces();
            if (accept('*')) {
                left = new Expression.Bin('*', left, unary());
            } else if (accept('/')) {
                left = new Expression.Bin('/', left, unary());
            } else {
                return left;
            }
        }
    }

    private Expression.Node unary() {
        skipSpaces();
        if (accept('-')) {
            return new Expression.Neg(unary());
        }
        return primary();
    }

    private Expression.Node primary() {
        skipSpaces();
        if (pos >= source.length()) {
            throw error("unexpected end of expression");
        }
        char c = source.charAt(pos);
        if (accept('(')) {
            Expression.Node inner = expression();
            expect(')');
            return inner;
        }
        if (Character.isDigit(c) || c == '.') {
            return number();
        }
        if (Character.isLetter(c) || c == '_') {
            return identifier();
        }
        throw error("unexpected '" + c + "'");
    }

    private Expression.Node number() {
        int start = pos;
        while (pos < source.length() && (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
            pos++;
        }
        String text = source.substring(start, pos);
        try {
            return new Expression.Num(Float.parseFloat(text));
        } catch (NumberFormatException e) {
            throw error("invalid number '" + text + "'");
        }
    }

    private Expression.Node identifier() {
        int start = pos;
        while (pos < source.length()
                && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String name = source.substring(start, pos);
        skipSpaces();
        if (accept('(')) {
            Integer arity = ARITY.get(name);
            if (arity == null) {
                throw error("unknown function '" + name + "'");
            }
            List<Expression.Node> args = new ArrayList<>();
            skipSpaces();
            if (!accept(')')) {
                do {
                    args.add(expression());
                    skipSpaces();
                } while (accept(','));
                expect(')');
            }
            if (args.size() != arity) {
                throw error(name + " takes " + arity + " argument(s), got " + args.size());
            }
            return new Expression.Call(name, args);
        }
        Float constant = CONSTANTS.get(name);
        return constant != null ? new Expression.Num(constant) : new Expression.Var(name);
    }

    private boolean accept(char expected) {
        if (pos < source.length() && source.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        skipSpaces();
        if (!accept(expected)) {
            throw error("expected '" + expected + "'");
        }
    }

    private void skipSpaces() {
        while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
            pos++;
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("bad expression '" + source + "' at " + pos + ": " + message);
    }
}
