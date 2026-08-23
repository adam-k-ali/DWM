package com.adamkali.sightline.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException

class SightlinePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('sightline', SightlineExtension, project)

        project.afterEvaluate {
            def loader = resolveLoader(project, extension)
            extension.loader = loader

            registerPrepareTask(project, extension)
            configureLoaderRun(project, extension, loader)
            configureDisplayOnRunTask(project, extension)
            registerRunAllTask(project, extension)
        }
    }

    private static String resolveLoader(Project project, SightlineExtension extension) {
        if (extension.loader != null && !extension.loader.isBlank()) {
            def value = extension.loader.trim().toLowerCase()
            if (!(value in ['fabric', 'forge', 'neoforge'])) {
                throw new GradleException("sightline.loader must be fabric, forge, or neoforge (was '${extension.loader}')")
            }
            return value
        }
        boolean fabric = project.plugins.hasPlugin('net.fabricmc.fabric-loom') ||
                project.plugins.hasPlugin('fabric-loom')
        boolean forge = project.plugins.hasPlugin('net.minecraftforge.gradle')
        boolean neoforge = project.plugins.hasPlugin('net.neoforged.gradle.userdev') ||
                project.plugins.hasPlugin('net.neoforged.gradle.common')
        int count = (fabric ? 1 : 0) + (forge ? 1 : 0) + (neoforge ? 1 : 0)
        if (count == 1) {
            if (fabric) return 'fabric'
            if (forge) return 'forge'
            return 'neoforge'
        }
        if (count == 0) {
            throw new GradleException(
                    "Sightline requires Fabric Loom, ForgeGradle, or NeoGradle. " +
                            "Apply one of those plugins, or set sightline.loader explicitly.")
        }
        throw new GradleException(
                "Multiple mod-loader plugins detected. Set sightline.loader to fabric, forge, or neoforge.")
    }

    private static void registerPrepareTask(Project project, SightlineExtension extension) {
        def displayMode = project.providers.gradleProperty('sightlineDisplay').orElse('display')
        project.tasks.register('prepareSightlineRun') {
            group = 'verification'
            description = 'Prepare Sightline run directory, options.txt, and vanilla server jar path'
            def optionsFile = project.layout.buildDirectory.file("${extension.outputDir}/run/options.txt")
            def vanillaServerDir = project.layout.buildDirectory.dir("${extension.outputDir}/vanilla-server")
            def jarPathFile = project.layout.buildDirectory.file("${extension.outputDir}/vanilla-server/server-jar.path")
            outputs.file(optionsFile)
            outputs.file(jarPathFile)
            outputs.upToDateWhen { false }
            doLast {
                project.delete(project.layout.buildDirectory.dir("${extension.outputDir}/run/saves"))
                project.delete(project.layout.buildDirectory.dir("${extension.outputDir}/vanilla-server/world"))
                optionsFile.get().asFile.parentFile.mkdirs()
                def mode = displayMode.get()
                def options = new StringBuilder("""\
lang:en_us
skipMultiplayerWarning:true
onboardAccessibility:false
""")
                if (mode == 'xvfb') {
                    options.append("""\
fullscreen:false
renderDistance:4
simulationDistance:4
maxFps:30
enableVsync:false
guiScale:1
""")
                }
                optionsFile.get().asFile.text = options.toString()
                def serverDir = vanillaServerDir.get().asFile
                serverDir.mkdirs()
                def serverJar = resolveMinecraftServerJar(project)
                if (serverJar != null) {
                    jarPathFile.get().asFile.text = serverJar.absolutePath
                } else {
                    jarPathFile.get().asFile.text = ''
                    logger.warn('Could not resolve Minecraft server jar path for Sightline vanilla-server harness')
                }
            }
        }
    }

    private static File resolveMinecraftServerJar(Project project) {
        try {
            def loomClass = Class.forName('net.fabricmc.loom.LoomGradleExtension')
            def getMethod = loomClass.getMethod('get', Project)
            def loomExt = getMethod.invoke(null, project)
            def minecraftProvider = loomExt.minecraftProvider
            return minecraftProvider.minecraftServerJar as File
        } catch (Throwable ignored) {
            return null
        }
    }

    private static void configureLoaderRun(Project project, SightlineExtension extension, String loader) {
        def scenarioId = project.providers.gradleProperty('sightline').orElse('createWorld')
        def timeout = project.providers.gradleProperty('sightlineTimeout').orElse('30')
        def reportFile = project.layout.buildDirectory.file("${extension.outputDir}/report.xml").get().asFile.absolutePath
        def vanillaServerDir = project.layout.buildDirectory.dir("${extension.outputDir}/vanilla-server").get().asFile.absolutePath
        def runDir = extension.runDir

        if (loader == 'fabric') {
            def loom = project.extensions.findByName('loom')
            if (loom == null) {
                throw new GradleException('Fabric loader selected but loom extension is missing')
            }
            // Prefer configuring an existing run if the consumer already declared one.
            def runs = loom.runs
            def run = runs.findByName('sightline')
            if (run == null) {
                run = runs.create('sightline')
                run.client()
                run.name = 'Sightline'
            }
            run.property 'sightline', scenarioId.get()
            run.property 'sightline.step-timeout-seconds', timeout.get()
            run.property 'sightline.report-file', reportFile
            run.property 'sightline.vanilla-server-dir', vanillaServerDir
            run.runDir runDir
            return
        }

        try {
            def runsContainer = null
            if (project.extensions.findByName('minecraft')?.hasProperty('runs')) {
                runsContainer = project.extensions.minecraft.runs
            } else if (project.extensions.findByName('runs') != null) {
                runsContainer = project.extensions.runs
            }
            if (runsContainer == null) {
                project.logger.warn("Sightline: no runs container found for loader '${loader}'")
                return
            }
            def run = runsContainer.findByName('sightline')
            if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'create', String)) {
                run = runsContainer.create('sightline')
            } else if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'register', String)) {
                run = runsContainer.register('sightline').get()
            }
            if (run == null) {
                project.logger.warn("Sightline: could not create '${loader}' run named sightline")
                return
            }
            if (run.hasProperty('workingDirectory')) {
                run.workingDirectory = project.layout.projectDirectory.dir(runDir)
            }
            if (run.metaClass.respondsTo(run, 'systemProperty', String, Object)) {
                run.systemProperty 'sightline', scenarioId.get()
                run.systemProperty 'sightline.step-timeout-seconds', timeout.get()
                run.systemProperty 'sightline.report-file', reportFile
                run.systemProperty 'sightline.vanilla-server-dir', vanillaServerDir
            }
        } catch (Throwable ex) {
            project.logger.warn("Sightline could not fully configure ${loader} run: ${ex.message}")
        }
    }

    private static void configureDisplayOnRunTask(Project project, SightlineExtension extension) {
        def displayMode = project.providers.gradleProperty('sightlineDisplay').orElse('display')
        project.tasks.configureEach { task ->
            if (task.name != 'runSightline') {
                return
            }
            task.dependsOn('prepareSightlineRun')
            task.doFirst {
                def mode = displayMode.get()
                if (!(mode in ['display', 'xvfb'])) {
                    throw new GradleException("Invalid -PsightlineDisplay='${mode}'. Allowed values: display, xvfb.")
                }
                if (task.hasProperty('useXvfb')) {
                    task.useXvfb = (mode == 'xvfb')
                }
                if (mode == 'xvfb') {
                    task.environment 'LIBGL_ALWAYS_SOFTWARE', '1'
                }
                def linux = org.gradle.internal.os.OperatingSystem.current().isLinux()
                if (mode == 'display') {
                    def display = System.getenv('DISPLAY')
                    if (linux && (display == null || display.isBlank())) {
                        throw new GradleException(
                                'sightlineDisplay=display requires $DISPLAY. '
                                        + 'Use -PsightlineDisplay=xvfb for headless/CI runs.')
                    }
                } else if (mode == 'xvfb') {
                    if (!linux) {
                        throw new GradleException('sightlineDisplay=xvfb is only supported on Linux.')
                    }
                    def which = new ProcessBuilder('which', 'xvfb-run').redirectErrorStream(true).start()
                    which.waitFor()
                    if (which.exitValue() != 0) {
                        throw new GradleException(
                                'sightlineDisplay=xvfb requires xvfb-run on PATH. Install with: apt install xvfb')
                    }
                }
            }
        }
    }

    private static void registerRunAllTask(Project project, SightlineExtension extension) {
        def displayMode = project.providers.gradleProperty('sightlineDisplay').orElse('display')
        def timeoutProperty = project.providers.gradleProperty('sightlineTimeout')
        def projectDirFile = project.layout.projectDirectory.asFile
        def resultsRoot = project.layout.buildDirectory.dir("${extension.outputDir}/results")
        def testsDirs = extension.allTestsDirs()

        def execOps = project.objects.newInstance(SightlineExecOps).execOps

        project.tasks.register('runAllSightlineTests') {
            group = 'verification'
            description = 'Discover all type:test YAML scenarios and run each via runSightline'

            doLast {
                def missing = testsDirs.findAll { !it.isDirectory() }
                if (missing) {
                    throw new GradleException("Sightline tests directory not found: ${missing}")
                }
                def scenarioIds = discoverScenarioTestIds(testsDirs)
                if (scenarioIds.isEmpty()) {
                    throw new GradleException("No scenario tests (type: test) found under ${testsDirs}")
                }
                logger.lifecycle("Discovered ${scenarioIds.size()} Sightline test(s): ${scenarioIds.join(', ')}")

                def gradleWrapper = new File(projectDirFile, 'gradlew')
                def gradleCommand = gradleWrapper.exists() ? gradleWrapper.absolutePath : 'gradle'
                def failed = []
                def resultsDir = resultsRoot.get().asFile
                project.delete(resultsDir)
                resultsDir.mkdirs()

                scenarioIds.each { String scenarioId ->
                    logger.lifecycle("Running Sightline test '${scenarioId}'")
                    def args = [gradleCommand, 'runSightline', "-Psightline=${scenarioId}"]
                    args << "-PsightlineDisplay=${displayMode.get()}"
                    if (timeoutProperty.isPresent()) {
                        args << "-PsightlineTimeout=${timeoutProperty.get()}"
                    }
                    def result = execOps.exec {
                        workingDir projectDirFile
                        commandLine args
                        ignoreExitValue = true
                    }

                    def archiveDir = new File(resultsDir, scenarioId)
                    archiveDir.mkdirs()
                    ['report.xml', 'metrics.json', 'diagnostics.txt'].each { String fileName ->
                        def source = new File(projectDirFile, "build/${extension.outputDir}/${fileName}")
                        if (source.isFile()) {
                            project.copy {
                                from source
                                into archiveDir
                            }
                        }
                    }
                    def screenshots = new File(projectDirFile, "build/${extension.outputDir}/run/screenshots")
                    if (screenshots.isDirectory()) {
                        project.copy {
                            from screenshots
                            into new File(archiveDir, 'screenshots')
                        }
                    }

                    if (result.exitValue != 0) {
                        failed << scenarioId
                        logger.error("Sightline test '${scenarioId}' failed (exit ${result.exitValue})")
                    } else {
                        logger.lifecycle("Sightline test '${scenarioId}' passed")
                    }
                }

                if (!failed.isEmpty()) {
                    throw new GradleException(
                            "Failed Sightline test(s): ${failed.join(', ')} "
                                    + "(${failed.size()} of ${scenarioIds.size()})")
                }
                logger.lifecycle("All ${scenarioIds.size()} Sightline test(s) passed")
            }
        }
    }

    private static List discoverScenarioTestIds(List<File> scenarioTestsRoots) {
        def ids = [] as TreeSet
        scenarioTestsRoots.each { File scenarioTestsRoot ->
            projectFileTree(scenarioTestsRoot).each { File yamlFile ->
                if (!isScenarioTestYaml(yamlFile)) {
                    return
                }
                def name = yamlFile.name
                def extension = name.lastIndexOf('.')
                def id = extension < 0 ? name : name.substring(0, extension)
                if (!ids.add(id)) {
                    throw new GradleException("Duplicate scenario test id '${id}' under ${scenarioTestsRoots}")
                }
            }
        }
        return ids as List
    }

    private static Collection<File> projectFileTree(File root) {
        def files = []
        root.eachFileRecurse { File f ->
            if (f.isFile() && (f.name.endsWith('.yaml') || f.name.endsWith('.yml'))) {
                files << f
            }
        }
        return files
    }

    private static boolean isScenarioTestYaml(File yamlFile) {
        def text = yamlFile.getText('UTF-8').replace('\r\n', '\n')
        if (!text.startsWith('---\n')) {
            return false
        }
        def closing = text.indexOf('\n---\n', 4)
        if (closing < 0) {
            return false
        }
        return text.substring(4, closing).readLines().any { line ->
            line.trim() == 'type: test'
        }
    }
}

interface SightlineExecOps {
    @javax.inject.Inject
    org.gradle.process.ExecOperations getExecOps()
}

