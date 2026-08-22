package com.adamkali.dwm.scenariotest;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public final class ScenarioCatalog {
    private final Map<String, ScenarioDocument> tests;
    private final Map<String, ScenarioDocument> commands;

    private ScenarioCatalog(Map<String, ScenarioDocument> tests, Map<String, ScenarioDocument> commands) {
        this.tests = Map.copyOf(tests);
        this.commands = Map.copyOf(commands);
    }

    public static ScenarioCatalog loadFromResources(ClassLoader classLoader) {
        URL root = classLoader.getResource("tests");
        if (root == null) {
            throw new ScenarioException("Could not find the scenario resource directory 'tests'");
        }
        if (!"file".equals(root.getProtocol())) {
            throw new ScenarioException("Scenario resources must be available as files, but found: " + root);
        }
        try {
            return load(Path.of(root.toURI()));
        } catch (URISyntaxException exception) {
            throw new ScenarioException("Invalid scenario resource URL: " + root, exception);
        }
    }

    public static ScenarioCatalog load(Path root) {
        if (!Files.isDirectory(root)) {
            throw new ScenarioException("Scenario root is not a directory: " + root);
        }

        Map<String, ScenarioDocument> tests = new LinkedHashMap<>();
        Map<String, ScenarioDocument> commands = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(ScenarioCatalog::isYaml)
                    .sorted()
                    .map(path -> parse(root, path))
                    .forEach(document -> {
                        Map<String, ScenarioDocument> target =
                                document.type() == ScenarioDocument.Type.TEST ? tests : commands;
                        ScenarioDocument previous = target.putIfAbsent(document.id(), document);
                        if (previous != null) {
                            throw new ScenarioException("Duplicate " + document.type().name().toLowerCase(Locale.ROOT)
                                    + " id '" + document.id() + "' in " + previous.source()
                                    + " and " + document.source());
                        }
                    });
        } catch (IOException exception) {
            throw new ScenarioException("Could not scan scenario resources under " + root, exception);
        }
        return new ScenarioCatalog(tests, commands);
    }

    public ScenarioDocument requireTest(String id) {
        ScenarioDocument test = tests.get(normalizeId(id));
        if (test == null) {
            throw new ScenarioException("Unknown test '" + id + "'. Available tests: " + tests.keySet());
        }
        return test;
    }

    public ScenarioDocument command(String id) {
        return commands.get(id);
    }

    public Map<String, ScenarioDocument> tests() {
        return tests;
    }

    public Map<String, ScenarioDocument> commands() {
        return commands;
    }

    private static ScenarioDocument parse(Path root, Path path) {
        String source = root.relativize(path).toString().replace('\\', '/');
        String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException exception) {
            throw new ScenarioException("Could not read " + source, exception);
        }

        if (!content.startsWith("---\n")) {
            throw new ScenarioException(source + ": expected YAML frontmatter opening '---'");
        }
        int closing = content.indexOf("\n---\n", 4);
        if (closing < 0) {
            throw new ScenarioException(source + ": expected YAML frontmatter closing '---'");
        }

        Map<String, Object> frontmatter = loadMap(content.substring(4, closing), source + " frontmatter");
        Map<String, Object> body = loadMap(content.substring(closing + 5), source + " body");
        String id = filenameStem(path);
        String name = requireString(frontmatter, "name", source);
        String rawType = requireString(frontmatter, "type", source);
        ScenarioDocument.Type type = switch (rawType) {
            case "test" -> ScenarioDocument.Type.TEST;
            case "command" -> ScenarioDocument.Type.COMMAND;
            default -> throw new ScenarioException(source + ": unsupported frontmatter type '" + rawType + "'");
        };

        List<ScenarioDocument.Parameter> parameters = parseParameters(body.get("parameters"), source, type);
        List<ScenarioDocument.Invocation> steps = parseSteps(body.get("steps"), source);
        return new ScenarioDocument(id, name, type, parameters, steps, source);
    }

    private static List<ScenarioDocument.Parameter> parseParameters(
            Object value,
            String source,
            ScenarioDocument.Type type
    ) {
        if (value == null) {
            return List.of();
        }
        if (type != ScenarioDocument.Type.COMMAND) {
            throw new ScenarioException(source + ": only command documents may declare parameters");
        }
        if (!(value instanceof List<?> values)) {
            throw new ScenarioException(source + ": 'parameters' must be a list");
        }

        List<ScenarioDocument.Parameter> parameters = new ArrayList<>();
        for (Object entry : values) {
            Map<String, Object> parameter = requireMap(entry, source + " parameter");
            String name = requireString(parameter, "name", source + " parameter");
            String parameterType = requireString(parameter, "type", source + " parameter");
            if (!"string".equals(parameterType)) {
                throw new ScenarioException(source + ": unsupported parameter type '" + parameterType
                        + "' for '" + name + "'");
            }
            if (parameters.stream().anyMatch(existing -> existing.name().equals(name))) {
                throw new ScenarioException(source + ": duplicate parameter '" + name + "'");
            }
            parameters.add(new ScenarioDocument.Parameter(name, parameterType));
        }
        return List.copyOf(parameters);
    }

    private static List<ScenarioDocument.Invocation> parseSteps(Object value, String source) {
        if (!(value instanceof List<?> values) || values.isEmpty()) {
            throw new ScenarioException(source + ": 'steps' must be a non-empty list");
        }
        List<ScenarioDocument.Invocation> steps = new ArrayList<>();
        for (Object entry : values) {
            if (entry instanceof String name) {
                steps.add(new ScenarioDocument.Invocation(name, Map.of()));
                continue;
            }
            Map<String, Object> invocation = requireMap(entry, source + " step");
            if (invocation.size() != 1) {
                throw new ScenarioException(source + ": each step must contain exactly one command");
            }
            Map.Entry<String, Object> command = invocation.entrySet().iterator().next();
            steps.add(new ScenarioDocument.Invocation(
                    command.getKey(),
                    normalizeArguments(command.getValue(), source, command.getKey())
            ));
        }
        return List.copyOf(steps);
    }

    private static Map<String, Object> normalizeArguments(Object value, String source, String step) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof String text) {
            return Map.of("text", text);
        }
        if (value instanceof Number number) {
            return Map.of("text", number.toString());
        }
        if (value instanceof List<?> list) {
            if (list.size() != 1) {
                throw new ScenarioException(source + ": step '" + step
                        + "' expects one argument object, but received " + list.size());
            }
            value = list.getFirst();
        }
        return Map.copyOf(requireMap(value, source + " step '" + step + "'"));
    }

    private static Map<String, Object> loadMap(String yaml, String context) {
        Object value;
        try {
            value = new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
        } catch (RuntimeException exception) {
            throw new ScenarioException(context + ": invalid YAML: " + exception.getMessage(), exception);
        }
        if (value == null) {
            return Map.of();
        }
        return requireMap(value, context);
    }

    private static Map<String, Object> requireMap(Object value, String context) {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new ScenarioException(context + ": expected an object");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((key, entryValue) -> {
            if (!(key instanceof String stringKey)) {
                throw new ScenarioException(context + ": object keys must be strings");
            }
            result.put(stringKey, entryValue);
        });
        return result;
    }

    private static String requireString(Map<String, Object> values, String key, String context) {
        Object value = values.get(key);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(context + ": '" + key + "' must be a non-empty string");
        }
        return string;
    }

    private static boolean isYaml(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".yaml") || name.endsWith(".yml");
    }

    private static String filenameStem(Path path) {
        String name = path.getFileName().toString();
        int extension = name.lastIndexOf('.');
        return extension < 0 ? name : name.substring(0, extension);
    }

    private static String normalizeId(String id) {
        String normalized = id.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        int extension = normalized.lastIndexOf('.');
        return extension < 0 ? normalized : normalized.substring(0, extension);
    }
}
