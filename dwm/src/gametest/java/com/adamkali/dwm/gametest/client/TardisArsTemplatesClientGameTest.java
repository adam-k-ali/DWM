package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Screenshots of the ARS structure templates, each placed on its own with {@link StructureTemplate#placeInWorld}
 * (no ARS logic). Every template is a closed box, so the interior shots ({@code ars_<name>[_n]}) are lit only by the
 * room's own blocks; then the ceiling is lifted off for a daylight overview ({@code ars_<name>_overview}).
 */
public class TardisArsTemplatesClientGameTest implements FabricClientGameTest {
    private static final BlockPos ORIGIN = new BlockPos(40, 90, 40);
    /** Cleared box around the origin, larger than the biggest template (24×14×24). */
    private static final int CLEAR = 30;
    private static final int LIGHT_WAIT_TICKS = 200;
    private static final int RENDER_TICKS = 40;
    /** Silent clearing: no drops and no shape updates that would pop decor off its support. */
    private static final int CLEAR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    /** A camera inside the template: template-local feet position and look target. */
    private record View(Vec3 eye, Vec3 target) {
    }

    private record Shot(String name, boolean lobby, List<View> views) {
    }

    private static final List<Shot> SHOTS = List.of(
            new Shot("corridor_straight", false, List.of(view(3.5, 1, 1.3, 4, 2.2, 7))),
            new Shot("corridor_corner", false, List.of(view(3.0, 1, 1.3, 7, 2.2, 4.5))),
            new Shot("corridor_t", false, List.of(view(4.0, 1, 5.6, 4, 2.6, 0.5))),
            new Shot("corridor_cross", false, List.of(view(4.0, 1, 1.3, 4, 2.0, 7))),
            new Shot("corridor_dead_end", false, List.of(view(4.0, 1, 1.2, 4, 2.0, 5.5))),
            new Shot("bedroom", false, List.of(
                    view(2.5, 1, 1.5, 15, 1.5, 5.5),
                    view(13.5, 1, 3.5, 1, 1.5, 3.5))),
            new Shot("library", false, List.of(
                    view(3.5, 1, 1.5, 12, 2.5, 13),
                    view(14.0, 1, 12.0, 3, 3.5, 4),
                    view(13.0, 3, 14.0, 3, 4, 1))),
            new Shot("ars_chamber", false, List.of(
                    view(11.5, 1, 1.5, 12, 6, 12),
                    view(2.5, 1, 13.5, 12, 7, 12),
                    view(20.0, 6, 4.0, 12, 6, 12))),
            new Shot("console_lobby_east", true, List.of(view(1.0, 1, 1.3, 1.0, 2.0, 4.6))),
            new Shot("console_lobby_west", true, List.of(view(1.5, 1, 1.3, 1.5, 2.0, 4.6))));

    private static View view(double x, double y, double z, double tx, double ty, double tz) {
        return new View(new Vec3(x, y, z), new Vec3(tx, ty, tz));
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            ClientGameTestSupport.toggleHud(context);
            world.getServer().runOnServer(server -> forceChunks(server.overworld(), true));
            try {
                for (Shot shot : SHOTS) {
                    Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis/ars/" + shot.name());
                    Vec3i size = world.getServer().computeOnServer(server -> place(server.overworld(), id, shot.lobby()));

                    for (int i = 0; i < shot.views().size(); i++) {
                        View view = shot.views().get(i);
                        BlockPos probe = BlockPos.containing(ORIGIN.getX() + view.eye().x, ORIGIN.getY() + view.eye().y + 1,
                                ORIGIN.getZ() + view.eye().z);
                        world.getServer().waitFor(server -> server.overworld().getBrightness(LightLayer.BLOCK, probe) > 0,
                                LIGHT_WAIT_TICKS);
                        aim(context, world, Vec3.atLowerCornerOf(ORIGIN).add(view.eye()),
                                Vec3.atLowerCornerOf(ORIGIN).add(view.target()));
                        context.waitTicks(RENDER_TICKS);
                        String suffix = shot.views().size() > 1 ? "_" + (i + 1) : "";
                        ClientGameTestSupport.screenshot(context, "ars_" + shot.name() + suffix);
                    }

                    // Overview: lift the ceiling off and look down from the north-west in daylight.
                    world.getServer().runOnServer(server -> {
                        ServerLevel level = server.overworld();
                        for (int x = 0; x < size.getX(); x++) {
                            for (int z = 0; z < size.getZ(); z++) {
                                level.setBlock(ORIGIN.offset(x, size.getY() - 1, z), Blocks.AIR.defaultBlockState(),
                                        CLEAR_FLAGS);
                            }
                        }
                    });
                    double span = Math.max(size.getX(), size.getZ());
                    Vec3 centre = new Vec3(ORIGIN.getX() + size.getX() / 2.0, ORIGIN.getY() + 1, ORIGIN.getZ() + size.getZ() / 2.0);
                    aim(context, world, centre.add(-span * 0.3, size.getY() + span * 0.5, -span * 0.3), centre);
                    context.waitTicks(RENDER_TICKS);
                    ClientGameTestSupport.screenshot(context, "ars_" + shot.name() + "_overview");
                }
            } finally {
                world.getServer().runOnServer(server -> forceChunks(server.overworld(), false));
            }
            ClientGameTestSupport.toggleHud(context);
        }
    }

    /** Clears the stage box, places the template at {@link #ORIGIN} and returns its size. */
    private static Vec3i place(ServerLevel level, Identifier id, boolean lobby) {
        for (int x = -2; x < CLEAR; x++) {
            for (int y = -2; y < 20; y++) {
                for (int z = -2; z < CLEAR; z++) {
                    level.setBlock(ORIGIN.offset(x, y, z), Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
                }
            }
        }
        // Nothing the previous template held may linger as a dropped item in the next shot.
        level.getEntitiesOfClass(ItemEntity.class, AABB.encapsulatingFullBlocks(ORIGIN.offset(-4, -4, -4), ORIGIN.offset(CLEAR + 4, 24, CLEAR + 4)))
                .forEach(Entity::discard);
        StructureTemplate template = level.getStructureManager().get(id)
                .orElseThrow(() -> new AssertionError("Missing structure template " + id));
        Vec3i size = template.getSize();
        if (lobby) {
            // Stand-ins for the console room's wall and the root cell's piece, which close the lobby's open ends.
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    level.setBlock(ORIGIN.offset(-1, y, z), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(ORIGIN.offset(size.getX(), y, z), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState(),
                            Block.UPDATE_CLIENTS);
                }
            }
        }
        if (!template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS)) {
            throw new AssertionError("Could not place " + id);
        }
        return size;
    }

    /** Teleports, waits for the position packet to land, then aims; the packet would otherwise reset the rotation. */
    private static void aim(ClientGameTestContext context, TestSingleplayerContext world, Vec3 feet, Vec3 target) {
        world.getServer().runCommand("tp @a " + feet.x + " " + feet.y + " " + feet.z);
        context.waitTicks(5);
        Vec3 delta = target.subtract(feet.add(0, EYE_HEIGHT, 0));
        float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
        context.getInput().lookAt(yaw, pitch);
    }

    private static final double EYE_HEIGHT = 1.62;

    private static void forceChunks(ServerLevel level, boolean forced) {
        for (int cx = (ORIGIN.getX() - 2) >> 4; cx <= (ORIGIN.getX() + CLEAR) >> 4; cx++) {
            for (int cz = (ORIGIN.getZ() - 2) >> 4; cz <= (ORIGIN.getZ() + CLEAR) >> 4; cz++) {
                level.setChunkForced(cx, cz, forced);
                if (forced) {
                    level.getChunk(cx, cz);
                }
            }
        }
    }
}
