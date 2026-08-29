package com.adamkali.screenplay.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;

import java.net.URI;

/**
 * Coordinates and GitHub Release download URLs for Screenplay loader jars.
 *
 * <p>Consumers resolve {@code com.adamkali.screenplay:screenplay-*} from the
 * matching {@code screenplay-v*} GitHub Release. The plugin registers an Ivy
 * repository pointing at {@link #githubReleaseDownloadBase(String)}.
 */
public final class ScreenplayHarnessRepository {
    public static final String GROUP = "com.adamkali.screenplay";
    public static final String GITHUB_OWNER_REPO = "adam-k-ali/DWM";
    public static final String REPOSITORY_NAME = "Screenplay GitHub Releases";
    /**
     * Optional project property that replaces the GitHub download base URL
     * (used by tests to point at a local server).
     */
    public static final String URL_OVERRIDE_PROPERTY = "screenplay.harnessRepositoryUrl";

    private ScreenplayHarnessRepository() {
    }

    public static String githubReleaseTag(String screenplayVersion) {
        requireVersion(screenplayVersion);
        return "screenplay-v" + screenplayVersion;
    }

    /**
     * GitHub encodes {@code +} in tag names as {@code %2B} in release-asset
     * download paths. The filename still uses a literal {@code +}.
     */
    public static String encodeTagPathSegment(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must be non-blank");
        }
        return tag.replace("+", "%2B");
    }

    public static String githubReleaseDownloadBase(String screenplayVersion) {
        String tag = encodeTagPathSegment(githubReleaseTag(screenplayVersion));
        return "https://github.com/" + GITHUB_OWNER_REPO + "/releases/download/" + tag;
    }

    public static String artifactFileName(String artifactId, String screenplayVersion) {
        requireVersion(screenplayVersion);
        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException("artifactId must be non-blank");
        }
        return artifactId + "-" + screenplayVersion + ".jar";
    }

    public static String artifactDownloadUrl(String artifactId, String screenplayVersion) {
        return githubReleaseDownloadBase(screenplayVersion)
                + "/"
                + artifactFileName(artifactId, screenplayVersion);
    }

    public static String ivyArtifactPattern() {
        return "[artifact]-[revision].[ext]";
    }

    public static String resolveDownloadBase(String screenplayVersion, Object override) {
        if (override != null) {
            String text = override.toString().trim();
            if (!text.isEmpty()) {
                return stripTrailingSlash(text);
            }
        }
        return githubReleaseDownloadBase(screenplayVersion);
    }

    public static void register(Project project, String screenplayVersion) {
        String repoUrl = resolveDownloadBase(
                screenplayVersion,
                project.findProperty(URL_OVERRIDE_PROPERTY)
        );
        RepositoryHandler repositories = project.getRepositories();
        repositories.exclusiveContent(spec -> {
            spec.forRepository(() -> repositories.ivy(ivy -> configureIvy(ivy, repoUrl)));
            spec.filter(filter -> filter.includeGroup(GROUP));
        });
    }

    private static void configureIvy(IvyArtifactRepository ivy, String repoUrl) {
        ivy.setName(REPOSITORY_NAME);
        ivy.setUrl(URI.create(repoUrl));
        if (repoUrl.startsWith("http://")) {
            ivy.setAllowInsecureProtocol(true);
        }
        ivy.patternLayout(layout -> layout.artifact(ivyArtifactPattern()));
        ivy.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
    }

    private static String stripTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static void requireVersion(String screenplayVersion) {
        if (screenplayVersion == null || screenplayVersion.isBlank()) {
            throw new IllegalArgumentException("screenplayVersion must be non-blank");
        }
    }
}
