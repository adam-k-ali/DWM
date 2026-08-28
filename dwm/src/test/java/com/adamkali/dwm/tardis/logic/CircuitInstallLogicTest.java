package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircuitInstallLogicTest {
    @TempDir
    Path tempDir;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        clearCache();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCache();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void matchingControl_installsBrokenCircuit() {
        TardisDataModel model = ownedUnfinished();
        CircuitInstallLogic.Result result = CircuitInstallLogic.evaluateConsole(
                model,
                model.ownerUuid,
                TardisCircuit.STABILISERS,
                LookTarget.STABILISERS,
                false
        );
        assertEquals(CircuitInstallLogic.Result.INSTALLED, result);
        CircuitFittedLogic.setFitted(model, TardisCircuit.STABILISERS, true);
        assertTrue(CircuitFittedLogic.isFitted(model, TardisCircuit.STABILISERS));
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR));
    }

    @Test
    void alreadyFitted_doesNotInstall() {
        TardisDataModel model = ownedUnfinished();
        CircuitFittedLogic.setFitted(model, TardisCircuit.WAYPOINTS, true);
        assertEquals(
                CircuitInstallLogic.Result.ALREADY_FITTED,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.WAYPOINTS,
                        LookTarget.WAYPOINT_SELECTOR,
                        false
                )
        );
    }

    @Test
    void wrongControl_doesNotMatch() {
        TardisDataModel model = ownedUnfinished();
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.STABILISERS,
                        LookTarget.PLANET_LOCATOR,
                        false
                )
        );
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.STABILISERS,
                        LookTarget.BIOME_SELECTOR,
                        false
                )
        );
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.STABILISERS,
                        LookTarget.REFUELER,
                        false
                )
        );
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.STABILISERS));
    }

    @Test
    void visitor_isNotOwner() {
        TardisDataModel model = ownedUnfinished();
        assertEquals(
                CircuitInstallLogic.Result.NOT_OWNER,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        UUID.randomUUID(),
                        TardisCircuit.STABILISERS,
                        LookTarget.STABILISERS,
                        false
                )
        );
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.STABILISERS));
    }

    @Test
    void coordinateLocks_matchAnyAxis() {
        TardisDataModel model = ownedUnfinished();
        for (LookTarget axis : new LookTarget[]{
                LookTarget.COORDINATE_LOCK_X,
                LookTarget.COORDINATE_LOCK_Y,
                LookTarget.COORDINATE_LOCK_Z
        }) {
            assertEquals(
                    CircuitInstallLogic.Result.INSTALLED,
                    CircuitInstallLogic.evaluateConsole(
                            model,
                            model.ownerUuid,
                            TardisCircuit.COORDINATE_LOCKS,
                            axis,
                            false
                    ),
                    axis.name()
            );
        }
    }

    @Test
    void remoteSummon_requiresRemoteInOtherHand() {
        TardisDataModel model = ownedUnfinished();
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateRemote(
                        model,
                        model.ownerUuid,
                        TardisCircuit.REMOTE_SUMMON,
                        false
                )
        );
        assertEquals(
                CircuitInstallLogic.Result.INSTALLED,
                CircuitInstallLogic.evaluateRemote(
                        model,
                        model.ownerUuid,
                        TardisCircuit.REMOTE_SUMMON,
                        true
                )
        );
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateRemote(
                        model,
                        model.ownerUuid,
                        TardisCircuit.STABILISERS,
                        true
                )
        );
    }

    @Test
    void remoteSummon_onConsoleMatchesWhenOtherHandIsRemote() {
        TardisDataModel model = ownedUnfinished();
        assertEquals(
                CircuitInstallLogic.Result.WRONG_TARGET,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.REMOTE_SUMMON,
                        LookTarget.PLANET_LOCATOR,
                        false
                )
        );
        assertEquals(
                CircuitInstallLogic.Result.INSTALLED,
                CircuitInstallLogic.evaluateConsole(
                        model,
                        model.ownerUuid,
                        TardisCircuit.REMOTE_SUMMON,
                        LookTarget.PLANET_LOCATOR,
                        true
                )
        );
    }

    @Test
    void remoteSummon_noOwnedTardisIsNotOwner() {
        assertEquals(
                CircuitInstallLogic.Result.NOT_OWNER,
                CircuitInstallLogic.evaluateRemote(
                        null,
                        UUID.randomUUID(),
                        TardisCircuit.REMOTE_SUMMON,
                        true
                )
        );
    }

    @Test
    void independentOrder_waypointsWithoutPlanetLocator() {
        TardisDataModel model = ownedUnfinished();
        assertTrue(CircuitInstallLogic.matchesConsole(
                TardisCircuit.WAYPOINTS,
                LookTarget.WAYPOINT_SELECTOR,
                false
        ));
        CircuitFittedLogic.setFitted(model, TardisCircuit.WAYPOINTS, true);
        assertTrue(CircuitFittedLogic.isFitted(model, TardisCircuit.WAYPOINTS));
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR));
    }

    @Test
    void overlayKeys_matchLang() {
        assertEquals("dwm.console.circuit_mismatch", CircuitInstallLogic.MISMATCH_KEY);
        assertEquals("dwm.console.circuit_already_fitted", CircuitInstallLogic.ALREADY_FITTED_KEY);
        assertEquals("dwm.console.circuit_installed", CircuitInstallLogic.INSTALLED_KEY);
        assertEquals("dwm.console.refueler", CircuitInstallLogic.controlTranslationKey(LookTarget.REFUELER));
        assertEquals("dwm.console.coordinate_locks", CircuitInstallLogic.controlTranslationKey(LookTarget.COORDINATE_LOCK_Y));
    }

    private static TardisDataModel ownedUnfinished() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        UUID owner = UUID.randomUUID();
        model.setOwner(owner);
        return model;
    }

    @SuppressWarnings("unchecked")
    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }
}
