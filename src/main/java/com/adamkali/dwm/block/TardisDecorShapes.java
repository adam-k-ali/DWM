package com.adamkali.dwm.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Collision / outline shapes for interior decor props. Overflow beyond a unit cube is intentional
 * (same idea as {@link FirstDoctorConsoleBlock}).
 */
public final class TardisDecorShapes {
    private TardisDecorShapes() {
    }

    /**
     * Decor meshes face along {@code FACING}. Use the player's look direction (not opposite)
     * so the front of the prop faces the player on place.
     */
    public static Direction facingForPlacement(Direction horizontalLook) {
        return horizontalLook;
    }

    /** Small chair: ~11×11 footprint, height ~1.12. */
    public static final VoxelShape SMALL_CHAIR = box(2.5, 0.0, 2.0, 13.5, 18.0, 13.0);

    /** Cushion top of {@link #SMALL_CHAIR} mesh (7/16). */
    public static final double SMALL_CHAIR_SEAT_Y = 7.0 / 16.0;

    /** Large chair: ~12×10.5 footprint, height ~1.56. */
    public static final VoxelShape LARGE_CHAIR = box(2.0, 0.0, 2.5, 14.0, 25.0, 13.0);

    /** Cushion top of {@link #LARGE_CHAIR} mesh (9.1/16). */
    public static final double LARGE_CHAIR_SEAT_Y = 9.1 / 16.0;

    /** Column after +8 X/Z shift into block space: 8×8 centered, height ~1.78. */
    public static final VoxelShape COLUMN = box(4.0, 0.0, 4.0, 12.0, 28.5, 12.0);

    /** Globe: ~9×4 footprint centered, height ~2.1. */
    public static final VoxelShape GLOBE = box(3.5, 0.0, 6.0, 12.5, 33.5, 10.0);

    /** Compact scanner: 1×1 footprint, height 2. */
    public static final VoxelShape COMPACT_SCANNER = box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    /**
     * Full scanner north-facing: 3 wide × ~1 deep × 2 tall (model X spans −24..24 around block center).
     * Rotated by {@link #rotateHorizontal(VoxelShape, Direction)}.
     */
    public static final VoxelShape FULL_SCANNER_NORTH = box(-16.0, 0.0, -0.5, 32.0, 32.0, 16.0);

    public static VoxelShape rotateHorizontal(VoxelShape northShape, Direction facing) {
        return switch (facing) {
            case SOUTH -> rotateY(northShape, 180);
            case WEST -> rotateY(northShape, 270);
            case EAST -> rotateY(northShape, 90);
            default -> northShape;
        };
    }

    private static VoxelShape box(double x0, double y0, double z0, double x1, double y1, double z1) {
        return Shapes.box(x0 / 16.0, y0 / 16.0, z0 / 16.0, x1 / 16.0, y1 / 16.0, z1 / 16.0);
    }

    private static VoxelShape rotateY(VoxelShape shape, int degrees) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double nMinX;
            double nMinZ;
            double nMaxX;
            double nMaxZ;
            switch (degrees) {
                case 90 -> {
                    nMinX = 1.0 - maxZ;
                    nMaxX = 1.0 - minZ;
                    nMinZ = minX;
                    nMaxZ = maxX;
                }
                case 180 -> {
                    nMinX = 1.0 - maxX;
                    nMaxX = 1.0 - minX;
                    nMinZ = 1.0 - maxZ;
                    nMaxZ = 1.0 - minZ;
                }
                case 270 -> {
                    nMinX = minZ;
                    nMaxX = maxZ;
                    nMinZ = 1.0 - maxX;
                    nMaxZ = 1.0 - minX;
                }
                default -> {
                    nMinX = minX;
                    nMaxX = maxX;
                    nMinZ = minZ;
                    nMaxZ = maxZ;
                }
            }
            result[0] = Shapes.or(result[0], Shapes.box(nMinX, minY, nMinZ, nMaxX, maxY, nMaxZ));
        });
        return result[0];
    }
}
