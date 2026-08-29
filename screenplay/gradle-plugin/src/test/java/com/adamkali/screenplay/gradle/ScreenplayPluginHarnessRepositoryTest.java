package com.adamkali.screenplay.gradle;

import com.sun.net.httpserver.HttpServer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenplayPluginHarnessRepositoryTest {
    private static final String VERSION = "1.0.0+26.2";
    private static final byte[] JAR_BYTES = new byte[] {0x50, 0x4b, 0x03, 0x04, 0x14, 0x00};

    @Test
    void applyRegistersExclusiveIvyRepoForGitHubReleases() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(ScreenplayPlugin.class);

        IvyArtifactRepository repository = assertInstanceOf(
                IvyArtifactRepository.class,
                project.getRepositories().getByName(ScreenplayHarnessRepository.REPOSITORY_NAME)
        );
        assertEquals(
                URI.create(ScreenplayHarnessRepository.githubReleaseDownloadBase(
                        ScreenplayPluginVersions.screenplayVersion()
                )),
                repository.getUrl()
        );
    }

    @Test
    void addHarnessDependencyFalseSkipsRepository() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("screenplay.addHarnessDependency", "false");
        project.getPluginManager().apply(ScreenplayPlugin.class);
        assertTrue(project.getRepositories().isEmpty());
    }

    @Test
    void ivyLayoutResolvesPlusVersionFromLocalServer(@TempDir Path tempDir) throws Exception {
        String fileName = ScreenplayHarnessRepository.artifactFileName("screenplay-fabric", VERSION);
        AtomicInteger heads = new AtomicInteger();
        AtomicInteger gets = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!fileName.equals(path)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            if ("HEAD".equals(exchange.getRequestMethod())) {
                heads.incrementAndGet();
                exchange.getResponseHeaders().add("Content-Type", "application/java-archive");
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                gets.incrementAndGet();
                exchange.getResponseHeaders().add("Content-Type", "application/java-archive");
                exchange.sendResponseHeaders(200, JAR_BYTES.length);
                try (OutputStream body = exchange.getResponseBody()) {
                    body.write(JAR_BYTES);
                }
                return;
            }
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
        });
        server.start();
        try {
            String repoUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            Path projectDir = tempDir.resolve("consumer");
            Files.createDirectories(projectDir);
            Files.writeString(
                    projectDir.resolve("settings.gradle"),
                    "rootProject.name = 'harness-resolve'\n",
                    StandardCharsets.UTF_8
            );
            Files.writeString(projectDir.resolve("build.gradle"), """
                    configurations { harness }
                    repositories {
                        exclusiveContent {
                            forRepository {
                                ivy {
                                    url = '%s'
                                    allowInsecureProtocol = true
                                    patternLayout {
                                        artifact '%s'
                                    }
                                    metadataSources {
                                        artifact()
                                    }
                                }
                            }
                            filter {
                                includeGroup '%s'
                            }
                        }
                    }
                    dependencies {
                        harness '%s:screenplay-fabric:%s'
                    }
                    tasks.register('resolveHarness') {
                        doLast {
                            def file = configurations.harness.singleFile
                            println "resolved=${file.name}"
                        }
                    }
                    """.formatted(
                    repoUrl,
                    ScreenplayHarnessRepository.ivyArtifactPattern(),
                    ScreenplayHarnessRepository.GROUP,
                    ScreenplayHarnessRepository.GROUP,
                    VERSION
            ), StandardCharsets.UTF_8);

            BuildResult result = GradleRunner.create()
                    .withProjectDir(projectDir.toFile())
                    .withArguments("resolveHarness", "--stacktrace")
                    .withPluginClasspath()
                    .build();

            assertEquals(TaskOutcome.SUCCESS, result.task(":resolveHarness").getOutcome());
            assertTrue(result.getOutput().contains("resolved=" + fileName));
            assertTrue(heads.get() + gets.get() > 0);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void publishedGitHubAssetIsReachableAtEncodedUrl() throws IOException {
        String version = ScreenplayPluginVersions.screenplayVersion();
        URI uri = URI.create(ScreenplayHarnessRepository.artifactDownloadUrl("screenplay-fabric", version));
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "screenplay-gradle-plugin-test");
        connection.connect();
        try {
            int status = connection.getResponseCode();
            assertFalse(status == HttpURLConnection.HTTP_NOT_FOUND, "GitHub asset 404 for " + uri);
            assertNotEquals(HttpURLConnection.HTTP_BAD_REQUEST, status, "GitHub asset bad request for " + uri);
            assertTrue(
                    status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308,
                    () -> "Unexpected status " + status + " for " + uri
            );
            if (status == HttpURLConnection.HTTP_OK) {
                byte[] prefix = connection.getInputStream().readNBytes(4);
                assertTrue(prefix.length >= 2, "jar too short from " + uri);
                assertEquals(0x50, prefix[0] & 0xff);
                assertEquals(0x4b, prefix[1] & 0xff);
            }
        } finally {
            connection.disconnect();
        }
    }
}
