package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
 * World edits for ARS pieces in one interior plot: placing piece and lobby templates, clearing boxes,
 * opening and re-sealing doorways, and the console room's doorways. No rules or bookkeeping; see
 * {@link ArsService}. Every box argument is grid-local ({@link ArsGeometry}); {@code gridOrigin} is the
 * world position of grid-local {@code (0, 0, 0)} ({@link ArsGrid#gridOrigin}, the plot's window origin).
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
            BlockPos gridOrigin,
            StructureTemplate template,
            ArsPlacedPiece piece,
            @Nullable BoundingBox clipLocal
    ) {
        BlockPos anchor = gridOrigin.offset(
                ArsGrid.cellMinBlockX(piece.cellX), ArsGrid.FLOOR_Y, ArsGrid.cellMinBlockZ(piece.cellZ));
        BlockPos position = ArsGeometry.templatePosition(
                anchor, piece.rotation, template.getSize().getX(), template.getSize().getZ());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(ArsGeometry.rotation(piece.rotation))
                .setIgnoreEntities(true);
        if (clipLocal != null) {
            settings.setBoundingBox(ArsGeometry.toWorld(clipLocal, gridOrigin));
        }
        return template.placeInWorld(world, position, position, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
    }

    /** Places a root entrance's lobby, unrotated at {@link ArsGeometry.RootEntrance#lobbyOrigin()}. */
    public static boolean placeLobby(ServerLevel world, BlockPos gridOrigin, ArsGeometry.RootEntrance entrance) {
        Optional<StructureTemplate> lobby = template(world, entrance.lobbyTemplate());
        if (lobby.isEmpty()) {
            return false;
        }
        BlockPos position = gridOrigin.offset(entrance.lobbyOrigin());
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        return lobby.get().placeInWorld(world, position, position, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
    }

    /** Clears a root entrance's lobby to air. */
    public static void clearLobby(ServerLevel world, BlockPos gridOrigin, ArsGeometry.RootEntrance entrance) {
        clearBox(world, gridOrigin, entrance.lobbyBox());
    }

    /** Opens a root entrance's 2×3 doorway through the console room's wall. */
    public static void carveConsoleDoorway(ServerLevel world, BlockPos gridOrigin, ArsGeometry.RootEntrance entrance) {
        fill(world, gridOrigin, entrance.consoleDoorwayBox(), Blocks.AIR.defaultBlockState());
    }

    /**
     * Restores the console room's wall in a root entrance's doorway by re-placing the console room template at
     * the plot {@code anchor}, bounded to the doorway (grid-local, moved by {@code gridOrigin}). On the west side
     * this puts back the decorative doors.
     *
     * @return false when the console room template is missing
     */
    public static boolean restoreConsoleWall(
            ServerLevel world,
            BlockPos anchor,
            BlockPos gridOrigin,
            ArsGeometry.RootEntrance entrance
    ) {
        Optional<StructureTemplate> room = template(world, TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
        if (room.isEmpty()) {
            return false;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .setBoundingBox(ArsGeometry.toWorld(entrance.consoleDoorwayBox(), gridOrigin));
        return room.get().placeInWorld(world, anchor, anchor, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
    }

    /** Opens both wall layers of an open link (for a root link the lobby side is already open). */
    public static void openDoorway(ServerLevel world, BlockPos gridOrigin, ArsLayoutLogic.Link link) {
        fill(world, gridOrigin, ArsGeometry.doorwayBox(link), Blocks.AIR.defaultBlockState());
    }

    /**
     * Clears a box to air without drops or container spill, then discards any item entities left in or
     * just around it.
     */
    public static void clearBox(ServerLevel world, BlockPos gridOrigin, BoundingBox local) {
        fill(world, gridOrigin, local, Blocks.AIR.defaultBlockState());
        AABB around = AABB.of(ArsGeometry.toWorld(local, gridOrigin)).inflate(1.0);
        world.getEntitiesOfClass(ItemEntity.class, around).forEach(Entity::discard);
    }

    /** Discards every non-player entity in a box (mobs, items, item frames, ...). */
    public static void discardNonPlayerEntities(ServerLevel world, BlockPos gridOrigin, BoundingBox local) {
        AABB box = AABB.of(ArsGeometry.toWorld(local, gridOrigin));
        List<Entity> entities = world.getEntitiesOfClass(Entity.class, box, entity -> !(entity instanceof Player));
        entities.forEach(Entity::discard);
    }

    /** Whether any player in {@code world} overlaps the box. */
    public static boolean hasPlayerInside(ServerLevel world, BlockPos gridOrigin, BoundingBox local) {
        AABB box = AABB.of(ArsGeometry.toWorld(local, gridOrigin));
        for (Player player : world.players()) {
            if (player.isAlive() && player.getBoundingBox().intersects(box)) {
                return true;
            }
        }
        return false;
    }

    /** Synchronously loads every chunk the box touches. */
    public static void forceLoad(ServerLevel world, BlockPos gridOrigin, BoundingBox local) {
        FirstDoctorConsoleRoomPlacer.forceLoadChunks(world, ArsGeometry.chunkBounds(ArsGeometry.toWorld(local, gridOrigin)));
    }

    /** Enables light storage and propagation for every chunk the box touches. */
    public static void enableLighting(ServerLevel world, BlockPos gridOrigin, BoundingBox local) {
        BotiInteriorSampler.enableChunkLighting(world, ArsGeometry.chunkBounds(ArsGeometry.toWorld(local, gridOrigin)));
    }

    private static void fill(ServerLevel world, BlockPos gridOrigin, BoundingBox local, BlockState state) {
        BoundingBox box = ArsGeometry.toWorld(local, gridOrigin);
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
