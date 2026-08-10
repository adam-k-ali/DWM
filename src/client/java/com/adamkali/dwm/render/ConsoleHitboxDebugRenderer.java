package com.adamkali.dwm.render;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Debug wireframes for First Doctor console hit regions, shown when entity hitboxes are
 * enabled (F3 + B).
 * <ul>
 *   <li>Cyan — {@link FirstDoctorConsoleBlock#COLLISION_SHAPE}</li>
 *   <li>Lime — biome selector look/click AABB</li>
 *   <li>Magenta — planet locator look/click AABB</li>
 *   <li>Orange — materialisation lever look/click AABB</li>
 * </ul>
 */
public final class ConsoleHitboxDebugRenderer {
    private static final int COLLISION_COLOR = 0xFF00FFFF;
    private static final float SELECTOR_R = 0.2f;
    private static final float SELECTOR_G = 1.0f;
    private static final float SELECTOR_B = 0.2f;
    private static final float SELECTOR_A = 1.0f;
    private static final float PLANET_R = 1.0f;
    private static final float PLANET_G = 0.2f;
    private static final float PLANET_B = 1.0f;
    private static final float PLANET_A = 1.0f;
    private static final float LEVER_R = 1.0f;
    private static final float LEVER_G = 0.55f;
    private static final float LEVER_B = 0.1f;
    private static final float LEVER_A = 1.0f;
    private static final int RANGE = 16;

    private ConsoleHitboxDebugRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ConsoleHitboxDebugRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;
        if (world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
            return;
        }
        if (!client.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            return;
        }

        MultiBufferSource consumers = context.consumers();
        VertexConsumer lines = consumers.getBuffer(RenderType.lines());
        PoseStack matrices = context.matrixStack();
        Vec3 cam = context.camera().getPosition();

        BlockPos playerPos = client.player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    mutable.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);
                    BlockState state = world.getBlockState(mutable);
                    if (!state.is(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
                        continue;
                    }

                    Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

                    ShapeRenderer.renderShape(
                            matrices,
                            lines,
                            FirstDoctorConsoleBlock.COLLISION_SHAPE,
                            mutable.getX(),
                            mutable.getY(),
                            mutable.getZ(),
                            COLLISION_COLOR
                    );

                    AABB selector = FirstDoctorConsoleControls.biomeSelectorWorldBox(mutable, facing);
                    ShapeRenderer.renderLineBox(
                            matrices,
                            lines,
                            selector,
                            SELECTOR_R,
                            SELECTOR_G,
                            SELECTOR_B,
                            SELECTOR_A
                    );

                    AABB planet = FirstDoctorConsoleControls.planetLocatorWorldBox(mutable, facing);
                    ShapeRenderer.renderLineBox(
                            matrices,
                            lines,
                            planet,
                            PLANET_R,
                            PLANET_G,
                            PLANET_B,
                            PLANET_A
                    );

                    AABB lever = FirstDoctorConsoleControls.materialisationLeverWorldBox(mutable, facing);
                    ShapeRenderer.renderLineBox(
                            matrices,
                            lines,
                            lever,
                            LEVER_R,
                            LEVER_G,
                            LEVER_B,
                            LEVER_A
                    );
                }
            }
        }

        matrices.popPose();
    }
}
