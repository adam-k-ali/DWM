package com.adamkali.screenplay.gradle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScreenplayHarnessRepositoryTest {
    @Test
    void encodesPlusInGitHubTagPathAndKeepsPlusInFilename() {
        String version = "1.0.0+26.2";
        assertEquals("screenplay-v1.0.0+26.2", ScreenplayHarnessRepository.githubReleaseTag(version));
        assertEquals(
                "screenplay-v1.0.0%2B26.2",
                ScreenplayHarnessRepository.encodeTagPathSegment("screenplay-v1.0.0+26.2")
        );
        assertEquals(
                "https://github.com/adam-k-ali/DWM/releases/download/screenplay-v1.0.0%2B26.2",
                ScreenplayHarnessRepository.githubReleaseDownloadBase(version)
        );
        assertEquals(
                "screenplay-fabric-1.0.0+26.2.jar",
                ScreenplayHarnessRepository.artifactFileName("screenplay-fabric", version)
        );
        assertEquals(
                "https://github.com/adam-k-ali/DWM/releases/download/screenplay-v1.0.0%2B26.2/screenplay-fabric-1.0.0+26.2.jar",
                ScreenplayHarnessRepository.artifactDownloadUrl("screenplay-fabric", version)
        );
        assertEquals("[artifact]-[revision].[ext]", ScreenplayHarnessRepository.ivyArtifactPattern());
    }

    @Test
    void overrideUrlWinsAndStripsTrailingSlash() {
        assertEquals(
                "http://127.0.0.1:9/repo",
                ScreenplayHarnessRepository.resolveDownloadBase("1.0.0+26.2", "http://127.0.0.1:9/repo/")
        );
        assertEquals(
                ScreenplayHarnessRepository.githubReleaseDownloadBase("1.0.0+26.2"),
                ScreenplayHarnessRepository.resolveDownloadBase("1.0.0+26.2", "  ")
        );
    }

    @Test
    void rejectsBlankInputs() {
        assertThrows(IllegalArgumentException.class, () -> ScreenplayHarnessRepository.githubReleaseTag(" "));
        assertThrows(IllegalArgumentException.class, () -> ScreenplayHarnessRepository.encodeTagPathSegment(""));
        assertThrows(
                IllegalArgumentException.class,
                () -> ScreenplayHarnessRepository.artifactFileName(" ", "1.0.0")
        );
    }
}
