package com.adamkali.dwm.platform;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Client-only loader SPI. Installed by the client entrypoint after {@link DwmPlatform}.
 */
public interface DwmClientPlatform {

    void registerEndClientTick(Consumer<Minecraft> handler);

    void registerClientStopping(Consumer<Minecraft> handler);

    void registerClientDisconnect(BiConsumer<ClientPacketListener, Minecraft> handler);

    <T extends CustomPacketPayload> void registerClientboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ClientPlayContext> handler
    );

    void sendToServer(CustomPacketPayload payload);

    void registerModelLayer(ModelLayerLocation location, LayerDefinitionProvider definition);

    <T extends Entity> void registerEntityRenderer(
            EntityType<? extends T> type,
            EntityRendererProvider<T> factory
    );

    <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
            BlockEntityType<? extends E> type,
            BlockEntityRendererProvider<? super E, ? super S> factory
    );

    Identifier hudAnchorCrosshair();

    Identifier hudAnchorMiscOverlays();

    void attachHudAfter(Identifier afterVanillaElement, Identifier elementId, HudExtractor extractor);

    void registerLevelRenderStartMain(Consumer<LevelRenderCtx> handler);

    void registerLevelRenderEndMain(Consumer<LevelRenderCtx> handler);

    void registerLevelRenderBeforeGizmos(Consumer<LevelRenderCtx> handler);

    interface ClientPlayContext {
        Minecraft client();
    }

    @FunctionalInterface
    interface LayerDefinitionProvider {
        LayerDefinition createLayerDefinition();
    }

    @FunctionalInterface
    interface HudExtractor {
        void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    }

    interface LevelRenderCtx {
        LevelRenderer levelRenderer();
    }
}
