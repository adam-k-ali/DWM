package com.adamkali.dwm.world.structure;

import net.minecraft.core.Direction;

/**
 * Pure geometry of one rectangular settlement building: a footprint anchored at {@code (minX, minZ)}
 * whose door faces {@code facing}.
 *
 * <p>Buildings are authored in a local frame: {@code u} runs along the front wall
 * ({@code 0 .. width-1}) and {@code v} runs from the front wall inwards ({@code 0 .. depth-1}), so the
 * door sits at {@code v = 0, u = width / 2} (widths are always odd, so that cell is the exact middle).
 * This class maps local cells to world/grid {@code x, z} for any of the four facings, and is shared by the
 * pure {@link ThalSettlementLayout} planner and the block-placing pieces so they cannot disagree.
 */
public record ThalFrame(int minX, int minZ, int width, int depth, Direction facing) {
    public ThalFrame {
        if (facing.getAxis().isVertical()) {
            throw new IllegalArgumentException("facing must be horizontal: " + facing);
        }
        if (width % 2 == 0) {
            throw new IllegalArgumentException("width must be odd so the door is centred: " + width);
        }
    }

    private boolean facesAlongZ() {
        return facing.getAxis() == Direction.Axis.Z;
    }

    /** Footprint extent along the X axis. */
    public int sizeX() {
        return facesAlongZ() ? width : depth;
    }

    /** Footprint extent along the Z axis. */
    public int sizeZ() {
        return facesAlongZ() ? depth : width;
    }

    public int maxX() {
        return minX + sizeX() - 1;
    }

    public int maxZ() {
        return minZ + sizeZ() - 1;
    }

    /** Local {@code u} of the door cell. */
    public int doorU() {
        return width / 2;
    }

    public int worldX(int u, int v) {
        return switch (facing) {
            case NORTH -> minX + u;
            case SOUTH -> minX + (width - 1 - u);
            case WEST -> minX + v;
            case EAST -> minX + (depth - 1 - v);
            default -> throw new IllegalStateException();
        };
    }

    public int worldZ(int u, int v) {
        return switch (facing) {
            case NORTH -> minZ + v;
            case SOUTH -> minZ + (depth - 1 - v);
            case WEST -> minZ + (width - 1 - u);
            case EAST -> minZ + u;
            default -> throw new IllegalStateException();
        };
    }

    /** Direction in which local {@code u} increases. */
    public Direction uDirection() {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
            default -> throw new IllegalStateException();
        };
    }

    /** Direction in which local {@code v} increases (into the building, away from the door). */
    public Direction inward() {
        return facing.getOpposite();
    }

    /** Cell of the footprint holding the door. */
    public ThalSettlementLayout.Cell doorCell() {
        return new ThalSettlementLayout.Cell(worldX(doorU(), 0), worldZ(doorU(), 0));
    }

    /** Cell just outside the door, where the path network attaches. */
    public ThalSettlementLayout.Cell entranceCell() {
        ThalSettlementLayout.Cell door = doorCell();
        return new ThalSettlementLayout.Cell(door.x() + facing.getStepX(), door.z() + facing.getStepZ());
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX() && z >= minZ && z <= maxZ();
    }

    /** Same building translated by {@code (dx, dz)}. */
    public ThalFrame translated(int dx, int dz) {
        return new ThalFrame(minX + dx, minZ + dz, width, depth, facing);
    }
}
