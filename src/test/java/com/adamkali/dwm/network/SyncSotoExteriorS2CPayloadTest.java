package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SyncSotoExteriorS2CPayloadTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void fromSnapshot_roundTripsBlocksEntitiesShellAndAtmosphere() {
        UUID id = UUID.randomUUID();
        NbtCompound beNbt = new NbtCompound();
        beNbt.putString("id", "minecraft:chest");
        NbtCompound entityNbt = new NbtCompound();
        entityNbt.putString("id", "minecraft:cow");
        BotiEntitySample entity = new BotiEntitySample(2.5f, 1.0f, 4.0f, 90.0f, 10.0f, entityNbt);
        SotoAtmosphere atmosphere = new SotoAtmosphere(
                DimensionTypes.THE_NETHER_ID,
                13000L,
                0.4f,
                0.2f,
                0x112233,
                0x445566
        );

        SotoExteriorSnapshot snapshot = SotoExteriorSnapshot.of(
                id,
                7,
                Map.of(new BlockPos(1, 2, 3), Blocks.OAK_PLANKS.getDefaultState()),
                Map.of(new BlockPos(1, 2, 3), beNbt),
                List.of(entity),
                TardisChameleonVariant.FOURTH_DOCTOR_BOX,
                0.8f,
                true,
                12,
                atmosphere
        );

        SyncSotoExteriorS2CPayload payload = SyncSotoExteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(id, payload.tardisId());
        assertEquals(7, payload.revision());
        assertEquals(SotoExteriorSnapshot.FORMAT_VERSION_ATMOSPHERE, payload.formatVersion());
        assertEquals(TardisChameleonVariant.FOURTH_DOCTOR_BOX.getId(), payload.variantId());
        assertEquals(0.8f, payload.doorSwing(), 1e-4f);
        assertTrue(payload.isOpen());
        assertEquals(12, payload.exteriorRotation());
        assertEquals(1, payload.toBlockMap().size());
        assertEquals(Blocks.OAK_PLANKS, payload.toBlockMap().get(new BlockPos(1, 2, 3)).getBlock());
        assertEquals(1, payload.toBlockEntityMap().size());
        assertEquals(1, payload.toEntityList().size());
        assertEquals(TardisChameleonVariant.FOURTH_DOCTOR_BOX, payload.variant());
        assertEquals(DimensionTypes.THE_NETHER_ID, payload.atmosphere().dimensionEffectsId());
        assertEquals(13000L, payload.atmosphere().timeOfDay());
        assertEquals(0.4f, payload.atmosphere().rainGradient(), 1e-4f);
        assertEquals(0.2f, payload.atmosphere().thunderGradient(), 1e-4f);
        assertEquals(0x112233, payload.atmosphere().biomeSkyColor());
        assertEquals(0x445566, payload.atmosphere().biomeFogColor());
        assertEquals(SyncSotoExteriorS2CPayload.ID, payload.getId());
    }
}
