package com.adamkali.dwm.render.soto;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.logging.LogUtils;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Portal-owned copy of vanilla's {@code CloudRenderer}.
 * <p>
 * Vanilla's renderer hard-binds the main (or clouds) target inside {@link #render} and keeps its
 * texture data private, so it cannot draw into the portal FBO. This class keeps the same face
 * encoding, mesh build and shader inputs ({@code CloudInfo} / {@code CloudFaces}) but takes the
 * destination colour/depth views as arguments. It owns its own mesh and ring buffers so the main
 * view's cloud mesh cache is never thrashed.
 */
public final class SotoCloudRenderer implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/environment/clouds.png");
    private static final int FLAG_INSIDE_FACE = 16;
    private static final int FLAG_USE_TOP_COLOR = 32;
    private static final float CELL_SIZE_IN_BLOCKS = 12.0F;
    private static final float CLOUD_THICKNESS = 4.0F;
    private static final int TICKS_PER_CELL = 400;
    private static final float DRIFT_BLOCKS_PER_TICK = 0.030000001F;
    private static final float Z_OFFSET = 3.96F;
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec3().putVec3().get();

    private TextureData texture;
    private boolean textureLoadAttempted;
    private boolean needsRebuild = true;
    private int prevCellX = Integer.MIN_VALUE;
    private int prevCellZ = Integer.MIN_VALUE;
    private RelativeCameraPos prevRelativeCameraPos = RelativeCameraPos.INSIDE_CLOUDS;
    private CloudStatus prevCloudStatus;
    private int prevRadiusCells = -1;
    private int quadCount;
    private final MappableRingBuffer ubo = new MappableRingBuffer(() -> "Soto Cloud UBO", 130, UBO_SIZE);
    private MappableRingBuffer utb;

    /**
     * Draws clouds into {@code colorTexture}/{@code depthTexture}. The model-view stack must carry
     * the camera rotation only: cloud positions reach the shader camera-relative via {@code CloudInfo}.
     *
     * @param cameraPosition world-space camera position used for cloud placement and drift origin
     */
    public void render(
            GpuTextureView colorTexture,
            GpuTextureView depthTexture,
            int color,
            CloudStatus cloudStatus,
            float bottomY,
            int rangeChunks,
            Vec3 cameraPosition,
            long gameTime,
            float partialTicks) {
        if (cloudStatus == CloudStatus.OFF || colorTexture == null || depthTexture == null) {
            return;
        }
        loadTextureOnce();
        if (texture == null) {
            return;
        }

        int radiusCells = Mth.ceil(rangeChunks * 16 / CELL_SIZE_IN_BLOCKS);
        int utbSize = faceBufferSize(radiusCells);
        if (utb == null || utb.currentBuffer().size() != utbSize) {
            if (utb != null) {
                utb.close();
            }
            utb = new MappableRingBuffer(() -> "Soto Cloud UTB", 258, utbSize);
            needsRebuild = true;
        }

        float relativeBottomY = (float) (bottomY - cameraPosition.y);
        float relativeTopY = relativeBottomY + CLOUD_THICKNESS;
        RelativeCameraPos relativePos = relativeTopY < 0.0F
                ? RelativeCameraPos.ABOVE_CLOUDS
                : relativeBottomY > 0.0F ? RelativeCameraPos.BELOW_CLOUDS : RelativeCameraPos.INSIDE_CLOUDS;

        float cloudOffset = (float) (gameTime % ((long) texture.width() * TICKS_PER_CELL)) + partialTicks;
        double cloudX = cameraPosition.x + cloudOffset * DRIFT_BLOCKS_PER_TICK;
        double cloudZ = cameraPosition.z + Z_OFFSET;
        double textureWidthBlocks = texture.width() * (double) CELL_SIZE_IN_BLOCKS;
        double textureHeightBlocks = texture.height() * (double) CELL_SIZE_IN_BLOCKS;
        cloudX -= Mth.floor(cloudX / textureWidthBlocks) * textureWidthBlocks;
        cloudZ -= Mth.floor(cloudZ / textureHeightBlocks) * textureHeightBlocks;
        int cellX = Mth.floor(cloudX / CELL_SIZE_IN_BLOCKS);
        int cellZ = Mth.floor(cloudZ / CELL_SIZE_IN_BLOCKS);
        float xInCell = (float) (cloudX - cellX * CELL_SIZE_IN_BLOCKS);
        float zInCell = (float) (cloudZ - cellZ * CELL_SIZE_IN_BLOCKS);
        boolean fancy = cloudStatus == CloudStatus.FANCY;
        RenderPipeline pipeline = fancy ? RenderPipelines.CLOUDS : RenderPipelines.FLAT_CLOUDS;

        if (needsRebuild
                || cellX != prevCellX
                || cellZ != prevCellZ
                || relativePos != prevRelativeCameraPos
                || cloudStatus != prevCloudStatus
                || radiusCells != prevRadiusCells) {
            needsRebuild = false;
            prevCellX = cellX;
            prevCellZ = cellZ;
            prevRelativeCameraPos = relativePos;
            prevCloudStatus = cloudStatus;
            prevRadiusCells = radiusCells;
            utb.rotate();
            try (GpuBufferSlice.MappedView view = utb.currentBuffer().map(false, true)) {
                buildMesh(relativePos, view.data(), cellX, cellZ, fancy, radiusCells);
                quadCount = view.data().position() / 3;
            }
        }

        if (quadCount == 0) {
            return;
        }
        try (GpuBufferSlice.MappedView view = ubo.currentBuffer().map(false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putVec4(ARGB.vector4fFromARGB32(color))
                    .putVec3(-xInCell, relativeBottomY, -zInCell)
                    .putVec3(CELL_SIZE_IN_BLOCKS, CLOUD_THICKNESS, CELL_SIZE_IN_BLOCKS);
        }

        GpuBufferSlice dynamicTransforms =
                RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6 * quadCount);
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Soto Clouds", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.setUniform("CloudInfo", ubo.currentBuffer());
            pass.setUniform("CloudFaces", utb.currentBuffer());
            pass.drawIndexed(6 * quadCount, 1, 0, 0, 0);
        }
        ubo.rotate();
    }

    @Override
    public void close() {
        ubo.close();
        if (utb != null) {
            utb.close();
            utb = null;
        }
    }

    private void loadTextureOnce() {
        if (textureLoadAttempted) {
            return;
        }
        textureLoadAttempted = true;
        ResourceManager manager = net.minecraft.client.Minecraft.getInstance().getResourceManager();
        texture = readTexture(manager);
    }

    private static TextureData readTexture(ResourceManager manager) {
        try (InputStream input = manager.open(TEXTURE_LOCATION); NativeImage image = NativeImage.read(input)) {
            int width = image.getWidth();
            int height = image.getHeight();
            long[] cells = new long[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = image.getPixel(x, y);
                    if (isCellEmpty(color)) {
                        continue;
                    }
                    boolean north = isCellEmpty(image.getPixel(x, Math.floorMod(y - 1, height)));
                    boolean east = isCellEmpty(image.getPixel(Math.floorMod(x + 1, height), y));
                    boolean south = isCellEmpty(image.getPixel(x, Math.floorMod(y + 1, height)));
                    boolean west = isCellEmpty(image.getPixel(Math.floorMod(x - 1, height), y));
                    cells[x + y * width] = packCellData(color, north, east, south, west);
                }
            }
            return new TextureData(cells, width, height);
        } catch (IOException e) {
            LOGGER.error("Failed to load cloud texture for SOTO portal", e);
            return null;
        }
    }

    private static int faceBufferSize(int radiusCells) {
        int maxCells = (radiusCells + 1) * 2 * (radiusCells + 1) * 2 / 2;
        int maxFaces = maxCells * 4 + 54;
        return maxFaces * 3;
    }

    private static boolean isCellEmpty(int color) {
        return ARGB.alpha(color) < 10;
    }

    private static long packCellData(int color, boolean north, boolean east, boolean south, boolean west) {
        return (long) color << 4 | (north ? 1 : 0) << 3 | (east ? 1 : 0) << 2 | (south ? 1 : 0) << 1 | (west ? 1 : 0);
    }

    private static boolean isNorthEmpty(long cellData) {
        return (cellData >> 3 & 1L) != 0L;
    }

    private static boolean isEastEmpty(long cellData) {
        return (cellData >> 2 & 1L) != 0L;
    }

    private static boolean isSouthEmpty(long cellData) {
        return (cellData >> 1 & 1L) != 0L;
    }

    private static boolean isWestEmpty(long cellData) {
        return (cellData & 1L) != 0L;
    }

    private void buildMesh(
            RelativeCameraPos relativePos,
            ByteBuffer faceBuffer,
            int centerCellX,
            int centerCellZ,
            boolean extrude,
            int radiusCells) {
        long[] cells = texture.cells();
        int textureWidth = texture.width();
        int textureHeight = texture.height();
        for (int ring = 0; ring <= 2 * radiusCells; ring++) {
            for (int relativeCellX = -ring; relativeCellX <= ring; relativeCellX++) {
                int relativeCellZ = ring - Math.abs(relativeCellX);
                if (relativeCellZ < 0
                        || relativeCellZ > radiusCells
                        || relativeCellX * relativeCellX + relativeCellZ * relativeCellZ > radiusCells * radiusCells) {
                    continue;
                }
                if (relativeCellZ != 0) {
                    tryBuildCell(relativePos, faceBuffer, centerCellX, centerCellZ, extrude,
                            relativeCellX, textureWidth, -relativeCellZ, textureHeight, cells);
                }
                tryBuildCell(relativePos, faceBuffer, centerCellX, centerCellZ, extrude,
                        relativeCellX, textureWidth, relativeCellZ, textureHeight, cells);
            }
        }
    }

    private static void tryBuildCell(
            RelativeCameraPos relativePos,
            ByteBuffer faceBuffer,
            int cellX,
            int cellZ,
            boolean extrude,
            int relativeCellX,
            int textureWidth,
            int relativeCellZ,
            int textureHeight,
            long[] cells) {
        int indexX = Math.floorMod(cellX + relativeCellX, textureWidth);
        int indexY = Math.floorMod(cellZ + relativeCellZ, textureHeight);
        long cellData = cells[indexX + indexY * textureWidth];
        if (cellData == 0L) {
            return;
        }
        if (extrude) {
            buildExtrudedCell(relativePos, faceBuffer, relativeCellX, relativeCellZ, cellData);
        } else {
            encodeFace(faceBuffer, relativeCellX, relativeCellZ, Direction.DOWN, FLAG_USE_TOP_COLOR);
        }
    }

    private static void buildExtrudedCell(
            RelativeCameraPos relativePos, ByteBuffer faceBuffer, int x, int z, long cellData) {
        if (relativePos != RelativeCameraPos.BELOW_CLOUDS) {
            encodeFace(faceBuffer, x, z, Direction.UP, 0);
        }
        if (relativePos != RelativeCameraPos.ABOVE_CLOUDS) {
            encodeFace(faceBuffer, x, z, Direction.DOWN, 0);
        }
        if (isNorthEmpty(cellData) && z > 0) {
            encodeFace(faceBuffer, x, z, Direction.NORTH, 0);
        }
        if (isSouthEmpty(cellData) && z < 0) {
            encodeFace(faceBuffer, x, z, Direction.SOUTH, 0);
        }
        if (isWestEmpty(cellData) && x > 0) {
            encodeFace(faceBuffer, x, z, Direction.WEST, 0);
        }
        if (isEastEmpty(cellData) && x < 0) {
            encodeFace(faceBuffer, x, z, Direction.EAST, 0);
        }
        if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
            for (Direction direction : Direction.values()) {
                encodeFace(faceBuffer, x, z, direction, FLAG_INSIDE_FACE);
            }
        }
    }

    private static void encodeFace(ByteBuffer faceBuffer, int x, int z, Direction direction, int flags) {
        int dirAndFlags = direction.get3DDataValue() | flags;
        dirAndFlags |= (x & 1) << 7;
        dirAndFlags |= (z & 1) << 6;
        faceBuffer.put((byte) (x >> 1)).put((byte) (z >> 1)).put((byte) dirAndFlags);
    }

    private enum RelativeCameraPos {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS
    }

    private record TextureData(long[] cells, int width, int height) {
    }
}
