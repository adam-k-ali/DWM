package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.render.portal.PortalFrameCache;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CloudStatus;
import net.minecraft.core.BlockPos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * SOTO look-out portal clouds check (called from {@link TardisSotoPortalClientGameTest}): looks up through the open interior door at the overworld sky, once with
 * clouds OFF and once FANCY, and fails unless the FANCY shot contains grey cloud pixels the OFF shot lacks.
 * The viewer stands close to the door: the door's top edge only rises above the horizon from up close, and
 * the cloud layer is only visible well above it.
 */
final class TardisSotoPortalCloudsClientGameTest {
    private TardisSotoPortalCloudsClientGameTest() {
    }

    private static final double VIEWER_LOCAL_X = 5.5;
    private static final double VIEWER_LOCAL_Y = 1.0;
    private static final double VIEWER_LOCAL_Z = 2.6;
    private static final float LOOK_UP_PITCH = -20.0F;
    /** Minimum number of pixels that must turn light (cloud-coloured) when clouds are enabled. */
    private static final int MIN_CLOUD_PIXELS = 400;
    /** ~0.03 blocks/tick drift: this many ticks shifts the cloud field by several blocks. */
    private static final int DRIFT_WAIT_TICKS = 200;
    private static final int MIN_DRIFT_PIXELS = 200;

    /**
     * Runs inside the SOTO test's world (a separate world per scenario exhausts the software-GL client
     * after the Thal scenario): moves the viewer next to the door looking up and compares OFF vs FANCY.
     */
    static void run(ClientGameTestContext context, TestSingleplayerContext world, UUID id) {
        // Options are process-wide and later scenarios share the client: restore them afterwards.
        CloudStatus originalStatus = context.computeOnClient(client -> client.options.cloudStatus().get());
        int originalRange = context.computeOnClient(client -> client.options.cloudRange().get());
        try {
            BlockPos plot = TardisPlotAllocator.plotOrigin(id);
            world.getServer().runCommand(String.format(
                    "execute in dwm:tardis run tp @a %.1f %.1f %.1f 180 %.1f",
                    plot.getX() + VIEWER_LOCAL_X, plot.getY() + VIEWER_LOCAL_Y, plot.getZ() + VIEWER_LOCAL_Z,
                    LOOK_UP_PITCH));
            context.waitTicks(20);

            Path off = shoot(context, CloudStatus.OFF, "portal_soto_clouds_off");
            Path on = shoot(context, CloudStatus.FANCY, "portal_soto_clouds_on");

            // Freshness: the portal frame is cached, so without the periodic refresh the clouds would stay frozen.
            // Do NOT dirty the portal here; only the refresh may redraw it.
            context.waitTicks(DRIFT_WAIT_TICKS);
            Path later = ClientGameTestSupport.screenshot(context, "portal_soto_clouds_later");

            int cloudPixels = countNewLightPixels(off, on);
            if (cloudPixels < MIN_CLOUD_PIXELS) {
                throw new AssertionError("Expected clouds in the SOTO portal: only " + cloudPixels
                        + " pixels turned cloud-grey with clouds enabled (need " + MIN_CLOUD_PIXELS + "); compare "
                        + off + " and " + on);
            }
            int moved = countChangedPixels(on, later);
            if (moved < MIN_DRIFT_PIXELS) {
                throw new AssertionError("Expected the SOTO portal clouds to drift: only " + moved
                        + " pixels changed over " + DRIFT_WAIT_TICKS + " ticks (need " + MIN_DRIFT_PIXELS
                        + "); the portal frame is stale. Compare " + on + " and " + later);
            }
        } finally {
            context.runOnClient(client -> {
                client.options.cloudStatus().set(originalStatus);
                client.options.cloudRange().set(originalRange);
            });
        }
    }

    private static Path shoot(ClientGameTestContext context, CloudStatus status, String name) {
        context.runOnClient(client -> {
            client.options.cloudStatus().set(status);
            client.options.cloudRange().set(128);
            PortalFrameCache.markAllDirty();
        });
        context.waitTicks(10);
        return ClientGameTestSupport.screenshot(context, name);
    }

    /** Pixels that are grey/white and low-saturation (cloud, as tinted by the door) in {@code on} but not in {@code off}. */
    static int countNewLightPixels(Path off, Path on) {
        try {
            BufferedImage a = ImageIO.read(off.toFile());
            BufferedImage b = ImageIO.read(on.toFile());
            int w = Math.min(a.getWidth(), b.getWidth());
            int h = Math.min(a.getHeight(), b.getHeight());
            int count = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (isCloudLike(b.getRGB(x, y)) && !isCloudLike(a.getRGB(x, y))) {
                        count++;
                    }
                }
            }
            return count;
        } catch (IOException e) {
            throw new AssertionError("Could not read screenshots", e);
        }
    }

    /** Pixels whose colour moved noticeably between two screenshots. */
    static int countChangedPixels(Path a, Path b) {
        try {
            BufferedImage first = ImageIO.read(a.toFile());
            BufferedImage second = ImageIO.read(b.toFile());
            int w = Math.min(first.getWidth(), second.getWidth());
            int h = Math.min(first.getHeight(), second.getHeight());
            int count = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int p = first.getRGB(x, y);
                    int q = second.getRGB(x, y);
                    int delta = Math.abs((p >> 16 & 0xFF) - (q >> 16 & 0xFF))
                            + Math.abs((p >> 8 & 0xFF) - (q >> 8 & 0xFF))
                            + Math.abs((p & 0xFF) - (q & 0xFF));
                    if (delta > 40) {
                        count++;
                    }
                }
            }
            return count;
        } catch (IOException e) {
            throw new AssertionError("Could not read screenshots", e);
        }
    }

    private static boolean isCloudLike(int argb) {
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int bl = argb & 0xFF;
        int min = Math.min(r, Math.min(g, bl));
        int max = Math.max(r, Math.max(g, bl));
        return min >= 110 && max - min <= 45;
    }
}
