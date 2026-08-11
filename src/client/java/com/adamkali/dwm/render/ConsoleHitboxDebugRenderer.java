package com.adamkali.dwm.render;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Debug wireframes for First Doctor console hit regions, shown when entity hitboxes are
 * enabled (F3 + B / {@link DebugScreenEntries#ENTITY_HITBOXES}).
 * <ul>
 *   <li>Cyan — {@link FirstDoctorConsoleBlock#COLLISION_SHAPE}</li>
 *   <li>Lime — biome selector look/click AABB</li>
 *   <li>Magenta — planet locator look/click AABB</li>
 *   <li>Orange — materialisation lever look/click AABB</li>
 * </ul>
 */
public final class ConsoleHitboxDebugRenderer {
    private static final int COLLISION_COLOR = 0xFF00FFFF;
    private static final int SELECTOR_COLOR = 0xFF33FF33;
    private static final int PLANET_COLOR = 0xFFFF33FF;
    private static final int LEVER_COLOR = 0xFFFF8C1A;
    private static final int RANGE = 16;

    private ConsoleHitboxDebugRenderer() {
    }

    public static void initialize() {
        LevelRenderEvents.BEFORE_GIZMOS.register(ConsoleHitboxDebugRenderer::beforeGizmos);
    }

    private static void beforeGizmos(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;
        if (world == null || client.player == null) {
            return;
        }
        if (!client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }

        BlockPos playerPos = client.player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        try (Gizmos.TemporaryCollection ignored = context.levelRenderer().collectPerFrameRenderThreadGizmos()) {
            for (int dx = -RANGE; dx <= RANGE; dx++) {
                for (int dy = -RANGE; dy <= RANGE; dy++) {
                    for (int dz = -RANGE; dz <= RANGE; dz++) {
                        mutable.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);
                        BlockState state = world.getBlockState(mutable);
                        if (!state.is(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
                            continue;
                        }

                        Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

                        for (AABB local : FirstDoctorConsoleBlock.COLLISION_SHAPE.toAabbs()) {
                            Gizmos.cuboid(
                                    local.move(mutable.getX(), mutable.getY(), mutable.getZ()),
                                    GizmoStyle.stroke(COLLISION_COLOR)
                            );
                        }

                        Gizmos.cuboid(
                                FirstDoctorConsoleControls.biomeSelectorWorldBox(mutable, facing),
                                GizmoStyle.stroke(SELECTOR_COLOR)
                        );
                        Gizmos.cuboid(
                                FirstDoctorConsoleControls.planetLocatorWorldBox(mutable, facing),
                                GizmoStyle.stroke(PLANET_COLOR)
                        );
                        Gizmos.cuboid(
                                FirstDoctorConsoleControls.materialisationLeverWorldBox(mutable, facing),
                                GizmoStyle.stroke(LEVER_COLOR)
                        );
                    }
                }
            }
        }
    }
}
