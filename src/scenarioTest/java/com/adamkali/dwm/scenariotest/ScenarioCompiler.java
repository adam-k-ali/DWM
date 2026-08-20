package com.adamkali.dwm.scenariotest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScenarioCompiler {
    private static final Set<String> PRIMITIVES = Set.of("launchGame", "assertVisible", "click", "debugScreen");
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");

    private final ScenarioCatalog catalog;

    public ScenarioCompiler(ScenarioCatalog catalog) {
        this.catalog = catalog;
    }

    public ScenarioPlan compile(String testId) {
        ScenarioDocument test = catalog.requireTest(testId);
        List<ScenarioPlan.Step> expanded = new ArrayList<>();
        expand(test.steps(), test.source(), Map.of(), new ArrayDeque<>(), expanded);
        return new ScenarioPlan(test.id(), test.name(), expanded);
    }

    private void expand(
            List<ScenarioDocument.Invocation> invocations,
            String source,
            Map<String, Object> bindings,
            Deque<String> commandStack,
            List<ScenarioPlan.Step> output
    ) {
        for (ScenarioDocument.Invocation invocation : invocations) {
            Map<String, Object> arguments = substituteMap(invocation.arguments(), bindings, source);
            if (PRIMITIVES.contains(invocation.name())) {
                validatePrimitive(invocation.name(), arguments, source);
                output.add(new ScenarioPlan.Step(invocation.name(), arguments, source));
                continue;
            }

            ScenarioDocument command = catalog.command(invocation.name());
            if (command == null) {
                throw new ScenarioException(source + ": unknown step or command '" + invocation.name() + "'");
            }
            if (commandStack.contains(command.id())) {
                List<String> cycle = new ArrayList<>(commandStack);
                cycle.add(command.id());
                throw new ScenarioException(source + ": recursive command cycle: " + String.join(" -> ", cycle));
            }

            Map<String, Object> commandBindings = validateCommandArguments(command, arguments, source);
            commandStack.addLast(command.id());
            expand(command.steps(), command.source(), commandBindings, commandStack, output);
            commandStack.removeLast();
        }
    }

    private static Map<String, Object> validateCommandArguments(
            ScenarioDocument command,
            Map<String, Object> arguments,
            String callerSource
    ) {
        Set<String> expected = command.parameters().stream()
                .map(ScenarioDocument.Parameter::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (String supplied : arguments.keySet()) {
            if (!expected.contains(supplied)) {
                throw new ScenarioException(callerSource + ": command '" + command.id()
                        + "' does not declare parameter '" + supplied + "'");
            }
        }

        Map<String, Object> bindings = new LinkedHashMap<>();
        for (ScenarioDocument.Parameter parameter : command.parameters()) {
            Object value = arguments.get(parameter.name());
            if (value == null) {
                throw new ScenarioException(callerSource + ": command '" + command.id()
                        + "' is missing parameter '" + parameter.name() + "'");
            }
            if ("string".equals(parameter.type()) && !(value instanceof String)) {
                throw new ScenarioException(callerSource + ": parameter '" + parameter.name()
                        + "' for command '" + command.id() + "' must be a string");
            }
            bindings.put(parameter.name(), value);
        }
        return bindings;
    }

    private static void validatePrimitive(String name, Map<String, Object> arguments, String source) {
        if ("launchGame".equals(name) || "debugScreen".equals(name)) {
            if (!arguments.isEmpty()) {
                throw new ScenarioException(source + ": " + name + " does not accept arguments");
            }
            return;
        }

        for (String key : arguments.keySet()) {
            if (!Set.of("type", "name").contains(key)) {
                throw new ScenarioException(source + ": step '" + name + "' has unknown selector field '" + key + "'");
            }
        }
        requireSelectorString(arguments, "type", name, source);
        requireSelectorString(arguments, "name", name, source);
        if (!Set.of("button", "cycle", "tab").contains(arguments.get("type"))) {
            throw new ScenarioException(source + ": unsupported element type '" + arguments.get("type")
                    + "'; supported types: [button, cycle, tab]");
        }
    }

    private static void requireSelectorString(
            Map<String, Object> arguments,
            String key,
            String step,
            String source
    ) {
        Object value = arguments.get(key);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(source + ": step '" + step + "' requires a non-empty string '" + key + "'");
        }
    }

    private static Map<String, Object> substituteMap(
            Map<String, Object> values,
            Map<String, Object> bindings,
            String source
    ) {
        Map<String, Object> substituted = new LinkedHashMap<>();
        values.forEach((key, value) -> substituted.put(key, substitute(value, bindings, source)));
        return substituted;
    }

    private static Object substitute(Object value, Map<String, Object> bindings, String source) {
        if (value instanceof String string) {
            Matcher matcher = TEMPLATE.matcher(string);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String parameter = matcher.group(1);
                Object replacement = bindings.get(parameter);
                if (replacement == null) {
                    throw new ScenarioException(source + ": no value supplied for template parameter '"
                            + parameter + "'");
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
            }
            matcher.appendTail(result);
            return result.toString();
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            rawMap.forEach((key, entryValue) ->
                    map.put(String.valueOf(key), substitute(entryValue, bindings, source)));
            return map;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(entry -> substitute(entry, bindings, source)).toList();
        }
        return value;
    }
}
