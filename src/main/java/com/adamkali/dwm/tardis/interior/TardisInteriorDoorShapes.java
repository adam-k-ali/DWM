package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.function.Predicate;

/**
 * Outline/collision shapes for the classic interior double-door mesh.
 *
 * <p>The mesh is ~3×2 blocks (thin in depth) after side jambs. Shapes are expressed
 * in cell-local space relative to the bank primary and are allowed to extend outside
 * the unit cube so the selection box matches the full rendered door.
 *
 * <p>Bounds mirror {@code TardisInteriorDoorBlockEntityRenderer} placement.
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
     * Shift so the ~3-block-wide mesh centers on a 3-wide bank (primary is bank start).
     * With {@link #MODEL_CENTER_X_PX}, yields primary-relative X of 0..3 when facing south.
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

    public static VoxelShape outline(BlockState state, BlockView world, BlockPos pos) {
        Direction facing = state.get(TardisInteriorDoorBlock.FACING);
        return forCell(facing, pos, neighbor -> isMatchingDoor(world.getBlockState(neighbor), facing));
    }

    /**
     * Full door AABB in {@code pos}'s local space (may extend outside 0..1).
     * Pure variant for unit tests: {@code isMatchingDoor} is true for cells in the same bank.
     */
    public static VoxelShape forCell(Direction facing, BlockPos pos, Predicate<BlockPos> isMatchingDoor) {
        BlockPos primary = findPrimary(pos, facing, isMatchingDoor);
        float[] model = modelAabbRelativeToPrimary(facing);
        int dx = pos.getX() - primary.getX();
        int dy = pos.getY() - primary.getY();
        int dz = pos.getZ() - primary.getZ();

        // Full ~3×2 mesh, offset into this cell's local coordinates (extends into neighbors).
        return Block.createCuboidShape(
                (model[0] - dx) * 16.0,
                (model[1] - dy) * 16.0,
                (model[2] - dz) * 16.0,
                (model[3] - dx) * 16.0,
                (model[4] - dy) * 16.0,
                (model[5] - dz) * 16.0
        );
    }

    /**
     * Model AABB in blocks relative to the primary cell origin, after BER transforms.
     * Returns {@code {minX, minY, minZ, maxX, maxY, maxZ}}.
     */
    public static float[] modelAabbRelativeToPrimary(Direction facing) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        float yawRad = -Direction.getHorizontalDegreesOrThrow(facing) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(yawRad);
        float sin = MathHelper.sin(yawRad);

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

    public static BlockPos findPrimary(BlockPos pos, Direction facing, Predicate<BlockPos> isMatchingDoor) {
        BlockPos primary = pos;
        while (isMatchingDoor.test(primary.down())) {
            primary = primary.down();
        }
        Direction towardBankStart = facing.rotateYClockwise();
        while (isMatchingDoor.test(primary.offset(towardBankStart))) {
            primary = primary.offset(towardBankStart);
        }
        return primary;
    }

    private static boolean isMatchingDoor(BlockState state, Direction facing) {
        return state.isOf(DWMBlocks.TARDIS_INTERIOR_DOOR)
                && state.get(TardisInteriorDoorBlock.FACING) == facing;
    }
}
