package com.adamkali.screenplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaPluginExtension

class ScreenplayPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('screenplay', ScreenplayExtension, project)
        registerHarnessDependencies(project)

        project.afterEvaluate {
            def loader = resolveLoader(project, extension)
            extension.loader = loader

            validateToolchain(project)
            validateMinecraftVersion(project)
            registerPrepareTask(project, extension)
            configureLoaderRun(project, extension, loader)
            configureDisplayOnRunTask(project, extension)
            registerRunAllTask(project, extension)
        }
    }

    private static boolean shouldAddHarnessDependency(Project project) {
        def value = project.findProperty('screenplay.addHarnessDependency')
        if (value == null) {
            return true
        }
        def text = value.toString().trim()
        return !(text.equalsIgnoreCase('false') || text == '0' || text.equalsIgnoreCase('no'))
    }

    private static void registerHarnessDependencies(Project project) {
        if (!shouldAddHarnessDependency(project)) {
            return
        }
        def version = ScreenplayPluginVersions.screenplayVersion()
        ScreenplayHarnessRepository.register(project, version)
        def fabricAdded = new boolean[1]
        def addFabric = {
            if (fabricAdded[0]) {
                return
            }
            fabricAdded[0] = true
            addHarnessDependency(
                    project,
                    "com.adamkali.screenplay:screenplay-fabric:${version}",
                    'modRuntimeOnly'
            )
        }
        project.pluginManager.withPlugin('net.fabricmc.fabric-loom', addFabric)
        project.pluginManager.withPlugin('fabric-loom', addFabric)

        project.pluginManager.withPlugin('net.minecraftforge.gradle') {
            addHarnessDependency(
                    project,
                    "com.adamkali.screenplay:screenplay-forge:${version}",
                    'runtimeOnly'
            )
        }
        def neoAdded = new boolean[1]
        def addNeo = {
            if (neoAdded[0]) {
                return
            }
            neoAdded[0] = true
            addHarnessDependency(
                    project,
                    "com.adamkali.screenplay:screenplay-neoforge:${version}",
                    'runtimeOnly'
            )
        }
        project.pluginManager.withPlugin('net.neoforged.gradle.userdev', addNeo)
        project.pluginManager.withPlugin('net.neoforged.gradle.common', addNeo)
    }

    private static void addHarnessDependency(Project project, String notation, String preferredConfig) {
        def configName = project.configurations.findByName(preferredConfig) != null ? preferredConfig : 'runtimeOnly'
        project.dependencies.add(configName, notation)
    }

    private static void validateToolchain(Project project) {
        def javaExt = project.extensions.findByType(JavaPluginExtension)
        if (javaExt == null) {
            return
        }
        def languageVersion = javaExt.toolchain.languageVersion
        if (!languageVersion.isPresent()) {
            return
        }
        int version = languageVersion.get().asInt()
        int required = ScreenplayPluginVersions.requiredJavaVersion()
        if (version < required) {
            throw new GradleException(
                    "Screenplay requires Java ${required} (this project uses Java ${version}). "
                            + "Install Temurin ${required} and set "
                            + "java { toolchain { languageVersion = JavaLanguageVersion.of(${required}) } }.")
        }
    }

    private static void validateMinecraftVersion(Project project) {
        def expected = ScreenplayPluginVersions.minecraftVersion()
        def actual = readMinecraftVersion(project)
        if (actual == null || actual.isBlank()) {
            return
        }
        if (actual != expected) {
            throw new GradleException(
                    "Screenplay ${ScreenplayPluginVersions.screenplayVersion()} is built for Minecraft ${expected}, "
                            + "but this project uses Minecraft ${actual}. "
                            + "Use a Screenplay release that matches your Minecraft version.")
        }
    }

    private static String readMinecraftVersion(Project project) {
        try {
            def loomClass = Class.forName('net.fabricmc.loom.LoomGradleExtension')
            def loomExt = loomClass.getMethod('get', Project).invoke(null, project)
            def provider = loomExt.minecraftProvider
            def method = provider.class.methods.find { it.name == 'minecraftVersion' && it.parameterCount == 0 }
            if (method != null) {
                def value = method.invoke(provider)
                if (value != null && !value.toString().isBlank()) {
                    return value.toString()
                }
            }
            if (provider.hasProperty('minecraftVersion')) {
                def value = provider.minecraftVersion
                if (value != null && !value.toString().isBlank()) {
                    return value.toString()
                }
            }
        } catch (Throwable ignored) {
        }
        def property = project.findProperty('minecraft_version')
        return property == null ? null : property.toString()
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
                    "Screenplay requires Fabric Loom, ForgeGradle, or NeoGradle. "
                            + "Apply one of those plugins, or set screenplay.loader explicitly.")
        }
        throw new GradleException(
                "Multiple mod-loader plugins detected. Set screenplay.loader to fabric, forge, or neoforge.")
    }

    private static String testsDirsPropertyValue(ScreenplayExtension extension) {
        return extension.allTestsDirs().collect { it.absolutePath }.join(File.pathSeparator)
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
                if (ScreenplayScenarioDiscovery.needsStarter(extension.testsDir)) {
                    def starter = ScreenplayScenarioDiscovery.writeStarter(extension.testsDir)
                    logger.lifecycle("Wrote starter Screenplay scenario: ${starter}")
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

    private static void configureLoaderRun(Project project, ScreenplayExtension extension, String loader) {
        def scenarioIdProperty = project.providers.gradleProperty('screenplay')
        def timeout = project.providers.gradleProperty('screenplayTimeout').orElse('30')
        def baselinesDirProperty = project.providers.gradleProperty('screenplayBaselinesDir')
        def recordProperty = project.providers.gradleProperty('screenplayRecord')
        def reportFile = project.layout.buildDirectory.file("${extension.outputDir}/report.xml").get().asFile.absolutePath
        def vanillaServerDir = project.layout.buildDirectory.dir("${extension.outputDir}/vanilla-server").get().asFile.absolutePath
        def runDir = extension.runDir
        def testsDirsValue = testsDirsPropertyValue(extension)
        def baselinesDir = baselinesDirProperty.isPresent()
                ? project.file(baselinesDirProperty.get()).absolutePath
                : null
        def recordValue = recordProperty.isPresent() ? normalizeRecordProperty(recordProperty.get()) : null
        def configuredScenarioId = scenarioIdProperty.isPresent() && !scenarioIdProperty.get().isBlank()
                ? scenarioIdProperty.get().trim()
                : null

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
            if (configuredScenarioId != null) {
                run.property 'screenplay', configuredScenarioId
            }
            run.property 'screenplay.step-timeout-seconds', timeout.get()
            run.property 'screenplay.report-file', reportFile
            run.property 'screenplay.vanilla-server-dir', vanillaServerDir
            run.property 'screenplay.tests-dirs', testsDirsValue
            if (baselinesDir != null) {
                run.property 'screenplay.baselines-dir', baselinesDir
            }
            if (recordValue != null) {
                run.property 'screenplay.record', recordValue
            }
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
                project.logger.warn("Screenplay: no runs container found for loader '${loader}'")
                return
            }
            def run = runsContainer.findByName('screenplayClient') ?: runsContainer.findByName('screenplay')
            if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'create', String)) {
                run = runsContainer.create('screenplayClient')
            } else if (run == null && runsContainer.metaClass.respondsTo(runsContainer, 'register', String)) {
                run = runsContainer.register('screenplayClient').get()
            }
            if (run == null) {
                project.logger.warn("Screenplay: could not create '${loader}' run named screenplayClient")
                return
            }
            if (run.hasProperty('workingDirectory')) {
                run.workingDirectory = project.layout.projectDirectory.dir(runDir)
            }
            if (run.metaClass.respondsTo(run, 'systemProperty', String, Object)) {
                if (configuredScenarioId != null) {
                    run.systemProperty 'screenplay', configuredScenarioId
                }
                run.systemProperty 'screenplay.step-timeout-seconds', timeout.get()
                run.systemProperty 'screenplay.report-file', reportFile
                run.systemProperty 'screenplay.vanilla-server-dir', vanillaServerDir
                run.systemProperty 'screenplay.tests-dirs', testsDirsValue
                if (baselinesDir != null) {
                    run.systemProperty 'screenplay.baselines-dir', baselinesDir
                }
                if (recordValue != null) {
                    run.systemProperty 'screenplay.record', recordValue
                }
            }
        } catch (Throwable ex) {
            project.logger.warn("Screenplay could not fully configure ${loader} run: ${ex.message}")
        }
    }

    private static String normalizeRecordProperty(String raw) {
        def value = raw == null ? '' : raw.trim().toLowerCase()
        if (!(value in ['true', 'false'])) {
            throw new GradleException(
                    "Invalid -PscreenplayRecord='${raw}'. Allowed values: true, false.")
        }
        return value
    }

    private static void configureDisplayOnRunTask(Project project, ScreenplayExtension extension) {
        def displayMode = project.providers.gradleProperty('screenplayDisplay').orElse('display')
        def recordProperty = project.providers.gradleProperty('screenplayRecord')
        def scenarioIdProperty = project.providers.gradleProperty('screenplay')
        project.tasks.configureEach { task ->
            if (!(task.name in ['runScreenplay', 'runScreenplayClient'])) {
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
                if (recordProperty.isPresent() && normalizeRecordProperty(recordProperty.get()) == 'true') {
                    def ffmpeg = new ProcessBuilder('which', 'ffmpeg').redirectErrorStream(true).start()
                    ffmpeg.waitFor()
                    if (ffmpeg.exitValue() != 0) {
                        throw new GradleException(
                                'screenplayRecord=true requires ffmpeg on PATH. Install with: apt install ffmpeg')
                    }
                }
                if (task instanceof org.gradle.process.JavaExecSpec) {
                    def requested = scenarioIdProperty.isPresent() ? scenarioIdProperty.get() : null
                    try {
                        def scenarioId = ScreenplayScenarioDiscovery.resolveScenarioId(
                                requested,
                                extension.allTestsDirs()
                        )
                        task.systemProperty('screenplay', scenarioId)
                    } catch (IllegalStateException exception) {
                        throw new GradleException(exception.message, exception)
                    }
                }
            }
        }
    }

    private static void registerRunAllTask(Project project, ScreenplayExtension extension) {
        def displayMode = project.providers.gradleProperty('screenplayDisplay').orElse('display')
        def timeoutProperty = project.providers.gradleProperty('screenplayTimeout')
        def baselinesDirProperty = project.providers.gradleProperty('screenplayBaselinesDir')
        def recordProperty = project.providers.gradleProperty('screenplayRecord')
        def projectDirFile = project.layout.projectDirectory.asFile
        def resultsRoot = project.layout.buildDirectory.dir("${extension.outputDir}/results")

        def execOps = project.objects.newInstance(ScreenplayExecOps).execOps

        project.tasks.register('runScreenplayTests') {
            group = 'verification'
            description = 'Discover Screenplay suites and standalone type:test scenarios, then run each via runScreenplay'

            doLast {
                if (ScreenplayScenarioDiscovery.needsStarter(extension.testsDir)) {
                    def starter = ScreenplayScenarioDiscovery.writeStarter(extension.testsDir)
                    logger.lifecycle("Wrote starter Screenplay scenario: ${starter}")
                }
                def testsDirs = extension.allTestsDirs()
                def extraMissing = (extension.extraTestsDirs ?: []).findAll { it != null && !it.isDirectory() }
                if (extraMissing) {
                    throw new GradleException("Screenplay extraTestsDirs not found: ${extraMissing}")
                }
                def discovery = ScreenplayScenarioDiscovery.discoverRunIds(testsDirs)
                def scenarioIds = discovery.runIds() as List
                if (scenarioIds.isEmpty()) {
                    throw new GradleException(
                            "No scenario suites or standalone tests found under ${testsDirs}. "
                                    + "Add a YAML file with type: test.")
                }
                logger.lifecycle(
                        "Discovered ${scenarioIds.size()} Screenplay run(s): ${scenarioIds.join(', ')}"
                                + " (${discovery.suiteIds().size()} suite(s), "
                                + "${discovery.standaloneTestIds().size()} standalone test(s))")

                def gradleWrapper = new File(projectDirFile, 'gradlew')
                def gradleCommand = gradleWrapper.exists() ? gradleWrapper.absolutePath : 'gradle'
                def failed = []
                def resultsDir = resultsRoot.get().asFile
                project.delete(resultsDir)
                resultsDir.mkdirs()

                scenarioIds.each { String scenarioId ->
                    logger.lifecycle("Running Screenplay '${scenarioId}'")
                    def args = [gradleCommand, 'runScreenplay', "-Pscreenplay=${scenarioId}"]
                    args << "-PscreenplayDisplay=${displayMode.get()}"
                    if (timeoutProperty.isPresent()) {
                        args << "-PscreenplayTimeout=${timeoutProperty.get()}"
                    }
                    if (baselinesDirProperty.isPresent()) {
                        args << "-PscreenplayBaselinesDir=${baselinesDirProperty.get()}"
                    }
                    if (recordProperty.isPresent()) {
                        args << "-PscreenplayRecord=${recordProperty.get()}"
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
                    def recordings = new File(projectDirFile, "build/${extension.outputDir}/run/recordings")
                    if (recordings.isDirectory()) {
                        project.copy {
                            from recordings
                            into new File(archiveDir, 'recordings')
                        }
                    }

                    if (result.exitValue != 0) {
                        failed << scenarioId
                        logger.error("Screenplay '${scenarioId}' failed (exit ${result.exitValue})")
                    } else {
                        logger.lifecycle("Screenplay '${scenarioId}' passed")
                    }
                }

                if (!failed.isEmpty()) {
                    throw new GradleException(
                            "Failed Screenplay run(s): ${failed.join(', ')} "
                                    + "(${failed.size()} of ${scenarioIds.size()})")
                }
                logger.lifecycle("All ${scenarioIds.size()} Screenplay run(s) passed")
            }
        }
    }
}

interface ScreenplayExecOps {
    @javax.inject.Inject
    org.gradle.process.ExecOperations getExecOps()
}
