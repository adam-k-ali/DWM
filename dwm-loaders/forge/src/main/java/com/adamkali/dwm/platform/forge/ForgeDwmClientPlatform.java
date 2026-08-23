package com.adamkali.dwm.platform.forge;

import com.adamkali.dwm.DWMReference;
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
import net.minecraftforge.client.FramePassManager;
import net.minecraftforge.client.event.AddFramePassEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Forge client implementation of {@link DwmClientPlatform} (Forge 65 / MC 26.2).
 */
public final class ForgeDwmClientPlatform implements DwmClientPlatform {
    private final ForgeDwmPlatform common;
    private final List<ModelLayerEntry> modelLayers = new CopyOnWriteArrayList<>();
    private final List<EntityRendererEntry<?>> entityRenderers = new CopyOnWriteArrayList<>();
    private final List<BlockEntityRendererEntry<?, ?>> blockEntityRenderers = new CopyOnWriteArrayList<>();
    private final List<HudLayerEntry> hudLayers = new CopyOnWriteArrayList<>();
    private final List<Consumer<LevelRenderCtx>> startMainHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<LevelRenderCtx>> endMainHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<LevelRenderCtx>> beforeGizmosHandlers = new CopyOnWriteArrayList<>();

    public ForgeDwmClientPlatform(ForgeDwmPlatform common) {
        this.common = common;
        EntityRenderersEvent.RegisterLayerDefinitions.BUS.addListener(this::onRegisterLayerDefinitions);
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(this::onRegisterRenderers);
        AddGuiOverlayLayersEvent.BUS.addListener(this::onAddGuiOverlayLayers);
        AddFramePassEvent.BUS.addListener(this::onAddFramePasses);
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

    private void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        ForgeLayeredDraw root = event.getLayeredDraw();
        for (HudLayerEntry entry : hudLayers) {
            root.addAbove(
                    ForgeLayeredDraw.PRE_SLEEP_STACK,
                    entry.elementId(),
                    entry.afterVanillaElement(),
                    entry.extractor()::extract
            );
        }
    }

    private void onAddFramePasses(AddFramePassEvent event) {
        // Forge 65 replaced RenderLevelStageEvent with frame-graph passes. Approximate
        // Fabric START_MAIN / END_MAIN / BEFORE_GIZMOS with ordered dwm passes on main.
        event.addPass(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "level_start_main"),
                passDefinition(startMainHandlers)
        );
        event.addPass(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "level_end_main"),
                passDefinition(endMainHandlers)
        );
        event.addPass(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "level_before_gizmos"),
                passDefinition(beforeGizmosHandlers)
        );
    }

    private static FramePassManager.PassDefinition passDefinition(List<Consumer<LevelRenderCtx>> handlers) {
        return new FramePassManager.PassDefinition() {
            @Override
            public void extracts(
                    net.minecraft.client.renderer.LevelTargetBundle bundle,
                    com.mojang.blaze3d.framegraph.FramePass pass,
                    net.minecraft.client.DeltaTracker deltaTracker
            ) {
                bundle.main = pass.readsAndWrites(bundle.main);
            }

            @Override
            public void executes(net.minecraft.client.renderer.state.level.LevelRenderState state) {
                LevelRenderCtx ctx = () -> Minecraft.getInstance().levelRenderer;
                for (Consumer<LevelRenderCtx> handler : handlers) {
                    handler.accept(ctx);
                }
            }
        };
    }

    @Override
    public void registerEndClientTick(Consumer<Minecraft> handler) {
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> handler.accept(Minecraft.getInstance()));
    }

    @Override
    public void registerClientStopping(Consumer<Minecraft> handler) {
        GameShuttingDownEvent.BUS.addListener(event -> handler.accept(Minecraft.getInstance()));
    }

    @Override
    public void registerClientDisconnect(BiConsumer<ClientPacketListener, Minecraft> handler) {
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> {
            Minecraft client = Minecraft.getInstance();
            ClientPacketListener connection = client.getConnection();
            if (connection != null) {
                handler.accept(connection, client);
            }
        });
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ClientPlayContext> handler
    ) {
        BiConsumer<T, CustomPayloadEvent.Context> adapted = (payload, ctx) ->
                handler.accept(payload, () -> Minecraft.getInstance());
        common.putClientboundHandler(type, adapted);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        common.channel().send(payload, PacketDistributor.SERVER.noArg());
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
        return ForgeLayeredDraw.CROSSHAIR;
    }

    @Override
    public Identifier hudAnchorMiscOverlays() {
        // Forge has no MISC_OVERLAYS; camera overlay is the early HUD slot.
        return ForgeLayeredDraw.CAMERA_OVERLAY;
    }

    @Override
    public void attachHudAfter(Identifier afterVanillaElement, Identifier elementId, HudExtractor extractor) {
        hudLayers.add(new HudLayerEntry(afterVanillaElement, elementId, extractor));
    }

    @Override
    public void registerLevelRenderStartMain(Consumer<LevelRenderCtx> handler) {
        startMainHandlers.add(handler);
    }

    @Override
    public void registerLevelRenderEndMain(Consumer<LevelRenderCtx> handler) {
        endMainHandlers.add(handler);
    }

    @Override
    public void registerLevelRenderBeforeGizmos(Consumer<LevelRenderCtx> handler) {
        beforeGizmosHandlers.add(handler);
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
