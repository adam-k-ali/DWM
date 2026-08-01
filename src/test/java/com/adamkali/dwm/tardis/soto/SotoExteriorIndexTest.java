package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorIndexTest {

    @AfterEach
    void tearDown() {
        SotoExteriorIndex.clear();
    }

    @Test
    void registerAndResolve_findsTardisInFootprint() {
        UUID id = UUID.randomUUID();
        RegistryKey<World> overworld = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
        BlockPos exterior = new BlockPos(50, 70, -10);
        SotoExteriorIndex.register(id, overworld, exterior);

        assertTrue(SotoExteriorIndex.isRegistered(id));
        assertEquals(exterior, SotoExteriorIndex.getExteriorPos(id));
        assertEquals(id, SotoExteriorIndex.resolve(overworld, exterior));
        assertEquals(id, SotoExteriorIndex.resolve(overworld, exterior.add(2, 0, -1)));
        assertNull(SotoExteriorIndex.resolve(overworld, exterior.add(20, 0, 0)));
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
                RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")),
                new BlockPos(8, 64, 16)
        ));
    }
}
