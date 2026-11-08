package com.adamkali.dwm.tardis.portal;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

/**
 * Dense, bounded portal light volume. One byte stores block light in the low nibble and sky light
 * in the high nibble. Coordinates use the same space as {@link #min()}.
 */
public final class PortalLightData {
    public static final PortalLightData EMPTY = new PortalLightData(BlockPos.ZERO, 0, 0, 0, new byte[0]);

    private final BlockPos min;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final byte[] packed;

    public PortalLightData(BlockPos min, int sizeX, int sizeY, int sizeZ, byte[] packed) {
        this.min = Objects.requireNonNull(min, "min").immutable();
        if (sizeX < 0 || sizeY < 0 || sizeZ < 0) {
            throw new IllegalArgumentException("Portal light dimensions must be non-negative");
        }
        long expected = (long) sizeX * sizeY * sizeZ;
        if (expected > Integer.MAX_VALUE || packed == null || packed.length != (int) expected) {
            throw new IllegalArgumentException("Portal light data length does not match dimensions");
        }
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.packed = packed.clone();
    }

    public static PortalLightData sample(LevelLightEngine lightEngine, BlockPos min, BlockPos max) {
        Objects.requireNonNull(lightEngine, "lightEngine");
        return sample(
                lightEngine.getLayerListener(LightLayer.BLOCK),
                lightEngine.getLayerListener(LightLayer.SKY),
                min,
                max
        );
    }

    /**
     * Copies nibble light layers into the packed volume. Null block layers read as zero; null sky
     * layers are resolved through the engine (open sky above the topmost section reads as 15).
     */
    public static PortalLightData sample(
            LayerLightEventListener blockLight,
            LayerLightEventListener skyLight,
            BlockPos min,
            BlockPos max
    ) {
        Objects.requireNonNull(blockLight, "blockLight");
        Objects.requireNonNull(skyLight, "skyLight");
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (max.getX() < min.getX() || max.getY() < min.getY() || max.getZ() < min.getZ()) {
            return EMPTY;
        }
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        byte[] values = new byte[Math.multiplyExact(Math.multiplyExact(sizeX, sizeY), sizeZ)];
        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int maxX = max.getX();
        int maxY = max.getY();
        int maxZ = max.getZ();
        int minSectionX = SectionPos.blockToSectionCoord(minX);
        int maxSectionX = SectionPos.blockToSectionCoord(maxX);
        int minSectionY = SectionPos.blockToSectionCoord(minY);
        int maxSectionY = SectionPos.blockToSectionCoord(maxY);
        int minSectionZ = SectionPos.blockToSectionCoord(minZ);
        int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);
        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            int y0 = Math.max(minY, SectionPos.sectionToBlockCoord(sectionY));
            int y1 = Math.min(maxY, SectionPos.sectionToBlockCoord(sectionY) + 15);
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                int z0 = Math.max(minZ, SectionPos.sectionToBlockCoord(sectionZ));
                int z1 = Math.min(maxZ, SectionPos.sectionToBlockCoord(sectionZ) + 15);
                for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
                    int x0 = Math.max(minX, SectionPos.sectionToBlockCoord(sectionX));
                    int x1 = Math.min(maxX, SectionPos.sectionToBlockCoord(sectionX) + 15);
                    SectionPos sectionPos = SectionPos.of(sectionX, sectionY, sectionZ);
                    DataLayer cachedBlock = blockLight.getDataLayerData(sectionPos);
                    DataLayer cachedSky = skyLight.getDataLayerData(sectionPos);
                    // A missing sky layer is not dark: vanilla treats it as open sky (15) above the
                    // topmost section and inherits from the layer above below it, so ask the engine.
                    boolean skyMissing = cachedSky == null;
                    if (isZeroLayer(cachedBlock) && !skyMissing && cachedSky.isEmpty()) {
                        continue;
                    }
                    for (int y = y0; y <= y1; y++) {
                        int ly = y & 15;
                        int iy = y - minY;
                        for (int z = z0; z <= z1; z++) {
                            int lz = z & 15;
                            int row = (iy * sizeZ + (z - minZ)) * sizeX;
                            for (int x = x0; x <= x1; x++) {
                                int lx = x & 15;
                                int block = cachedBlock == null ? 0 : cachedBlock.get(lx, ly, lz);
                                int sky = skyMissing
                                        ? skyLight.getLightValue(new BlockPos(x, y, z))
                                        : cachedSky.get(lx, ly, lz);
                                values[row + (x - minX)] = pack(block, sky);
                            }
                        }
                    }
                }
            }
        }
        return new PortalLightData(min, sizeX, sizeY, sizeZ, values);
    }

    private static boolean isZeroLayer(DataLayer layer) {
        return layer == null || layer.isEmpty();
    }

    /**
     * Fallback brightness walk used by unit tests that mock {@link LevelReader}.
     */
    public static PortalLightData sample(LevelReader level, BlockPos min, BlockPos max) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (max.getX() < min.getX() || max.getY() < min.getY() || max.getZ() < min.getZ()) {
            return EMPTY;
        }
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        byte[] values = new byte[Math.multiplyExact(Math.multiplyExact(sizeX, sizeY), sizeZ)];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int index = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    cursor.set(min.getX() + x, min.getY() + y, min.getZ() + z);
                    int block = level.getBrightness(LightLayer.BLOCK, cursor);
                    int sky = level.getBrightness(LightLayer.SKY, cursor);
                    values[index++] = pack(block, sky);
                }
            }
        }
        return new PortalLightData(min, sizeX, sizeY, sizeZ, values);
    }

    public static byte pack(int block, int sky) {
        if (block < 0 || block > 15 || sky < 0 || sky > 15) {
            throw new IllegalArgumentException("Portal light levels must be between 0 and 15");
        }
        return (byte) ((sky << 4) | block);
    }

    public int brightness(LightLayer layer, BlockPos pos, int fallback) {
        int packedValue = packed(pos);
        if (packedValue < 0) {
            return fallback;
        }
        return layer == LightLayer.SKY ? packedValue >>> 4 : packedValue & 0xF;
    }

    public int packed(BlockPos pos) {
        int x = pos.getX() - min.getX();
        int y = pos.getY() - min.getY();
        int z = pos.getZ() - min.getZ();
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            return -1;
        }
        return packed[(y * sizeZ + z) * sizeX + x] & 0xFF;
    }

    /**
     * Returns a copy where every cell strictly above the highest streamed block of its column reads
     * as full sky light (block light is kept). The server may sample light before it has propagated
     * (or from a stale visible layer), which stores 0 over open air and renders neighbouring faces
     * and their ambient occlusion black. Columns with no streamed block are left unchanged.
     *
     * @param blocks positions in the same coordinate space as {@link #min()}
     */
    public PortalLightData withOpenSkyAbove(Iterable<BlockPos> blocks) {
        if (isEmpty()) {
            return this;
        }
        int[] top = new int[sizeX * sizeZ];
        Arrays.fill(top, Integer.MIN_VALUE);
        for (BlockPos pos : blocks) {
            int x = pos.getX() - min.getX();
            int z = pos.getZ() - min.getZ();
            if (x < 0 || x >= sizeX || z < 0 || z >= sizeZ) {
                continue;
            }
            int column = z * sizeX + x;
            top[column] = Math.max(top[column], pos.getY());
        }
        byte[] out = packed.clone();
        boolean changed = false;
        for (int z = 0; z < sizeZ; z++) {
            for (int x = 0; x < sizeX; x++) {
                int columnTop = top[z * sizeX + x];
                if (columnTop == Integer.MIN_VALUE) {
                    continue;
                }
                for (int y = Math.max(0, columnTop + 1 - min.getY()); y < sizeY; y++) {
                    int index = (y * sizeZ + z) * sizeX + x;
                    byte updated = (byte) (0xF0 | (out[index] & 0xF));
                    if (updated != out[index]) {
                        out[index] = updated;
                        changed = true;
                    }
                }
            }
        }
        return changed ? new PortalLightData(min, sizeX, sizeY, sizeZ, out) : this;
    }

    public PortalLightData translated(BlockPos offset) {
        return isEmpty() ? EMPTY : new PortalLightData(min.offset(offset), sizeX, sizeY, sizeZ, packed);
    }

    public boolean isEmpty() {
        return packed.length == 0;
    }

    public BlockPos min() {
        return min;
    }

    public int sizeX() {
        return sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public int sizeZ() {
        return sizeZ;
    }

    public byte[] packedCopy() {
        return packed.clone();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PortalLightData that
                && sizeX == that.sizeX
                && sizeY == that.sizeY
                && sizeZ == that.sizeZ
                && min.equals(that.min)
                && Arrays.equals(packed, that.packed);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(min, sizeX, sizeY, sizeZ);
        return 31 * result + Arrays.hashCode(packed);
    }
}
