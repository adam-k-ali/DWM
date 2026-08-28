package com.adamkali.screenplay.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filesystem helpers for discovering YAML scenario ids and writing the first-run starter.
 * Kept free of {@code Project} so it can be unit-tested without TestKit.
 */
public final class ScreenplayScenarioDiscovery {
    public static final String STARTER_FILENAME = "myFirstTest.yaml";
    public static final String STARTER_CONTENTS = """
            ---
            name: Create a world
            type: test
            ---
            steps:
              - launchGame
              - createWorld:
                  worldType: flat
                  gameMode: creative
              - captureScreenshot:
                  name: world-ready.png
            """;

    private static final Pattern SUITE_TEST_ITEM = Pattern.compile("^\\s*-\\s+(.+?)\\s*$");

    private ScreenplayScenarioDiscovery() {
    }

    public record Discovery(
            Set<String> suiteIds,
            Set<String> standaloneTestIds,
            Set<String> runIds
    ) {
        public Discovery {
            suiteIds = Set.copyOf(suiteIds);
            standaloneTestIds = Set.copyOf(standaloneTestIds);
            runIds = Set.copyOf(runIds);
        }
    }

    public static boolean needsStarter(File testsDir) {
        if (testsDir == null || !testsDir.isDirectory()) {
            return true;
        }
        return yamlFiles(testsDir).isEmpty();
    }

    public static File writeStarter(File testsDir) {
        if (testsDir == null) {
            throw new IllegalArgumentException("testsDir is required");
        }
        File starter = new File(testsDir, STARTER_FILENAME);
        if (starter.isFile()) {
            return starter;
        }
        if (!testsDir.exists() && !testsDir.mkdirs()) {
            throw new IllegalStateException("Could not create Screenplay tests directory: " + testsDir);
        }
        try {
            Files.writeString(starter.toPath(), STARTER_CONTENTS, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write starter scenario: " + starter, exception);
        }
        return starter;
    }

    /**
     * @param requestedId {@code -Pscreenplay} value, or {@code null}/blank to auto-select
     * @return the scenario id to run
     */
    public static String resolveScenarioId(String requestedId, List<File> testsDirs) {
        if (requestedId != null && !requestedId.isBlank()) {
            return requestedId.trim();
        }
        if (testsDirs == null || testsDirs.isEmpty() || testsDirs.getFirst() == null) {
            throw new IllegalStateException(
                    "Screenplay testsDir is not configured. Set screenplay.testsDir or create "
                            + "src/screenplayTests/resources/tests/.");
        }
        File primary = testsDirs.getFirst();
        if (needsStarter(primary)) {
            writeStarter(primary);
        }
        Discovery discovery = discoverRunIds(testsDirs);
        List<String> runIds = new ArrayList<>(discovery.runIds());
        if (runIds.isEmpty()) {
            throw new IllegalStateException(
                    "No Screenplay tests or suites found under " + testsDirs
                            + ". Add a YAML file with type: test, or run ./gradlew runScreenplay to generate "
                            + STARTER_FILENAME + ".");
        }
        if (runIds.size() == 1) {
            return runIds.getFirst();
        }
        throw new IllegalStateException(
                "Multiple Screenplay scenarios found: " + String.join(", ", runIds)
                        + ". Run one with ./gradlew runScreenplay -Pscreenplay=<id> "
                        + "or run all with ./gradlew runScreenplayTests.");
    }

    public static Discovery discoverRunIds(List<File> scenarioTestsRoots) {
        Set<String> suiteIds = new TreeSet<>();
        Set<String> testIds = new TreeSet<>();
        Set<String> suiteMemberIds = new LinkedHashSet<>();
        List<File> roots = scenarioTestsRoots == null ? List.of() : scenarioTestsRoots;
        for (File scenarioTestsRoot : roots) {
            if (scenarioTestsRoot == null || !scenarioTestsRoot.isDirectory()) {
                continue;
            }
            for (File yamlFile : yamlFiles(scenarioTestsRoot)) {
                String frontmatterType = readFrontmatterType(yamlFile);
                if (frontmatterType == null) {
                    continue;
                }
                String id = filenameStem(yamlFile);
                if ("suite".equals(frontmatterType)) {
                    if (!suiteIds.add(id)) {
                        throw new IllegalStateException(
                                "Duplicate scenario suite id '" + id + "' under " + roots);
                    }
                    suiteMemberIds.addAll(readSuiteTestIds(yamlFile));
                } else if ("test".equals(frontmatterType)) {
                    if (!testIds.add(id)) {
                        throw new IllegalStateException(
                                "Duplicate scenario test id '" + id + "' under " + roots);
                    }
                }
            }
        }
        Set<String> standaloneTestIds = new TreeSet<>();
        for (String testId : testIds) {
            if (!suiteMemberIds.contains(testId)) {
                standaloneTestIds.add(testId);
            }
        }
        Set<String> runIds = new TreeSet<>();
        runIds.addAll(suiteIds);
        runIds.addAll(standaloneTestIds);
        return new Discovery(suiteIds, standaloneTestIds, runIds);
    }

    static Collection<File> yamlFiles(File root) {
        List<File> files = new ArrayList<>();
        collectYamlFiles(root, files);
        return files;
    }

    private static void collectYamlFiles(File file, List<File> files) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".yaml") || name.endsWith(".yml")) {
                files.add(file);
            }
            return;
        }
        File[] children = file.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            collectYamlFiles(child, files);
        }
    }

    static String filenameStem(File yamlFile) {
        String name = yamlFile.getName();
        int extension = name.lastIndexOf('.');
        return extension < 0 ? name : name.substring(0, extension);
    }

    static String readFrontmatterType(File yamlFile) {
        String text;
        try {
            text = Files.readString(yamlFile.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException exception) {
            return null;
        }
        if (!text.startsWith("---\n")) {
            return null;
        }
        int closing = text.indexOf("\n---\n", 4);
        if (closing < 0) {
            return null;
        }
        String typeLine = null;
        for (String line : text.substring(4, closing).split("\n", -1)) {
            if (line.trim().startsWith("type:")) {
                typeLine = line;
                break;
            }
        }
        if (typeLine == null) {
            return null;
        }
        return typeLine.substring(typeLine.indexOf(':') + 1).trim();
    }

    static List<String> readSuiteTestIds(File yamlFile) {
        String text;
        try {
            text = Files.readString(yamlFile.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException exception) {
            return List.of();
        }
        int closing = text.indexOf("\n---\n", 4);
        if (closing < 0) {
            return List.of();
        }
        String body = text.substring(closing + 5);
        List<String> ids = new ArrayList<>();
        boolean inTests = false;
        for (String rawLine : body.split("\n", -1)) {
            String line = rawLine.stripTrailing();
            if (!inTests) {
                if (line.trim().equals("tests:")) {
                    inTests = true;
                }
                continue;
            }
            if (line.matches("^[A-Za-z0-9_-]+:.*")) {
                inTests = false;
                continue;
            }
            Matcher matcher = SUITE_TEST_ITEM.matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!value.isBlank()) {
                    ids.add(value);
                }
            }
        }
        return ids;
    }
}
