package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.CardinalLighting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Ghost meshes bake face shading in, so a cardinal light type change must trigger a rebake. */
class PortalSceneStoreCardinalLightingTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void firstMeta_rebakesOnlyWhenTypeIsNotDefault() {
        assertTrue(PortalSceneStore.cardinalLightingChanged(null, atmosphere(CardinalLighting.Type.NETHER)));
        assertFalse(PortalSceneStore.cardinalLightingChanged(null, atmosphere(CardinalLighting.Type.DEFAULT)));
    }

    @Test
    void laterMeta_rebakesOnlyOnTypeChange() {
        PortalAtmosphere nether = atmosphere(CardinalLighting.Type.NETHER);
        PortalAtmosphere overworld = atmosphere(CardinalLighting.Type.DEFAULT);
        assertFalse(PortalSceneStore.cardinalLightingChanged(nether, atmosphere(CardinalLighting.Type.NETHER)));
        assertTrue(PortalSceneStore.cardinalLightingChanged(overworld, nether));
        assertTrue(PortalSceneStore.cardinalLightingChanged(nether, overworld));
    }

    private static PortalAtmosphere atmosphere(CardinalLighting.Type type) {
        return new PortalAtmosphere(
                Identifier.fromNamespaceAndPath("dwm", "tardis"),
                6000L,
                0.0f,
                0.0f,
                0,
                0,
                0.0f,
                0xFFFFFF,
                0x000000,
                PortalAtmosphere.DEFAULT_BLOCK_LIGHT_TINT,
                type
        );
    }
}
