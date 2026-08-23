package com.adamkali.dwm.platform.fabric;

import com.adamkali.dwm.platform.DwmPlatform;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.CompostableItemRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fabric common/server implementation of {@link DwmPlatform}.
 */
public final class FabricDwmPlatform implements DwmPlatform {
    @Override
    public void registerServerStarted(Consumer<MinecraftServer> handler) {
        ServerLifecycleEvents.SERVER_STARTED.register(handler::accept);
    }

    @Override
    public void registerAfterSave(AfterSaveHandler handler) {
        ServerLifecycleEvents.AFTER_SAVE.register(handler::onAfterSave);
    }

    @Override
    public void registerServerStopped(Consumer<MinecraftServer> handler) {
        ServerLifecycleEvents.SERVER_STOPPED.register(handler::accept);
    }

    @Override
    public void registerEndServerTick(Consumer<MinecraftServer> handler) {
        ServerTickEvents.END_SERVER_TICK.register(handler::accept);
    }

    @Override
    public void registerCommands(CommandRegistrationHandler handler) {
        CommandRegistrationCallback.EVENT.register(handler::register);
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ServerPlayContext> handler
    ) {
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                handler.accept(payload, new ServerPlayContext() {
                    @Override
                    public MinecraftServer server() {
                        return context.server();
                    }

                    @Override
                    public ServerPlayer player() {
                        return context.player();
                    }
                }));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public Collection<ServerPlayer> playersTracking(ServerLevel level, BlockPos pos) {
        return PlayerLookup.tracking(level, pos);
    }

    @Override
    public Collection<ServerPlayer> playersAround(ServerLevel level, Vec3 center, double radius) {
        return PlayerLookup.around(level, center, radius);
    }

    @Override
    public void registerBeforeBlockBreak(BeforeBlockBreakHandler handler) {
        PlayerBlockBreakEvents.BEFORE.register(handler::allowBreak);
    }

    @Override
    public void registerAfterBlockBreak(AfterBlockBreakHandler handler) {
        PlayerBlockBreakEvents.AFTER.register(handler::onBreak);
    }

    @Override
    public void registerAttackBlock(AttackBlockHandler handler) {
        AttackBlockCallback.EVENT.register(handler::onAttack);
    }

    @Override
    public void registerUseBlock(UseBlockHandler handler) {
        UseBlockCallback.EVENT.register(handler::onUse);
    }

    @Override
    public void modifyCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeTabOutput> modifier) {
        CreativeModeTabEvents.modifyOutputEvent(tab).register(content -> modifier.accept(content::accept));
    }

    @Override
    public void registerCompostable(ItemLike item, float chance) {
        CompostableItemRegistry.INSTANCE.add(item, chance);
    }

    @Override
    public void registerStrippable(Block input, Block stripped) {
        StrippableBlockRegistry.register(input, stripped);
    }

    @Override
    public void registerFlammable(Block block, int burnOdds, int spreadOdds) {
        FlammableBlockRegistry.getDefaultInstance().add(block, burnOdds, spreadOdds);
    }

    @Override
    public BlockSetType registerBlockSetType(Identifier id) {
        return new BlockSetTypeBuilder().register(id);
    }

    @Override
    public WoodType registerWoodType(Identifier id, BlockSetType setType) {
        return new WoodTypeBuilder().register(id, setType);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> buildBlockEntityType(
            BlockEntityFactory<T> factory,
            Block... blocks
    ) {
        return FabricBlockEntityTypeBuilder.<T>create(factory::create, blocks).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Mob> void registerSpawnPlacement(
            EntityType<T> type,
            SpawnPlacementType placement,
            Heightmap.Types heightmap,
            SpawnPredicate<T> predicate
    ) {
        SpawnPlacements.register(
                type,
                placement,
                heightmap,
                (entityType, level, reason, pos, random) ->
                        predicate.test((EntityType<T>) entityType, level, reason, pos, random)
        );
    }

    @Override
    public void registerDefaultAttributes(
            EntityType<? extends LivingEntity> type,
            Supplier<AttributeSupplier.Builder> attributes
    ) {
        FabricDefaultAttributeRegistry.register(type, attributes.get());
    }
}
