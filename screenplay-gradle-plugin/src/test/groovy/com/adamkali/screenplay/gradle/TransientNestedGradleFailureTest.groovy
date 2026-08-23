package com.adamkali.screenplay.gradle

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class TransientNestedGradleFailureTest {
    @Test
    void detectsNeoForgeClientCacheFailure() {
        def log = '''
> Task :neoforge:cacheVersionExecutableClient26.2 FAILED
Execution failed for task ':neoforge:cacheVersionExecutableClient26.2'
> Failed to execute stage: Default[outputs=[...], execute=net.neoforged.gradle.common.tasks.MinecraftArtifactFileCacheProvider$$Lambda]
BUILD FAILED in 50s
'''
        assertTrue(ScreenplayPlugin.isTransientNestedGradleFailure(log))
    }

    @Test
    void detectsNetworkTimeoutHints() {
        assertTrue(ScreenplayPlugin.isTransientNestedGradleFailure('Read timed out while downloading client.jar'))
        assertTrue(ScreenplayPlugin.isTransientNestedGradleFailure('Could not GET https://piston-data.mojang.com/...'))
    }

    @Test
    void doesNotRetryInGameScenarioFailures() {
        def log = '''
[Screenplay]: Scenario 'createWorld' failed: timed out waiting for world
BUILD SUCCESSFUL in 2m
'''
        assertFalse(ScreenplayPlugin.isTransientNestedGradleFailure(log))
        assertFalse(ScreenplayPlugin.isTransientNestedGradleFailure(''))
        assertFalse(ScreenplayPlugin.isTransientNestedGradleFailure(null))
    }
}
