package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Outline/collision shapes for the classic interior double-door mesh.
 *
 * <p>The mesh is ~3×2 blocks (thin in depth). Each bank cell exposes only the portion of that
 * mesh that intersects its unit cube so vanilla raycast/{@code onUse} work on every cell.
 *
 * <p>Bounds mirror {@code TardisInteriorDoorBlockEntityRenderer} placement (origin = lower/slot 0).
 */
public final class TardisInteriorDoorShapes {
    /**
     * Geometric center of the closed-door mesh in Blockbench X pixels
     * ({@code (MODEL_MIN_X_PX + MODEL_MAX_X_PX) / 2}). Must match BER placement.
     */
    public static final float MODEL_CENTER_X_PX = 8.0F;
    /** Must match {@code TardisInteriorDoorBlockEntityRenderer} height translate. */
    public static final float MODEL_HEIGHT_BLOCKS = 2.0F;
    /**
     * Shift so the ~3-block-wide mesh centers on a 3-wide bank (origin is bank start).
     * With {@link #MODEL_CENTER_X_PX}, yields origin-relative X of 0..3 when facing south.
     */
    public static final float BANK_CENTER_OFFSET_BLOCKS = 1.0F;

    /** Closed-door mesh extents in Blockbench pixel space (doors + side jambs). */
    public static final float MODEL_MIN_X_PX = -16.0F;
    public static final float MODEL_MAX_X_PX = 32.0F;
    public static final float MODEL_MIN_Y_PX = 0.0F;
    public static final float MODEL_MAX_Y_PX = 32.0F;
    public static final float MODEL_MIN_Z_PX = -8.0F;
    public static final float MODEL_MAX_Z_PX = 1.2F;

    private TardisInteriorDoorShapes() {
    }

    public static VoxelShape outline(BlockState state) {
        Direction facing = state.getValue(TardisInteriorDoorBlock.FACING);
        DoubleBlockHalf half = state.getValue(TardisInteriorDoorBlock.HALF);
        int slot = state.getValue(TardisInteriorDoorBlock.SLOT);
        return forCell(facing, half, slot);
    }

    /**
     * Portion of the door mesh inside this bank cell, in cell-local space (clipped to 0..1).
     */
    public static VoxelShape forCell(Direction facing, DoubleBlockHalf half, int slot) {
        float[] model = modelAabbRelativeToPrimary(facing);
        Direction alongBank = facing.getCounterClockWise();
        int dx = alongBank.getStepX() * slot;
        int dy = half == DoubleBlockHalf.UPPER ? 1 : 0;
        int dz = alongBank.getStepZ() * slot;

        VoxelShape fullInCell = Block.box(
                (model[0] - dx) * 16.0,
                (model[1] - dy) * 16.0,
                (model[2] - dz) * 16.0,
                (model[3] - dx) * 16.0,
                (model[4] - dy) * 16.0,
                (model[5] - dz) * 16.0
        );
        return Shapes.join(fullInCell, Shapes.block(), BooleanOp.AND);
    }

    /**
     * Model AABB in blocks relative to the origin cell, after BER transforms.
     * Returns {@code {minX, minY, minZ, maxX, maxY, maxZ}}.
     */
    public static float[] modelAabbRelativeToPrimary(Direction facing) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        float yawRad = -Direction.getYRot(facing) * Mth.DEG_TO_RAD;
        float cos = Mth.cos(yawRad);
        float sin = Mth.sin(yawRad);

        for (float px : new float[]{MODEL_MIN_X_PX, MODEL_MAX_X_PX}) {
            for (float py : new float[]{MODEL_MIN_Y_PX, MODEL_MAX_Y_PX}) {
                for (float pz : new float[]{MODEL_MIN_Z_PX, MODEL_MAX_Z_PX}) {
                    float x = px / 16.0F - MODEL_CENTER_X_PX / 16.0F;
                    float y = py / 16.0F;
                    float z = pz / 16.0F;
                    // X-180 (Blockbench tile-entity flip)
                    y = -y;
                    z = -z;
                    x += BANK_CENTER_OFFSET_BLOCKS;
                    // Facing yaw — same as MatrixStack POSITIVE_Y (right-handed):
                    // x' = x cos + z sin,  z' = -x sin + z cos
                    float rx = x * cos + z * sin;
                    float rz = -x * sin + z * cos;
                    rx += 0.5F;
                    float ry = y + MODEL_HEIGHT_BLOCKS;
                    rz += 0.5F;

                    minX = Math.min(minX, rx);
                    minY = Math.min(minY, ry);
                    minZ = Math.min(minZ, rz);
                    maxX = Math.max(maxX, rx);
                    maxY = Math.max(maxY, ry);
                    maxZ = Math.max(maxZ, rz);
                }
            }
        }

        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
}
