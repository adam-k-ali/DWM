package com.adamkali.dwm.platform.forge;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.platform.DwmPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.payload.PayloadFlow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Forge common/server implementation of {@link DwmPlatform} (Forge 65 / MC 26.2).
 */
public final class ForgeDwmPlatform implements DwmPlatform {
    private final List<PendingPayload<?>> clientboundPayloads = new CopyOnWriteArrayList<>();
    private final List<PendingPayload<?>> serverboundPayloads = new CopyOnWriteArrayList<>();
    private final Map<CustomPacketPayload.Type<?>, BiConsumer<?, ServerPlayContext>> serverboundHandlers =
            new ConcurrentHashMap<>();
    private final Map<CustomPacketPayload.Type<?>, BiConsumer<?, CustomPayloadEvent.Context>> clientboundHandlers =
            new ConcurrentHashMap<>();
    private final List<SpawnPlacementEntry<?>> spawnPlacements = new CopyOnWriteArrayList<>();
    private final List<AttributeEntry> attributes = new CopyOnWriteArrayList<>();
    private final Map<ResourceKey<CreativeModeTab>, List<Consumer<CreativeTabOutput>>> creativeTabModifiers =
            new ConcurrentHashMap<>();
    private final Map<Block, Block> strippables = new ConcurrentHashMap<>();

    private Channel<CustomPacketPayload> channel;

    public ForgeDwmPlatform() {
        EntityAttributeCreationEvent.BUS.addListener(this::onEntityAttributeCreation);
        SpawnPlacementRegisterEvent.BUS.addListener(this::onSpawnPlacementRegister);
        BuildCreativeModeTabContentsEvent.BUS.addListener(this::onBuildCreativeTabs);
        BlockEvent.BlockToolModificationEvent.BUS.addListener(this::onToolModification);
    }

    /**
     * Builds the Forge {@link Channel} after all payload codecs are registered
     * (call after {@link com.adamkali.dwm.DwmCommon#init()}).
     */
    public void buildNetwork() {
        if (channel != null) {
            return;
        }

        var connection = ChannelBuilder
                .named(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "main"))
                .networkProtocolVersion(1)
                .payloadChannel();

        PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> clientbound =
                connection.play().clientbound();
        for (PendingPayload<?> pending : clientboundPayloads) {
            pending.addClientbound(clientbound, clientboundHandlers);
        }

        PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> serverbound =
                clientbound.serverbound();
        for (PendingPayload<?> pending : serverboundPayloads) {
            pending.addServerbound(serverbound, serverboundHandlers);
        }

        channel = serverbound.build();
    }

    Channel<CustomPacketPayload> channel() {
        if (channel == null) {
            buildNetwork();
        }
        return channel;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <T extends CustomPacketPayload> void putClientboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, CustomPayloadEvent.Context> handler
    ) {
        clientboundHandlers.put(type, (BiConsumer) handler);
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        for (AttributeEntry entry : attributes) {
            event.put(entry.type(), entry.attributes().get().build());
        }
    }

    private void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        for (SpawnPlacementEntry<?> entry : spawnPlacements) {
            entry.register(event);
        }
    }

    private void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        List<Consumer<CreativeTabOutput>> modifiers = creativeTabModifiers.get(event.getTabKey());
        if (modifiers == null) {
            return;
        }
        CreativeTabOutput output = event::accept;
        for (Consumer<CreativeTabOutput> modifier : modifiers) {
            modifier.accept(output);
        }
    }

    private boolean onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != ToolActions.AXE_STRIP) {
            return false;
        }
        Block stripped = strippables.get(event.getState().getBlock());
        if (stripped == null) {
            return false;
        }
        event.setFinalState(stripped.withPropertiesOf(event.getState()));
        return false;
    }

    @Override
    public void registerServerStarted(Consumer<MinecraftServer> handler) {
        ServerStartedEvent.BUS.addListener(event -> handler.accept(event.getServer()));
    }

    @Override
    public void registerAfterSave(AfterSaveHandler handler) {
        LevelEvent.Save.BUS.addListener(event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                handler.onAfterSave(serverLevel.getServer(), true, false);
            }
        });
    }

    @Override
    public void registerServerStopped(Consumer<MinecraftServer> handler) {
        ServerStoppedEvent.BUS.addListener(event -> handler.accept(event.getServer()));
    }

    @Override
    public void registerEndServerTick(Consumer<MinecraftServer> handler) {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> handler.accept(event.server()));
    }

    @Override
    public void registerCommands(CommandRegistrationHandler handler) {
        RegisterCommandsEvent.BUS.addListener(event ->
                handler.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        clientboundPayloads.add(new PendingPayload<>(type, codec));
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        serverboundPayloads.add(new PendingPayload<>(type, codec));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> void registerServerboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ServerPlayContext> handler
    ) {
        serverboundHandlers.put(type, (BiConsumer<?, ServerPlayContext>) handler);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        channel().send(payload, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public Collection<ServerPlayer> playersTracking(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(pos), false);
    }

    @Override
    public Collection<ServerPlayer> playersAround(ServerLevel level, Vec3 center, double radius) {
        double diameter = radius * 2.0;
        AABB box = AABB.ofSize(center, diameter, diameter, diameter);
        double radiusSq = radius * radius;
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (player.distanceToSqr(center) <= radiusSq) {
                result.add(player);
            }
        }
        return result;
    }

    @Override
    public void registerBeforeBlockBreak(BeforeBlockBreakHandler handler) {
        BlockEvent.BreakEvent.BUS.addListener(event -> {
            if (!(event.getLevel() instanceof Level level)) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(event.getPos());
            return !handler.allowBreak(level, event.getPlayer(), event.getPos(), event.getState(), blockEntity);
        });
    }

    @Override
    public void registerAfterBlockBreak(AfterBlockBreakHandler handler) {
        // Forge 65 has no dedicated after-break bus; invoke when BreakEvent is allowed to proceed.
        BlockEvent.BreakEvent.BUS.addListener(event -> {
            if (!(event.getLevel() instanceof Level level)) {
                return;
            }
            BlockEntity blockEntity = level.getBlockEntity(event.getPos());
            handler.onBreak(level, event.getPlayer(), event.getPos(), event.getState(), blockEntity);
        });
    }

    @Override
    public void registerAttackBlock(AttackBlockHandler handler) {
        PlayerInteractEvent.LeftClickBlock.BUS.addListener(event -> {
            InteractionResult result = handler.onAttack(
                    event.getEntity(),
                    event.getLevel(),
                    event.getHand(),
                    event.getPos(),
                    event.getFace()
            );
            return result != InteractionResult.PASS;
        });
    }

    @Override
    public void registerUseBlock(UseBlockHandler handler) {
        PlayerInteractEvent.RightClickBlock.BUS.addListener(event -> {
            InteractionResult result = handler.onUse(
                    event.getEntity(),
                    event.getLevel(),
                    event.getHand(),
                    event.getHitVec()
            );
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                return true;
            }
            return false;
        });
    }

    @Override
    public void modifyCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeTabOutput> modifier) {
        creativeTabModifiers.computeIfAbsent(tab, key -> new CopyOnWriteArrayList<>()).add(modifier);
    }

    @Override
    public void registerCompostable(ItemLike item, float chance) {
        ComposterBlock.COMPOSTABLES.put(item.asItem(), chance);
    }

    @Override
    public void registerStrippable(Block input, Block stripped) {
        strippables.put(input, stripped);
    }

    @Override
    public void registerFlammable(Block block, int burnOdds, int spreadOdds) {
        ((FireBlock) Blocks.FIRE).setFlammable(block, burnOdds, spreadOdds);
    }

    @Override
    public BlockSetType registerBlockSetType(Identifier id) {
        return BlockSetType.register(new BlockSetType(id.toString()));
    }

    @Override
    public WoodType registerWoodType(Identifier id, BlockSetType setType) {
        return WoodType.register(new WoodType(id.toString(), setType));
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> buildBlockEntityType(
            BlockEntityFactory<T> factory,
            Block... blocks
    ) {
        return new BlockEntityType<>(factory::create, Set.of(blocks));
    }

    @Override
    public <T extends Mob> void registerSpawnPlacement(
            EntityType<T> type,
            SpawnPlacementType placement,
            Heightmap.Types heightmap,
            SpawnPredicate<T> predicate
    ) {
        spawnPlacements.add(new SpawnPlacementEntry<>(type, placement, heightmap, predicate));
    }

    @Override
    public void registerDefaultAttributes(
            EntityType<? extends LivingEntity> type,
            Supplier<AttributeSupplier.Builder> attributes
    ) {
        this.attributes.add(new AttributeEntry(type, attributes));
    }

    @Override
    public void addValidBlockEntityBlock(BlockEntityType<?> type, Block block) {
        Set<Block> blocks = type.validBlocks;
        if (!(blocks instanceof java.util.HashSet)) {
            blocks = new java.util.HashSet<>(blocks);
            type.validBlocks = blocks;
        }
        blocks.add(block);
    }

    private record PendingPayload<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        @SuppressWarnings("unchecked")
        void addClientbound(
                PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow,
                Map<CustomPacketPayload.Type<?>, BiConsumer<?, CustomPayloadEvent.Context>> handlers
        ) {
            StreamCodec<RegistryFriendlyByteBuf, T> typedCodec =
                    (StreamCodec<RegistryFriendlyByteBuf, T>) codec;
            flow.addMain(type, typedCodec, (payload, ctx) -> {
                BiConsumer<T, CustomPayloadEvent.Context> handler =
                        (BiConsumer<T, CustomPayloadEvent.Context>) handlers.get(type);
                if (handler != null) {
                    handler.accept(payload, ctx);
                }
            });
        }

        @SuppressWarnings("unchecked")
        void addServerbound(
                PayloadFlow<RegistryFriendlyByteBuf, CustomPacketPayload> flow,
                Map<CustomPacketPayload.Type<?>, BiConsumer<?, ServerPlayContext>> handlers
        ) {
            StreamCodec<RegistryFriendlyByteBuf, T> typedCodec =
                    (StreamCodec<RegistryFriendlyByteBuf, T>) codec;
            BiConsumer<T, ServerPlayContext> handler =
                    (BiConsumer<T, ServerPlayContext>) handlers.get(type);
            flow.addMain(type, typedCodec, (payload, ctx) -> {
                if (handler == null) {
                    return;
                }
                ServerPlayer sender = ctx.getSender();
                if (sender == null) {
                    return;
                }
                handler.accept(payload, new ServerPlayContext() {
                    @Override
                    public MinecraftServer server() {
                        return sender.level().getServer();
                    }

                    @Override
                    public ServerPlayer player() {
                        return sender;
                    }
                });
            });
        }
    }

    private record SpawnPlacementEntry<T extends Mob>(
            EntityType<T> type,
            SpawnPlacementType placement,
            Heightmap.Types heightmap,
            SpawnPredicate<T> predicate
    ) {
        void register(SpawnPlacementRegisterEvent event) {
            event.register(
                    type,
                    placement,
                    heightmap,
                    (entityType, level, reason, pos, random) ->
                            predicate.test(entityType, level, reason, pos, random),
                    SpawnPlacementRegisterEvent.Operation.REPLACE
            );
        }
    }

    private record AttributeEntry(
            EntityType<? extends LivingEntity> type,
            Supplier<AttributeSupplier.Builder> attributes
    ) {
    }
}
