package com.adamkali.screenplay;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class ScenarioCatalog {
    private static final Set<String> SUITE_BODY_KEYS = Set.of(
            "before-all", "before-each", "after-each", "after-all", "tests"
    );

    private final Map<String, ScenarioDocument> tests;
    private final Map<String, ScenarioDocument> commands;
    private final Map<String, ScenarioDocument> suites;

    private ScenarioCatalog(
            Map<String, ScenarioDocument> tests,
            Map<String, ScenarioDocument> commands,
            Map<String, ScenarioDocument> suites
    ) {
        this.tests = Map.copyOf(tests);
        this.commands = Map.copyOf(commands);
        this.suites = Map.copyOf(suites);
    }

    public static ScenarioCatalog loadFromResources(ClassLoader classLoader) {
        Enumeration<URL> roots;
        try {
            roots = classLoader.getResources("tests");
        } catch (IOException exception) {
            throw new ScenarioException("Could not enumerate scenario resource directories named 'tests'", exception);
        }
        if (!roots.hasMoreElements()) {
            throw new ScenarioException("Could not find the scenario resource directory 'tests'");
        }

        Map<String, ScenarioDocument> tests = new LinkedHashMap<>();
        Map<String, ScenarioDocument> commands = new LinkedHashMap<>();
        Map<String, ScenarioDocument> suites = new LinkedHashMap<>();
        while (roots.hasMoreElements()) {
            URL rootUrl = roots.nextElement();
            Path root = toPath(rootUrl);
            loadInto(root, tests, commands, suites);
        }
        return new ScenarioCatalog(tests, commands, suites);
    }

    public static ScenarioCatalog load(Path root) {
        Map<String, ScenarioDocument> tests = new LinkedHashMap<>();
        Map<String, ScenarioDocument> commands = new LinkedHashMap<>();
        Map<String, ScenarioDocument> suites = new LinkedHashMap<>();
        loadInto(root, tests, commands, suites);
        return new ScenarioCatalog(tests, commands, suites);
    }

    private static Path toPath(URL root) {
        try {
            var uri = root.toURI();
            if ("jar".equals(uri.getScheme())) {
                try {
                    return Path.of(uri);
                } catch (java.nio.file.FileSystemNotFoundException exception) {
                    FileSystems.newFileSystem(uri, Collections.emptyMap());
                    return Path.of(uri);
                }
            }
            if ("file".equals(uri.getScheme())) {
                return Path.of(uri);
            }
            throw new ScenarioException("Scenario resources must be file or jar URLs, but found: " + root);
        } catch (URISyntaxException | IOException exception) {
            throw new ScenarioException("Invalid scenario resource URL: " + root, exception);
        }
    }

    private static void loadInto(
            Path root,
            Map<String, ScenarioDocument> tests,
            Map<String, ScenarioDocument> commands,
            Map<String, ScenarioDocument> suites
    ) {
        if (!Files.isDirectory(root)) {
            throw new ScenarioException("Scenario root is not a directory: " + root);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(ScenarioCatalog::isYaml)
                    .sorted()
                    .map(path -> parse(root, path))
                    .forEach(document -> {
                        Map<String, ScenarioDocument> target = switch (document.type()) {
                            case TEST -> tests;
                            case COMMAND -> commands;
                            case SUITE -> suites;
                        };
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
    }

    public ScenarioDocument requireTest(String id) {
        ScenarioDocument test = tests.get(normalizeId(id));
        if (test == null) {
            throw new ScenarioException("Unknown test '" + id + "'. Available tests: " + tests.keySet());
        }
        return test;
    }

    public ScenarioDocument requireSuite(String id) {
        ScenarioDocument suite = suites.get(normalizeId(id));
        if (suite == null) {
            throw new ScenarioException("Unknown suite '" + id + "'. Available suites: " + suites.keySet());
        }
        return suite;
    }

    public ScenarioDocument.Type resolveExecutableType(String id) {
        String normalized = normalizeId(id);
        if (suites.containsKey(normalized)) {
            return ScenarioDocument.Type.SUITE;
        }
        if (tests.containsKey(normalized)) {
            return ScenarioDocument.Type.TEST;
        }
        throw new ScenarioException("Unknown scenario '" + id
                + "'. Available tests: " + tests.keySet()
                + "; available suites: " + suites.keySet());
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

    public Map<String, ScenarioDocument> suites() {
        return suites;
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
            case "suite" -> ScenarioDocument.Type.SUITE;
            default -> throw new ScenarioException(source + ": unsupported frontmatter type '" + rawType + "'");
        };

        boolean record = optionalBoolean(frontmatter, "record", source);
        if (type == ScenarioDocument.Type.SUITE) {
            return parseSuite(id, name, record, body, source);
        }
        return parseTestOrCommand(id, name, type, record, body, source);
    }

    private static ScenarioDocument parseSuite(
            String id,
            String name,
            boolean record,
            Map<String, Object> body,
            String source
    ) {
        if (body.containsKey("parameters")) {
            throw new ScenarioException(source + ": suite documents may not declare parameters");
        }
        if (body.containsKey("steps")) {
            throw new ScenarioException(source
                    + ": suite documents may not declare steps; use before-all/before-each/after-each/after-all");
        }
        for (String key : body.keySet()) {
            if (!SUITE_BODY_KEYS.contains(key)) {
                throw new ScenarioException(source + ": unsupported suite body key '" + key + "'");
            }
        }

        List<ScenarioDocument.Invocation> beforeAll = parseOptionalHook(body.get("before-all"), source, "before-all");
        List<ScenarioDocument.Invocation> beforeEach = parseOptionalHook(body.get("before-each"), source, "before-each");
        List<ScenarioDocument.Invocation> afterEach = parseOptionalHook(body.get("after-each"), source, "after-each");
        List<ScenarioDocument.Invocation> afterAll = parseOptionalHook(body.get("after-all"), source, "after-all");
        List<String> testIds = parseTestIds(body.get("tests"), source);
        return new ScenarioDocument(
                id,
                name,
                ScenarioDocument.Type.SUITE,
                record,
                List.of(),
                List.of(),
                beforeAll,
                beforeEach,
                afterEach,
                afterAll,
                testIds,
                source
        );
    }

    private static ScenarioDocument parseTestOrCommand(
            String id,
            String name,
            ScenarioDocument.Type type,
            boolean record,
            Map<String, Object> body,
            String source
    ) {
        for (String key : body.keySet()) {
            if (SUITE_BODY_KEYS.contains(key)) {
                throw new ScenarioException(source + ": only suite documents may declare '" + key + "'");
            }
        }

        List<ScenarioDocument.Parameter> parameters = parseParameters(body.get("parameters"), source, type);
        List<ScenarioDocument.Invocation> steps = parseStepList(body.get("steps"), source, "steps");
        return new ScenarioDocument(
                id,
                name,
                type,
                record,
                parameters,
                steps,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                source
        );
    }

    private static List<ScenarioDocument.Invocation> parseOptionalHook(Object value, String source, String key) {
        if (value == null) {
            return List.of();
        }
        return parseStepList(value, source, key);
    }

    private static List<String> parseTestIds(Object value, String source) {
        if (!(value instanceof List<?> values) || values.isEmpty()) {
            throw new ScenarioException(source + ": 'tests' must be a non-empty list");
        }
        Set<String> seen = new LinkedHashSet<>();
        List<String> testIds = new ArrayList<>();
        for (Object entry : values) {
            if (!(entry instanceof String testId) || testId.isBlank()) {
                throw new ScenarioException(source + ": each suite test id must be a non-empty string");
            }
            if (!seen.add(testId)) {
                throw new ScenarioException(source + ": duplicate suite test id '" + testId + "'");
            }
            testIds.add(testId);
        }
        return List.copyOf(testIds);
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

    private static List<ScenarioDocument.Invocation> parseStepList(Object value, String source, String field) {
        if (!(value instanceof List<?> values) || values.isEmpty()) {
            throw new ScenarioException(source + ": '" + field + "' must be a non-empty list");
        }
        List<ScenarioDocument.Invocation> steps = new ArrayList<>();
        for (Object entry : values) {
            if (entry instanceof String stepName) {
                steps.add(new ScenarioDocument.Invocation(stepName, Map.of()));
                continue;
            }
            Map<String, Object> invocation = requireMap(entry, source + " " + field + " step");
            if (invocation.size() != 1) {
                throw new ScenarioException(source + ": each " + field + " step must contain exactly one command");
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

    private static boolean optionalBoolean(Map<String, Object> values, String key, String context) {
        if (!values.containsKey(key)) {
            return false;
        }
        Object value = values.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new ScenarioException(context + ": '" + key + "' must be a boolean");
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
