package com.adamkali.dwm.render.soto.portal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

/**
 * Owns a private {@link FeatureRenderDispatcher} for SOTO portal entity/BE flushes.
 * <p>
 * The game renderer dispatcher still has its main-pass {@code PreparedFrame} open during
 * {@code LevelRenderEvents.END_MAIN}, so calling {@code renderAllFeatures} on it throws
 * {@code PreparedFrame already in use}. Portal features must use a separate dispatcher
 * (and staged vertex buffer) that is not mid-frame.
 */
final class SotoPortalFeatureFlush implements AutoCloseable {
    private static SotoPortalFeatureFlush instance;

    private final RenderBuffers renderBuffers;
    private final FeatureRenderDispatcher dispatcher;
    private boolean closed;

    private SotoPortalFeatureFlush(Minecraft client) {
        this.renderBuffers = new RenderBuffers(1);
        this.dispatcher = new FeatureRenderDispatcher(
                this.renderBuffers,
                client.getModelManager(),
                client.getAtlasManager(),
                client.font,
                client.gameRenderer.gameRenderState()
        );
    }

    static SotoPortalFeatureFlush get(Minecraft client) {
        if (client == null) {
            return null;
        }
        if (instance == null || instance.closed) {
            instance = new SotoPortalFeatureFlush(client);
        }
        return instance;
    }

    static void closeGlobal() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    void renderAllFeatures(SubmitNodeStorage submitNodeStorage) {
        if (closed || submitNodeStorage == null) {
            return;
        }
        dispatcher.renderAllFeatures(submitNodeStorage);
        renderBuffers.endFrame();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        dispatcher.close();
        renderBuffers.close();
    }
}
