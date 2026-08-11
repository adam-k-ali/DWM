package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorIndexTest {

    @AfterEach
    void tearDown() {
        SotoExteriorIndex.clear();
    }

    @Test
    void registerAndResolve_findsTardisInFootprint() {
        UUID id = UUID.randomUUID();
        ResourceKey<Level> overworld = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        BlockPos exterior = new BlockPos(50, 70, -10);
        SotoExteriorIndex.register(id, overworld, exterior);

        assertTrue(SotoExteriorIndex.isRegistered(id));
        assertEquals(exterior, SotoExteriorIndex.getExteriorPos(id));
        assertEquals(id, SotoExteriorIndex.resolve(overworld, exterior));
        assertEquals(id, SotoExteriorIndex.resolve(overworld, exterior.offset(2, 0, -1)));
        assertNull(SotoExteriorIndex.resolve(overworld, exterior.offset(20, 0, 0)));
    }

    @Test
    void registerFromModel_usesExteriorLocation() {
        UUID id = UUID.randomUUID();
        TardisDataModel model = new TardisDataModel();
        model.uuid = id;
        model.setExteriorLocation("minecraft:overworld", 8, 64, 16, 4);

        SotoExteriorIndex.register(id, model);

        assertEquals(new BlockPos(8, 64, 16), SotoExteriorIndex.getExteriorPos(id));
        assertEquals(id, SotoExteriorIndex.resolve(
                ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "overworld")),
                new BlockPos(8, 64, 16)
        ));
    }

    @Test
    void unregister_removesTracking() {
        UUID id = UUID.randomUUID();
        ResourceKey<Level> overworld = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        BlockPos exterior = new BlockPos(12, 70, 4);
        SotoExteriorIndex.register(id, overworld, exterior);

        SotoExteriorIndex.unregister(id);

        assertFalse(SotoExteriorIndex.isRegistered(id));
        assertNull(SotoExteriorIndex.getExteriorPos(id));
        assertNull(SotoExteriorIndex.resolve(overworld, exterior));
    }
}
