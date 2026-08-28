package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.ScenarioPrimitive;
import com.adamkali.screenplay.primitive.ScenarioPrimitives;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScenarioCompiler {
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");

    private final ScenarioCatalog catalog;

    public ScenarioCompiler(ScenarioCatalog catalog) {
        this.catalog = catalog;
    }

    public ScenarioPlan compile(String testId) {
        ScenarioDocument test = catalog.requireTest(testId);
        List<ScenarioPlan.Step> expanded = new ArrayList<>();
        expand(test.steps(), test.source(), Map.of(), new ArrayDeque<>(), expanded);
        return new ScenarioPlan(test.id(), test.name(), expanded, test.record());
    }

    public SuitePlan compileSuite(String suiteId) {
        ScenarioDocument suite = catalog.requireSuite(suiteId);
        List<ScenarioPlan.Step> beforeAll = expandHook(suite.beforeAll(), suite.source());
        List<ScenarioPlan.Step> beforeEach = expandHook(suite.beforeEach(), suite.source());
        List<ScenarioPlan.Step> afterEach = expandHook(suite.afterEach(), suite.source());
        List<ScenarioPlan.Step> afterAll = expandHook(suite.afterAll(), suite.source());

        Set<String> seen = new LinkedHashSet<>();
        List<ScenarioPlan> members = new ArrayList<>();
        for (String testId : suite.testIds()) {
            if (!seen.add(testId)) {
                throw new ScenarioException(suite.source() + ": duplicate suite test id '" + testId + "'");
            }
            if (catalog.suites().containsKey(testId)) {
                throw new ScenarioException(suite.source()
                        + ": suite member '" + testId + "' must be a test, not a suite");
            }
            if (catalog.command(testId) != null && !catalog.tests().containsKey(testId)) {
                throw new ScenarioException(suite.source()
                        + ": suite member '" + testId + "' must be a test, not a command");
            }
            if (!catalog.tests().containsKey(testId)) {
                throw new ScenarioException(suite.source() + ": unknown suite test '" + testId
                        + "'. Available tests: " + catalog.tests().keySet());
            }
            members.add(compile(testId));
        }
        return new SuitePlan(
                suite.id(),
                suite.name(),
                beforeAll,
                beforeEach,
                afterEach,
                afterAll,
                members,
                suite.record()
        );
    }

    private List<ScenarioPlan.Step> expandHook(List<ScenarioDocument.Invocation> invocations, String source) {
        if (invocations.isEmpty()) {
            return List.of();
        }
        List<ScenarioPlan.Step> expanded = new ArrayList<>();
        expand(invocations, source, Map.of(), new ArrayDeque<>(), expanded);
        return List.copyOf(expanded);
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
            ScenarioPrimitive primitive = ScenarioPrimitives.find(invocation.name());
            if (primitive != null) {
                output.add(new ScenarioPlan.Step(
                        primitive.name(),
                        primitive.validate(arguments, source),
                        source));
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
