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
     * Copies nibble light layers into the packed volume. Null section layers read as zero.
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
        DataLayer cachedBlock = null;
        DataLayer cachedSky = null;
        long cachedSection = Long.MIN_VALUE;
        int index = 0;
        for (int y = 0; y < sizeY; y++) {
            int wy = min.getY() + y;
            for (int z = 0; z < sizeZ; z++) {
                int wz = min.getZ() + z;
                for (int x = 0; x < sizeX; x++) {
                    int wx = min.getX() + x;
                    long sectionKey = SectionPos.asLong(
                            SectionPos.blockToSectionCoord(wx),
                            SectionPos.blockToSectionCoord(wy),
                            SectionPos.blockToSectionCoord(wz)
                    );
                    if (sectionKey != cachedSection) {
                        SectionPos sectionPos = SectionPos.of(
                                SectionPos.blockToSectionCoord(wx),
                                SectionPos.blockToSectionCoord(wy),
                                SectionPos.blockToSectionCoord(wz)
                        );
                        cachedBlock = blockLight.getDataLayerData(sectionPos);
                        cachedSky = skyLight.getDataLayerData(sectionPos);
                        cachedSection = sectionKey;
                    }
                    int lx = wx & 15;
                    int ly = wy & 15;
                    int lz = wz & 15;
                    int block = cachedBlock == null ? 0 : cachedBlock.get(lx, ly, lz);
                    int sky = cachedSky == null ? 0 : cachedSky.get(lx, ly, lz);
                    values[index++] = pack(block, sky);
                }
            }
        }
        return new PortalLightData(min, sizeX, sizeY, sizeZ, values);
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
