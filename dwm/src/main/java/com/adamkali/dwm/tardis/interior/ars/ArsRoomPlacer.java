package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * World edits for ARS pieces in one interior plot: placing piece and annex templates, clearing boxes,
 * opening and re-sealing doorways, and the console room's east doorway. No rules or bookkeeping; see
 * {@link ArsService}. Every box argument is plot-local ({@link ArsGeometry}); {@code plotOrigin} is the
 * world position of plot-local {@code (0, 0, 0)}.
 */
public final class ArsRoomPlacer {
    /**
     * Silent removal: no drops, no container spill ({@code preRemoveSideEffects}), and no shape updates
     * that would pop decor off its support while a room is cleared around it.
     */
    public static final int CLEAR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE
            | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS;

    private ArsRoomPlacer() {
    }

    public static Optional<StructureTemplate> template(ServerLevel world, Identifier id) {
        return world.getStructureManager().get(id);
    }

    /**
     * Places {@code template} for {@code piece} rotated so its rotated bounding box min corner sits on the
     * anchor cell's min corner. With a {@code clipLocal} box only blocks inside it are written (used to
     * re-seal one wall layer with the original blocks).
     *
     * @return false when vanilla placed nothing (empty template)
     */
    public static boolean placePiece(
            ServerLevel world,
            BlockPos plotOrigin,
            StructureTemplate template,
            ArsPlacedPiece piece,
            @Nullable BoundingBox clipLocal
    ) {
        BlockPos anchor = plotOrigin.offset(
                ArsGrid.cellMinBlockX(piece.cellX), ArsGrid.FLOOR_Y, ArsGrid.cellMinBlockZ(piece.cellZ));
        BlockPos position = ArsGeometry.templatePosition(
                anchor, piece.rotation, template.getSize().getX(), template.getSize().getZ());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(ArsGeometry.rotation(piece.rotation))
                .setIgnoreEntities(true);
        if (clipLocal != null) {
            settings.setBoundingBox(ArsGeometry.toWorld(clipLocal, plotOrigin));
        }
        return template.placeInWorld(world, position, position, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
    }

    /** Places the console annex lobby, unrotated at {@link ArsGeometry#ANNEX_ORIGIN}. */
    public static boolean placeAnnex(ServerLevel world, BlockPos plotOrigin) {
        Optional<StructureTemplate> annex = template(world, ArsGeometry.ANNEX_STRUCTURE_ID);
        if (annex.isEmpty()) {
            return false;
        }
        BlockPos position = plotOrigin.offset(ArsGeometry.ANNEX_ORIGIN);
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        return annex.get().placeInWorld(world, position, position, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
    }

    /** Opens the 2×3 doorway through the console room's east wall. */
    public static void carveConsoleDoorway(ServerLevel world, BlockPos plotOrigin) {
        fill(world, plotOrigin, ArsGeometry.consoleDoorwayBox(), Blocks.AIR.defaultBlockState());
    }

    /** Opens both wall layers of an open link (for the root link the annex side is already open). */
    public static void openDoorway(ServerLevel world, BlockPos plotOrigin, ArsLayoutLogic.Link link) {
        fill(world, plotOrigin, ArsGeometry.doorwayBox(link), Blocks.AIR.defaultBlockState());
    }

    /**
     * Clears a box to air without drops or container spill, then discards any item entities left in or
     * just around it.
     */
    public static void clearBox(ServerLevel world, BlockPos plotOrigin, BoundingBox local) {
        fill(world, plotOrigin, local, Blocks.AIR.defaultBlockState());
        AABB around = AABB.of(ArsGeometry.toWorld(local, plotOrigin)).inflate(1.0);
        world.getEntitiesOfClass(ItemEntity.class, around).forEach(Entity::discard);
    }

    /** Synchronously loads every chunk the box touches. */
    public static void forceLoad(ServerLevel world, BlockPos plotOrigin, BoundingBox local) {
        FirstDoctorConsoleRoomPlacer.forceLoadChunks(world, ArsGeometry.chunkBounds(ArsGeometry.toWorld(local, plotOrigin)));
    }

    /** Enables light storage and propagation for every chunk the box touches. */
    public static void enableLighting(ServerLevel world, BlockPos plotOrigin, BoundingBox local) {
        BotiInteriorSampler.enableChunkLighting(world, ArsGeometry.chunkBounds(ArsGeometry.toWorld(local, plotOrigin)));
    }

    private static void fill(ServerLevel world, BlockPos plotOrigin, BoundingBox local, BlockState state) {
        BoundingBox box = ArsGeometry.toWorld(local, plotOrigin);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    world.setBlock(pos.set(x, y, z), state, CLEAR_FLAGS);
                }
            }
        }
    }
}
