package com.adamkali.sightline.gradle

import org.gradle.api.Project

class SightlineExtension {
    /**
     * Loader to wire: fabric, forge, or neoforge. Auto-detected when unambiguous.
     */
    String loader

    /**
     * Directory containing YAML scenarios (type: test|command).
     */
    File testsDir

    /**
     * Additional YAML roots to scan for {@code runSightlineTests} discovery.
     */
    List<File> extraTestsDirs = []

    /**
     * Client run directory (relative to the project).
     */
    String runDir = 'build/sightline/run'

    /**
     * Build-relative output directory for reports, metrics, and archives.
     */
    String outputDir = 'sightline'

    SightlineExtension(Project project) {
        this.testsDir = project.file('src/sightlineTests/resources/tests')
    }

    List<File> allTestsDirs() {
        def dirs = [testsDir]
        if (extraTestsDirs != null) {
            dirs.addAll(extraTestsDirs)
        }
        return dirs.findAll { it != null }
    }
}
