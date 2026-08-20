package com.adamkali.dwm.scenariotest;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class VanillaServerProcess {
    static final int DEFAULT_PORT = 25565;
    static final String DIR_PROPERTY = "dwm.scenario.vanilla-server-dir";
    static final String JAR_PATH_FILE = "server-jar.path";

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int PROBE_TIMEOUT_MS = 100;
    private static final int STOP_WAIT_SECONDS = 5;
    private static final int DESTROY_WAIT_SECONDS = 3;
    private static final int LOG_TAIL_LINES = 40;
    private static final String LOOPBACK = "127.0.0.1";

    private final Logger logger;
    private final Object lock = new Object();

    private Process process;
    private Path logFile;
    private Thread shutdownHook;
    private boolean launched;
    private boolean completed;
    private boolean stopped;

    VanillaServerProcess(Logger logger) {
        this.logger = logger;
    }

    static int parsePort(Object value) {
        if (value == null) {
            return DEFAULT_PORT;
        }
        int port;
        if (value instanceof Integer integer) {
            port = integer;
        } else if (value instanceof Long longValue) {
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw invalidPort();
            }
            port = longValue.intValue();
        } else if (value instanceof String string) {
            if (string.isBlank()) {
                throw invalidPort();
            }
            try {
                port = Integer.parseInt(string.trim());
            } catch (NumberFormatException exception) {
                throw invalidPort();
            }
        } else {
            throw invalidPort();
        }
        if (port < MIN_PORT || port > MAX_PORT) {
            throw invalidPort();
        }
        return port;
    }

    static String serverProperties(int port) {
        parsePort(port);
        return """
                online-mode=false
                server-ip=%s
                server-port=%d
                level-type=minecraft:flat
                spawn-monsters=false
                spawn-npcs=false
                spawn-animals=false
                difficulty=peaceful
                enforce-secure-profile=false
                max-tick-time=-1
                motd=DWM Scenario Vanilla Server
                """.formatted(LOOPBACK, port);
    }

    static String eulaText() {
        return "eula=true\n";
    }

    boolean tick(int port) {
        synchronized (lock) {
            if (completed) {
                throw new ScenarioException("startVanillaServer can only run once per scenario");
            }
            if (!launched) {
                start(port);
                launched = true;
                return false;
            }
            if (process == null || !process.isAlive()) {
                throw died();
            }
            if (isListening(port)) {
                logger.info("Vanilla server is accepting connections on {}:{}", LOOPBACK, port);
                completed = true;
                return true;
            }
            return false;
        }
    }

    void stop() {
        synchronized (lock) {
            if (stopped) {
                return;
            }
            stopped = true;
            if (process != null && process.isAlive()) {
                try {
                    OutputStream stdin = process.getOutputStream();
                    stdin.write("stop\n".getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                    if (!process.waitFor(STOP_WAIT_SECONDS, TimeUnit.SECONDS) && process.isAlive()) {
                        process.destroyForcibly();
                        process.waitFor(DESTROY_WAIT_SECONDS, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    process.destroyForcibly();
                } catch (IOException exception) {
                    process.destroyForcibly();
                }
            }
            if (shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (IllegalStateException ignored) {
                    // JVM is already shutting down.
                }
                shutdownHook = null;
            }
        }
    }

    private void start(int port) {
        parsePort(port);
        Path dir = resolveServerDir();
        Path jar = resolveServerJar(dir);
        if (isListening(port)) {
            throw new ScenarioException("Port " + port + " is already in use");
        }

        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("eula.txt"), eulaText(), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("server.properties"), serverProperties(port), StandardCharsets.UTF_8);
            Path logs = dir.resolve("logs");
            Files.createDirectories(logs);
            logFile = logs.resolve("harness.log");
        } catch (IOException exception) {
            throw new ScenarioException("Could not prepare the vanilla server directory " + dir, exception);
        }

        ProcessBuilder builder = new ProcessBuilder(
                javaExecutable(),
                "-Xmx1G",
                "-jar",
                jar.toAbsolutePath().toString(),
                "nogui"
        );
        builder.directory(dir.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));

        try {
            process = builder.start();
        } catch (IOException exception) {
            throw new ScenarioException("Could not start the vanilla Minecraft server", exception);
        }

        shutdownHook = new Thread(this::stop, "dwm-vanilla-server-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.info("Started vanilla server pid {} on {}:{} using {}", process.pid(), LOOPBACK, port, jar);
    }

    private static Path resolveServerDir() {
        String dir = System.getProperty(DIR_PROPERTY);
        if (dir == null || dir.isBlank()) {
            throw new ScenarioException("System property '" + DIR_PROPERTY
                    + "' is missing. Run prepareScenarioTestRun first.");
        }
        return Path.of(dir);
    }

    private static Path resolveServerJar(Path dir) {
        Path jarPathFile = dir.resolve(JAR_PATH_FILE);
        if (!Files.isRegularFile(jarPathFile)) {
            throw new ScenarioException("Vanilla server jar path file is missing: " + jarPathFile
                    + ". Run prepareScenarioTestRun first.");
        }
        String raw;
        try {
            raw = Files.readString(jarPathFile, StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new ScenarioException("Could not read " + jarPathFile, exception);
        }
        if (raw.isEmpty()) {
            throw new ScenarioException("Vanilla server jar path file is empty: " + jarPathFile
                    + ". Run prepareScenarioTestRun first.");
        }
        Path jar = Path.of(raw);
        if (!Files.isRegularFile(jar)) {
            throw new ScenarioException("Vanilla server jar is missing: " + jar
                    + ". Run prepareScenarioTestRun first.");
        }
        return jar;
    }

    private static boolean isListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(LOOPBACK, port), PROBE_TIMEOUT_MS);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static String javaExecutable() {
        String home = System.getProperty("java.home");
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("win");
        return Path.of(home, "bin", windows ? "java.exe" : "java").toString();
    }

    private ScenarioException died() {
        int exit = process == null ? -1 : process.exitValue();
        return new ScenarioException("Vanilla Minecraft server exited with code " + exit
                + " before accepting connections. Last log lines:\n" + lastLogLines());
    }

    private String lastLogLines() {
        if (logFile == null || !Files.isRegularFile(logFile)) {
            return "<no harness log>";
        }
        try {
            List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            int from = Math.max(0, lines.size() - LOG_TAIL_LINES);
            if (from >= lines.size()) {
                return "<empty harness log>";
            }
            return String.join("\n", lines.subList(from, lines.size()));
        } catch (IOException exception) {
            return "<could not read harness log: " + exception.getMessage() + ">";
        }
    }

    private static ScenarioException invalidPort() {
        return new ScenarioException("startVanillaServer port must be an integer between 1 and 65535");
    }
}
