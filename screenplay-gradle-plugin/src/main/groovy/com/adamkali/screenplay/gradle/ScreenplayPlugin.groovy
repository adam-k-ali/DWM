package com.adamkali.screenplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException

class ScreenplayPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('screenplay', ScreenplayExtension, project)

        project.afterEvaluate {
            def loader = resolveLoader(project, extension)
            extension.loader = loader

            registerPrepareTask(project, extension)
            configureLoaderRun(project, extension, loader)
            configureDisplayOnRunTask(project, extension)
            registerRunAllTask(project, extension)
        }
    }

    private static String resolveLoader(Project project, ScreenplayExtension extension) {
        if (extension.loader != null && !extension.loader.isBlank()) {
            def value = extension.loader.trim().toLowerCase()
            if (!(value in ['fabric', 'forge', 'neoforge'])) {
                throw new GradleException("screenplay.loader must be fabric, forge, or neoforge (was '${extension.loader}')")
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
                    "Screenplay requires Fabric Loom, ForgeGradle, or NeoGradle. " +
                            "Apply one of those plugins, or set screenplay.loader explicitly.")
        }
        throw new GradleException(
                "Multiple mod-loader plugins detected. Set screenplay.loader to fabric, forge, or neoforge.")
    }

    private static void registerPrepareTask(Project project, ScreenplayExtension extension) {
        def displayMode = project.providers.gradleProperty('screenplayDisplay').orElse('display')
        project.tasks.register('prepareScreenplayRun') {
            group = 'verification'
            description = 'Prepare Screenplay run directory, options.txt, and vanilla server jar path'
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
                    logger.warn('Could not resolve Minecraft server jar path for Screenplay vanilla-server harness')
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
            // Fall through to non-Loom resolution.
        }
        def override = project.findProperty('screenplayServerJar')
        if (override != null) {
            def file = project.file(override.toString())
            if (file.isFile()) {
                return file
            }
        }
        // Best-effort: locate a cached official server jar under the Gradle caches.
        def userHome = System.getProperty('user.home', '')
        def version = project.findProperty('minecraft_version')?.toString()
        if (userHome && version) {
            def candidates = [
                    new File("${userHome}/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/server/${version}/server-${version}.jar"),
                    new File("${userHome}/.gradle/caches/minecraft/${version}/server.jar"),
            ]
            def found = candidates.find { it.isFile() }
            if (found != null) {
                return found
            }
        }
        return null
    }

    private static void configureLoaderRun(Project project, ScreenplayExtension extension, String loader) {
        def scenarioId = project.providers.gradleProperty('screenplay').orElse('createWorld')
        def timeout = project.providers.gradleProperty('screenplayTimeout').orElse('30')
        def reportFile = project.layout.buildDirectory.file("${extension.outputDir}/report.xml").get().asFile.absolutePath
        def vanillaServerDir = project.layout.buildDirectory.dir("${extension.outputDir}/vanilla-server").get().asFile.absolutePath
        def runDir = extension.runDir

        if (loader == 'fabric') {
            def loom = project.extensions.findByName('loom')
            if (loom == null) {
                throw new GradleException('Fabric loader selected but loom extension is missing')
            }
            // Prefer configuring an existing run if the consumer already declared one.
            // Use name "screenplayClient" (not "screenplay") so NeoGradle sibling projects
            // do not treat it as an unknown NeoGradle run type.
            def runs = loom.runs
            def run = runs.findByName('screenplayClient') ?: runs.findByName('screenplay')
            if (run == null) {
                run = runs.create('screenplayClient')
                run.client()
                run.name = 'Screenplay'
            }
            run.property 'screenplay', scenarioId.get()
            run.property 'screenplay.step-timeout-seconds', timeout.get()
            run.property 'screenplay.report-file', reportFile
            run.property 'screenplay.vanilla-server-dir', vanillaServerDir
            run.runDir runDir

            // Stable task name for docs/CI regardless of Loom run config name.
            if (project.tasks.findByName('runScreenplay') == null) {
                project.tasks.register('runScreenplay') {
                    group = 'verification'
                    description = 'Run a Screenplay YAML scenario in the real Minecraft client'
                    dependsOn 'runScreenplayClient'
                }
            }
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
                throw new GradleException("Screenplay: no runs container found for loader '${loader}'")
            }
            def run
            if (loader == 'neoforge') {
                // NeoGradle findByName/create of unknown run types throws — only use client/server.
                run = runsContainer.findByName('client')
            } else {
                run = runsContainer.findByName('screenplayClient') ?: runsContainer.findByName('screenplay')
                if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'create', String)) {
                    run = runsContainer.create('screenplayClient')
                } else if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'register', String)) {
                    run = runsContainer.register('screenplayClient').get()
                }
                if (run == null) {
                    run = runsContainer.findByName('client')
                }
            }
            if (run == null) {
                throw new GradleException("Screenplay: could not create or find a client run for '${loader}'")
            }
            if (run.hasProperty('workingDirectory')) {
                run.workingDirectory = project.file(runDir)
            }
            // NeoGradle NamedDomainObjectContainer uses workingDirectory = ...
            try {
                run.workingDirectory project.layout.projectDirectory.dir(runDir)
            } catch (Throwable ignored) {
            }
            if (run.metaClass.respondsTo(run, 'systemProperty', String, Object)) {
                run.systemProperty 'screenplay', scenarioId.get()
                run.systemProperty 'screenplay.step-timeout-seconds', timeout.get()
                run.systemProperty 'screenplay.report-file', reportFile
                run.systemProperty 'screenplay.vanilla-server-dir', vanillaServerDir
            } else {
                try {
                    run.systemProperty 'screenplay', scenarioId.get()
                    run.systemProperty 'screenplay.step-timeout-seconds', timeout.get()
                    run.systemProperty 'screenplay.report-file', reportFile
                    run.systemProperty 'screenplay.vanilla-server-dir', vanillaServerDir
                } catch (Throwable ignored) {
                    project.logger.warn("Screenplay: could not set system properties on ${loader} run")
                }
            }

            // NeoGradle treats any task named run* as a run-type request. Avoid that prefix.
            def screenplayTaskName = loader == 'neoforge' ? 'executeScreenplay' : 'runScreenplay'
            def delegateCandidates = loader == 'neoforge'
                    ? ['runClient']
                    : ['runScreenplayClient', 'run_screenplayClient', 'runClient']
            def delegateTask = delegateCandidates.find { name ->
                project.tasks.names.contains(name)
            }
            if (project.tasks.findByName(screenplayTaskName) == null) {
                project.tasks.register(screenplayTaskName) {
                    group = 'verification'
                    description = 'Run a Screenplay YAML scenario in the real Minecraft client'
                    if (delegateTask != null) {
                        dependsOn delegateTask
                    } else if (loader == 'neoforge') {
                        dependsOn 'runClient'
                    } else {
                        project.afterEvaluate {
                            def late = ['runScreenplayClient', 'run_screenplayClient', 'runClient'].find { name ->
                                project.tasks.names.contains(name)
                            }
                            if (late != null) {
                                project.tasks.named(screenplayTaskName).configure { dependsOn late }
                            } else {
                                logger.warn("Screenplay: no client run task found for ${loader}; ${screenplayTaskName} may no-op")
                            }
                        }
                    }
                }
            }
            project.extensions.extraProperties.set('screenplayRunTask', screenplayTaskName)
        } catch (Throwable ex) {
            throw new GradleException("Screenplay could not configure ${loader} run: ${ex.message}", ex)
        }
    }

    private static void configureDisplayOnRunTask(Project project, ScreenplayExtension extension) {
        def displayMode = project.providers.gradleProperty('screenplayDisplay').orElse('display')
        def screenplayRunTask = project.providers.provider {
            project.extensions.extraProperties.has('screenplayRunTask')
                    ? project.extensions.extraProperties.get('screenplayRunTask').toString()
                    : 'runScreenplay'
        }
        project.tasks.configureEach { task ->
            def runTaskName = screenplayRunTask.get()
            if (!(task.name in [runTaskName, 'runScreenplay', 'runScreenplayClient', 'executeScreenplay'])) {
                return
            }
            // Only attach prepare/display gating to the Screenplay entry task (and Fabric client run).
            if (!(task.name in [runTaskName, 'runScreenplay', 'runScreenplayClient', 'executeScreenplay'])) {
                return
            }
            task.dependsOn('prepareScreenplayRun')

            def mode = displayMode.get()
            if (!(mode in ['display', 'xvfb'])) {
                throw new GradleException("Invalid -PscreenplayDisplay='${mode}'. Allowed values: display, xvfb.")
            }
            // Loom run tasks are JavaExec; the runScreenplay alias may be a plain DefaultTask.
            if (task instanceof org.gradle.process.JavaExecSpec) {
                if (task.hasProperty('useXvfb')) {
                    task.useXvfb = (mode == 'xvfb')
                }
                if (mode == 'xvfb') {
                    task.environment 'LIBGL_ALWAYS_SOFTWARE', '1'
                }
            }

            task.doFirst {
                def currentMode = displayMode.get()
                def linux = org.gradle.internal.os.OperatingSystem.current().isLinux()
                if (currentMode == 'display') {
                    def display = System.getenv('DISPLAY')
                    if (linux && (display == null || display.isBlank())) {
                        throw new GradleException(
                                'screenplayDisplay=display requires $DISPLAY. '
                                        + 'Use -PscreenplayDisplay=xvfb for headless/CI runs.')
                    }
                } else if (currentMode == 'xvfb') {
                    if (!linux) {
                        throw new GradleException('screenplayDisplay=xvfb is only supported on Linux.')
                    }
                    def which = new ProcessBuilder('which', 'xvfb-run').redirectErrorStream(true).start()
                    which.waitFor()
                    if (which.exitValue() != 0) {
                        throw new GradleException(
                                'screenplayDisplay=xvfb requires xvfb-run on PATH. Install with: apt install xvfb')
                    }
                }
            }
        }
    }

    private static void registerRunAllTask(Project project, ScreenplayExtension extension) {
        def displayMode = project.providers.gradleProperty('screenplayDisplay').orElse('display')
        def timeoutProperty = project.providers.gradleProperty('screenplayTimeout')
        def projectDirFile = project.layout.projectDirectory.asFile
        def resultsRoot = project.layout.buildDirectory.dir("${extension.outputDir}/results")
        def testsDirs = extension.allTestsDirs()

        def execOps = project.objects.newInstance(ScreenplayExecOps).execOps

        project.tasks.register('runScreenplayTests') {
            group = 'verification'
            description = 'Discover all type:test YAML scenarios and run each via runScreenplay'
            // NeoGradle rejects unknown run* task names during registration of sibling tasks;
            // runScreenplayTests is fine as a plain task (not mapped to a run config).

            doLast {
                def missing = testsDirs.findAll { !it.isDirectory() }
                if (missing) {
                    throw new GradleException("Screenplay tests directory not found: ${missing}")
                }
                def scenarioIds = discoverScenarioTestIds(testsDirs)
                if (scenarioIds.isEmpty()) {
                    throw new GradleException("No scenario tests (type: test) found under ${testsDirs}")
                }
                logger.lifecycle("Discovered ${scenarioIds.size()} Screenplay test(s): ${scenarioIds.join(', ')}")

                def gradleWrapper = findGradleWrapper(projectDirFile)
                def gradleCommand = gradleWrapper != null ? gradleWrapper.absolutePath : 'gradle'
                def gradleWorkingDir = gradleWrapper != null ? gradleWrapper.parentFile : projectDirFile
                def failed = []
                def resultsDir = resultsRoot.get().asFile
                project.delete(resultsDir)
                resultsDir.mkdirs()

                scenarioIds.each { String scenarioId ->
                    logger.lifecycle("Running Screenplay test '${scenarioId}'")
                    def runTask = project.extensions.extraProperties.has('screenplayRunTask')
                            ? project.extensions.extraProperties.get('screenplayRunTask').toString()
                            : 'runScreenplay'
                    def args = [gradleCommand]
                    // Included-build consumers (dwm-loaders) are invoked via -p from the repo root wrapper.
                    def wrappersRoot = gradleWorkingDir
                    def loadersDir = new File(wrappersRoot, 'dwm-loaders')
                    if (loadersDir.isDirectory() && project.projectDir.absolutePath.startsWith(loadersDir.absolutePath)) {
                        args << '-p'
                        args << 'dwm-loaders'
                        args << "${project.name}:${runTask}"
                    } else if (project.path != ':') {
                        args << "${project.path}:${runTask}"
                    } else {
                        args << runTask
                    }
                    args << "-Pscreenplay=${scenarioId}"
                    args << "-PscreenplayDisplay=${displayMode.get()}"
                    if (timeoutProperty.isPresent()) {
                        args << "-PscreenplayTimeout=${timeoutProperty.get()}"
                    }
                    def result = execOps.exec {
                        workingDir gradleWorkingDir
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
                        logger.error("Screenplay test '${scenarioId}' failed (exit ${result.exitValue})")
                    } else {
                        logger.lifecycle("Screenplay test '${scenarioId}' passed")
                    }
                }

                if (!failed.isEmpty()) {
                    throw new GradleException(
                            "Failed Screenplay test(s): ${failed.join(', ')} "
                                    + "(${failed.size()} of ${scenarioIds.size()})")
                }
                logger.lifecycle("All ${scenarioIds.size()} Screenplay test(s) passed")
            }
        }
    }

    private static File findGradleWrapper(File startDir) {
        File dir = startDir
        while (dir != null) {
            def wrapper = new File(dir, 'gradlew')
            if (wrapper.isFile()) {
                return wrapper
            }
            dir = dir.parentFile
        }
        return null
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

interface ScreenplayExecOps {
    @javax.inject.Inject
    org.gradle.process.ExecOperations getExecOps()
}

