package com.adamkali.dwm.platform.fabric;

import com.adamkali.dwm.platform.DwmClientPlatform;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
 * Fabric client implementation of {@link DwmClientPlatform}.
 */
public final class FabricDwmClientPlatform implements DwmClientPlatform {
    @Override
    public void registerEndClientTick(Consumer<Minecraft> handler) {
        ClientTickEvents.END_CLIENT_TICK.register(handler::accept);
    }

    @Override
    public void registerClientStopping(Consumer<Minecraft> handler) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(handler::accept);
    }

    @Override
    public void registerClientDisconnect(BiConsumer<ClientPacketListener, Minecraft> handler) {
        ClientPlayConnectionEvents.DISCONNECT.register(handler::accept);
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ClientPlayContext> handler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                handler.accept(payload, context::client));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void registerModelLayer(ModelLayerLocation location, LayerDefinitionProvider definition) {
        ModelLayerRegistry.registerModelLayer(location, definition::createLayerDefinition);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(
            EntityType<? extends T> type,
            EntityRendererProvider<T> factory
    ) {
        EntityRendererRegistry.register(type, factory);
    }

    @Override
    public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
            BlockEntityType<? extends E> type,
            BlockEntityRendererProvider<? super E, ? super S> factory
    ) {
        BlockEntityRendererRegistry.register(type, factory);
    }

    @Override
    public Identifier hudAnchorCrosshair() {
        return VanillaHudElements.CROSSHAIR;
    }

    @Override
    public Identifier hudAnchorMiscOverlays() {
        return VanillaHudElements.MISC_OVERLAYS;
    }

    @Override
    public void attachHudAfter(Identifier afterVanillaElement, Identifier elementId, HudExtractor extractor) {
        HudElementRegistry.attachElementAfter(afterVanillaElement, elementId, extractor::extract);
    }

    @Override
    public void registerLevelRenderStartMain(Consumer<LevelRenderCtx> handler) {
        LevelRenderEvents.START_MAIN.register(context -> handler.accept(context::levelRenderer));
    }

    @Override
    public void registerLevelRenderEndMain(Consumer<LevelRenderCtx> handler) {
        LevelRenderEvents.END_MAIN.register(context -> handler.accept(context::levelRenderer));
    }

    @Override
    public void registerLevelRenderBeforeGizmos(Consumer<LevelRenderCtx> handler) {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> handler.accept(context::levelRenderer));
    }
}
