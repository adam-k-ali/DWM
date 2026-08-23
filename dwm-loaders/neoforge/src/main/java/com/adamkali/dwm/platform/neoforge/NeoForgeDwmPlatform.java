package com.adamkali.dwm.platform.neoforge;

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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NeoForge common/server implementation of {@link DwmPlatform}.
 */
public final class NeoForgeDwmPlatform implements DwmPlatform {
    private final List<PendingClientboundPayload<?>> clientboundPayloads = new CopyOnWriteArrayList<>();
    private final List<PendingServerboundPayload<?>> serverboundPayloads = new CopyOnWriteArrayList<>();
    private final Map<CustomPacketPayload.Type<?>, BiConsumer<?, ServerPlayContext>> serverboundHandlers =
            new HashMap<>();
    private final List<SpawnPlacementEntry<?>> spawnPlacements = new CopyOnWriteArrayList<>();
    private final List<AttributeEntry> attributes = new CopyOnWriteArrayList<>();
    private final Map<ResourceKey<CreativeModeTab>, List<Consumer<CreativeTabOutput>>> creativeTabModifiers =
            new HashMap<>();
    private final Map<Block, Block> strippables = new ConcurrentHashMap<>();
    private final Map<BlockEntityType<?>, List<Block>> blockEntityValidBlocks = new HashMap<>();

    public NeoForgeDwmPlatform(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloads);
        modBus.addListener(this::onRegisterSpawnPlacements);
        modBus.addListener(this::onEntityAttributeCreation);
        modBus.addListener(this::onBuildCreativeTabs);
        modBus.addListener(this::onBlockEntityTypeAddBlocks);
        NeoForge.EVENT_BUS.addListener(this::onBlockToolModification);
    }

    private void onBlockEntityTypeAddBlocks(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event) {
        for (Map.Entry<BlockEntityType<?>, List<Block>> entry : blockEntityValidBlocks.entrySet()) {
            event.modify(entry.getKey(), entry.getValue().toArray(Block[]::new));
        }
    }

    private void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) {
            return;
        }
        Block stripped = strippables.get(event.getState().getBlock());
        if (stripped != null) {
            event.setFinalState(stripped.withPropertiesOf(event.getState()));
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (PendingClientboundPayload<?> pending : clientboundPayloads) {
            pending.register(registrar);
        }
        for (PendingServerboundPayload<?> pending : serverboundPayloads) {
            pending.register(registrar, serverboundHandlers);
        }
    }

    private void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (SpawnPlacementEntry<?> entry : spawnPlacements) {
            entry.register(event);
        }
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        for (AttributeEntry entry : attributes) {
            event.put(entry.type(), entry.attributes().get().build());
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

    @Override
    public void registerServerStarted(Consumer<MinecraftServer> handler) {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> handler.accept(event.getServer()));
    }

    @Override
    public void registerAfterSave(AfterSaveHandler handler) {
        NeoForge.EVENT_BUS.addListener((LevelEvent.Save event) -> {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                handler.onAfterSave(serverLevel.getServer(), true, false);
            }
        });
    }

    @Override
    public void registerServerStopped(Consumer<MinecraftServer> handler) {
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> handler.accept(event.getServer()));
    }

    @Override
    public void registerEndServerTick(Consumer<MinecraftServer> handler) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> handler.accept(event.getServer()));
    }

    @Override
    public void registerCommands(CommandRegistrationHandler handler) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                handler.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        clientboundPayloads.add(new PendingClientboundPayload<>(type, codec));
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        serverboundPayloads.add(new PendingServerboundPayload<>(type, codec));
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
        PacketDistributor.sendToPlayer(player, payload);
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
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, (BreakBlockEvent event) -> {
            if (!(event.getLevel() instanceof net.minecraft.world.level.Level level)) {
                return;
            }
            BlockEntity blockEntity = level.getBlockEntity(event.getPos());
            if (!handler.allowBreak(level, event.getPlayer(), event.getPos(), event.getState(), blockEntity)) {
                event.setCanceled(true);
                event.setNotifyClient(true);
            }
        });
    }

    @Override
    public void registerAfterBlockBreak(AfterBlockBreakHandler handler) {
        NeoForge.EVENT_BUS.addListener((BlockDropsEvent event) -> {
            if (!(event.getBreaker() instanceof net.minecraft.world.entity.player.Player player)) {
                return;
            }
            if (!(event.getLevel() instanceof net.minecraft.world.level.Level level)) {
                return;
            }
            handler.onBreak(level, player, event.getPos(), event.getState(), event.getBlockEntity());
        });
    }

    @Override
    public void registerAttackBlock(AttackBlockHandler handler) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            InteractionResult result = handler.onAttack(
                    event.getEntity(),
                    event.getLevel(),
                    event.getHand(),
                    event.getPos(),
                    event.getFace()
            );
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
            }
        });
    }

    @Override
    public void registerUseBlock(UseBlockHandler handler) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            InteractionResult result = handler.onUse(
                    event.getEntity(),
                    event.getLevel(),
                    event.getHand(),
                    event.getHitVec()
            );
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
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
    public void addValidBlockEntityBlock(BlockEntityType<?> type, Block block) {
        blockEntityValidBlocks.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(block);
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

    private record PendingClientboundPayload<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        void register(PayloadRegistrar registrar) {
            registrar.playToClient(type, codec);
        }
    }

    private record PendingServerboundPayload<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        @SuppressWarnings("unchecked")
        void register(
                PayloadRegistrar registrar,
                Map<CustomPacketPayload.Type<?>, BiConsumer<?, ServerPlayContext>> handlers
        ) {
            BiConsumer<T, ServerPlayContext> handler =
                    (BiConsumer<T, ServerPlayContext>) handlers.get(type);
            if (handler == null) {
                registrar.playToServer(type, codec, (payload, context) -> {
                });
                return;
            }
            registrar.playToServer(type, codec, (payload, context) ->
                    handler.accept(payload, new ServerPlayContext() {
                        @Override
                        public MinecraftServer server() {
                            return ((ServerPlayer) context.player()).level().getServer();
                        }

                        @Override
                        public ServerPlayer player() {
                            return (ServerPlayer) context.player();
                        }
                    }));
        }
    }

    private record SpawnPlacementEntry<T extends Mob>(
            EntityType<T> type,
            SpawnPlacementType placement,
            Heightmap.Types heightmap,
            SpawnPredicate<T> predicate
    ) {
        void register(RegisterSpawnPlacementsEvent event) {
            event.register(
                    type,
                    placement,
                    heightmap,
                    (entityType, level, reason, pos, random) ->
                            predicate.test(entityType, level, reason, pos, random),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }
    }

    private record AttributeEntry(
            EntityType<? extends LivingEntity> type,
            Supplier<AttributeSupplier.Builder> attributes
    ) {
    }
}
