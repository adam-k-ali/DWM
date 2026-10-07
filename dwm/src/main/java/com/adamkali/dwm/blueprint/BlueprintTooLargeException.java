package com.adamkali.dwm.blueprint;

/**
 * Raised when rasterization would produce more than {@link BlueprintRasterizer#MAX_VOXELS} voxels.
 */
public final class BlueprintTooLargeException extends RuntimeException {
    private final int voxelCount;

    public BlueprintTooLargeException(int voxelCount) {
        super("Blueprint exceeds maximum of " + BlueprintRasterizer.MAX_VOXELS + " voxels (got " + voxelCount + ")");
        this.voxelCount = voxelCount;
    }

    public int voxelCount() {
        return voxelCount;
    }
}
