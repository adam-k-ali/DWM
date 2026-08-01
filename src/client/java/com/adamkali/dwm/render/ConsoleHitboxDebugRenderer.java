package com.adamkali.dwm.render;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Debug wireframes for First Doctor console hit regions, shown when entity hitboxes are
 * enabled (F3 + B).
 * <ul>
 *   <li>Cyan — {@link FirstDoctorConsoleBlock#COLLISION_SHAPE}</li>
 *   <li>Lime — biome selector look/click AABB</li>
 * </ul>
 */
public final class ConsoleHitboxDebugRenderer {
    private static final int COLLISION_COLOR = 0xFF00FFFF;
    private static final float SELECTOR_R = 0.2f;
    private static final float SELECTOR_G = 1.0f;
    private static final float SELECTOR_B = 0.2f;
    private static final float SELECTOR_A = 1.0f;
    private static final int RANGE = 16;

    private ConsoleHitboxDebugRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ConsoleHitboxDebugRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
            return;
        }
        if (!client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            return;
        }

        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack matrices = context.matrixStack();
        Vec3d cam = context.camera().getPos();

        BlockPos playerPos = client.player.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    mutable.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);
                    BlockState state = world.getBlockState(mutable);
                    if (!state.isOf(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
                        continue;
                    }

                    Direction facing = state.get(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

                    VertexRendering.drawOutline(
                            matrices,
                            lines,
                            FirstDoctorConsoleBlock.COLLISION_SHAPE,
                            mutable.getX(),
                            mutable.getY(),
                            mutable.getZ(),
                            COLLISION_COLOR
                    );

                    Box selector = FirstDoctorConsoleControls.biomeSelectorWorldBox(mutable, facing);
                    VertexRendering.drawBox(
                            matrices,
                            lines,
                            selector,
                            SELECTOR_R,
                            SELECTOR_G,
                            SELECTOR_B,
                            SELECTOR_A
                    );
                }
            }
        }

        matrices.pop();
    }
}
