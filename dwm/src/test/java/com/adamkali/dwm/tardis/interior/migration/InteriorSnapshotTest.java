package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.MinecraftTestBootstrap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteriorSnapshotTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void roundTripsBlocksStatesBlockEntitiesAndEntitiesThroughCompressedNbt() throws Exception {
        BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST)
                .setValue(StairBlock.HALF, Half.TOP)
                .setValue(BlockStateProperties.WATERLOGGED, true);
        CompoundTag chest = new CompoundTag();
        chest.putString("id", "minecraft:chest");
        chest.putString("CustomName", "\"Spares\"");
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:armor_stand");
        entity.putIntArray("UUID", new int[]{1, 2, 3, 4});
        InteriorSnapshot snapshot = new InteriorSnapshot(
                new BlockPos(640, 64, 1280),
                List.of(
                        new InteriorSnapshot.BlockEntry(new BlockPos(613, 64, 1253), stairs, null),
                        new InteriorSnapshot.BlockEntry(new BlockPos(676, -60, 1316), Blocks.CHEST.defaultBlockState(), chest),
                        new InteriorSnapshot.BlockEntry(new BlockPos(614, 319, 1253), stairs, null)),
                List.of(entity));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(snapshot.toTag(), bytes);
        CompoundTag read = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()), NbtAccounter.unlimitedHeap());
        InteriorSnapshot loaded = InteriorSnapshot.fromTag(read, BuiltInRegistries.BLOCK);

        assertEquals(snapshot.legacyAnchor(), loaded.legacyAnchor());
        assertEquals(snapshot.blocks().size(), loaded.blocks().size());
        for (int i = 0; i < snapshot.blocks().size(); i++) {
            assertEquals(snapshot.blocks().get(i).pos(), loaded.blocks().get(i).pos());
            assertEquals(snapshot.blocks().get(i).state(), loaded.blocks().get(i).state());
            assertEquals(snapshot.blocks().get(i).blockEntity(), loaded.blocks().get(i).blockEntity());
        }
        assertNull(loaded.blocks().get(0).blockEntity());
        assertEquals(chest, loaded.blocks().get(1).blockEntity());
        assertEquals(1, loaded.blockEntityCount());
        assertEquals(List.of(entity), loaded.entities());
        // The palette stores each distinct state once.
        assertEquals(2, read.getListOrEmpty("palette").size());
    }

    @Test
    void emptySnapshotsAreEmptyAndUnknownFormatsAreRefused() {
        InteriorSnapshot empty = new InteriorSnapshot(BlockPos.ZERO, List.of(), List.of());
        assertTrue(empty.isEmpty());
        assertTrue(InteriorSnapshot.fromTag(empty.toTag(), BuiltInRegistries.BLOCK).isEmpty());
        assertFalse(new InteriorSnapshot(BlockPos.ZERO, List.of(), List.of(new CompoundTag())).isEmpty());
        CompoundTag future = empty.toTag();
        future.putInt("format", 99);
        assertThrows(IllegalArgumentException.class, () -> InteriorSnapshot.fromTag(future, BuiltInRegistries.BLOCK));
    }
}
