package com.adamkali.dwm.platform.neoforge;

import com.adamkali.dwm.platform.DwmClientPlatform;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * NeoForge client implementation of {@link DwmClientPlatform}.
 */
public final class NeoForgeDwmClientPlatform implements DwmClientPlatform {
    private final Map<CustomPacketPayload.Type<?>, BiConsumer<?, ClientPlayContext>> clientboundHandlers =
            new HashMap<>();
    private final List<ModelLayerEntry> modelLayers = new CopyOnWriteArrayList<>();
    private final List<EntityRendererEntry<?>> entityRenderers = new CopyOnWriteArrayList<>();
    private final List<BlockEntityRendererEntry<?, ?>> blockEntityRenderers = new CopyOnWriteArrayList<>();
    private final List<HudLayerEntry> hudLayers = new CopyOnWriteArrayList<>();

    public NeoForgeDwmClientPlatform(IEventBus modBus) {
        modBus.addListener(this::onRegisterClientPayloadHandlers);
        modBus.addListener(this::onRegisterLayerDefinitions);
        modBus.addListener(this::onRegisterRenderers);
        modBus.addListener(this::onRegisterGuiLayers);
    }

    private void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        for (Map.Entry<CustomPacketPayload.Type<?>, BiConsumer<?, ClientPlayContext>> entry :
                clientboundHandlers.entrySet()) {
            registerHandler(event, entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void registerHandler(
            RegisterClientPayloadHandlersEvent event,
            CustomPacketPayload.Type<?> type,
            BiConsumer<?, ClientPlayContext> handler
    ) {
        BiConsumer<T, ClientPlayContext> typed = (BiConsumer<T, ClientPlayContext>) handler;
        event.register((CustomPacketPayload.Type<T>) type, (payload, context) ->
                typed.accept(payload, () -> Minecraft.getInstance()));
    }

    private void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (ModelLayerEntry entry : modelLayers) {
            event.registerLayerDefinition(entry.location(), entry.definition()::createLayerDefinition);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (EntityRendererEntry<?> entry : entityRenderers) {
            event.registerEntityRenderer((EntityType) entry.type(), entry.factory());
        }
        for (BlockEntityRendererEntry<?, ?> entry : blockEntityRenderers) {
            event.registerBlockEntityRenderer((BlockEntityType) entry.type(), entry.factory());
        }
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        for (HudLayerEntry entry : hudLayers) {
            event.registerAbove(entry.afterVanillaElement(), entry.elementId(), entry.extractor()::extract);
        }
    }

    @Override
    public void registerEndClientTick(Consumer<Minecraft> handler) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> handler.accept(Minecraft.getInstance()));
    }

    @Override
    public void registerClientStopping(Consumer<Minecraft> handler) {
        NeoForge.EVENT_BUS.addListener((ClientStoppingEvent event) -> handler.accept(Minecraft.getInstance()));
    }

    @Override
    public void registerClientDisconnect(BiConsumer<ClientPacketListener, Minecraft> handler) {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                handler.accept(connection, Minecraft.getInstance());
            }
        });
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ClientPlayContext> handler
    ) {
        clientboundHandlers.put(type, handler);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void registerModelLayer(ModelLayerLocation location, LayerDefinitionProvider definition) {
        modelLayers.add(new ModelLayerEntry(location, definition));
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(
            EntityType<? extends T> type,
            EntityRendererProvider<T> factory
    ) {
        entityRenderers.add(new EntityRendererEntry<>(type, factory));
    }

    @Override
    public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
            BlockEntityType<? extends E> type,
            BlockEntityRendererProvider<? super E, ? super S> factory
    ) {
        blockEntityRenderers.add(new BlockEntityRendererEntry<>(type, factory));
    }

    @Override
    public Identifier hudAnchorCrosshair() {
        return VanillaGuiLayers.CROSSHAIR;
    }

    @Override
    public Identifier hudAnchorMiscOverlays() {
        // NeoForge has no MISC_OVERLAYS; camera overlays are the early HUD slot.
        return VanillaGuiLayers.CAMERA_OVERLAYS;
    }

    @Override
    public void attachHudAfter(Identifier afterVanillaElement, Identifier elementId, HudExtractor extractor) {
        hudLayers.add(new HudLayerEntry(afterVanillaElement, elementId, extractor));
    }

    @Override
    public void registerLevelRenderStartMain(Consumer<LevelRenderCtx> handler) {
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterSky event) ->
                handler.accept(event::getLevelRenderer));
    }

    @Override
    public void registerLevelRenderEndMain(Consumer<LevelRenderCtx> handler) {
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterTranslucentBlocks event) ->
                handler.accept(event::getLevelRenderer));
    }

    @Override
    public void registerLevelRenderBeforeGizmos(Consumer<LevelRenderCtx> handler) {
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterLevel event) ->
                handler.accept(event::getLevelRenderer));
    }

    private record ModelLayerEntry(ModelLayerLocation location, LayerDefinitionProvider definition) {
    }

    private record EntityRendererEntry<T extends Entity>(
            EntityType<? extends T> type,
            EntityRendererProvider<T> factory
    ) {
    }

    private record BlockEntityRendererEntry<E extends BlockEntity, S extends BlockEntityRenderState>(
            BlockEntityType<? extends E> type,
            BlockEntityRendererProvider<? super E, ? super S> factory
    ) {
    }

    private record HudLayerEntry(Identifier afterVanillaElement, Identifier elementId, HudExtractor extractor) {
    }
}
