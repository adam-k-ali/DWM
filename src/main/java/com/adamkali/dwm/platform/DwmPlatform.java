package com.adamkali.dwm.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Common/server loader SPI. Client-only hooks live on {@link DwmClientPlatform}.
 */
public interface DwmPlatform {

    void registerServerStarted(Consumer<MinecraftServer> handler);

    void registerAfterSave(AfterSaveHandler handler);

    void registerServerStopped(Consumer<MinecraftServer> handler);

    void registerEndServerTick(Consumer<MinecraftServer> handler);

    void registerCommands(CommandRegistrationHandler handler);

    <T extends CustomPacketPayload> void registerClientboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    );

    <T extends CustomPacketPayload> void registerServerboundPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    );

    <T extends CustomPacketPayload> void registerServerboundHandler(
            CustomPacketPayload.Type<T> type,
            BiConsumer<T, ServerPlayContext> handler
    );

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    Collection<ServerPlayer> playersTracking(ServerLevel level, BlockPos pos);

    default boolean hasTrackingPlayers(ServerLevel level, BlockPos pos) {
        return !playersTracking(level, pos).isEmpty();
    }

    Collection<ServerPlayer> playersAround(ServerLevel level, Vec3 center, double radius);

    void registerBeforeBlockBreak(BeforeBlockBreakHandler handler);

    void registerAfterBlockBreak(AfterBlockBreakHandler handler);

    void registerAttackBlock(AttackBlockHandler handler);

    void registerUseBlock(UseBlockHandler handler);

    void modifyCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeTabOutput> modifier);

    void registerCompostable(ItemLike item, float chance);

    void registerStrippable(Block input, Block stripped);

    void registerFlammable(Block block, int burnOdds, int spreadOdds);

    BlockSetType registerBlockSetType(Identifier id);

    WoodType registerWoodType(Identifier id, BlockSetType setType);

    <T extends BlockEntity> BlockEntityType<T> buildBlockEntityType(
            BlockEntityFactory<T> factory,
            Block... blocks
    );

    <T extends Mob> void registerSpawnPlacement(
            EntityType<T> type,
            SpawnPlacementType placement,
            Heightmap.Types heightmap,
            SpawnPredicate<T> predicate
    );

    void registerDefaultAttributes(
            EntityType<? extends LivingEntity> type,
            Supplier<AttributeSupplier.Builder> attributes
    );

    @FunctionalInterface
    interface AfterSaveHandler {
        void onAfterSave(MinecraftServer server, boolean flush, boolean force);
    }

    @FunctionalInterface
    interface CommandRegistrationHandler {
        void register(
                CommandDispatcher<CommandSourceStack> dispatcher,
                CommandBuildContext buildContext,
                Commands.CommandSelection selection
        );
    }

    interface ServerPlayContext {
        MinecraftServer server();

        ServerPlayer player();
    }

    @FunctionalInterface
    interface BeforeBlockBreakHandler {
        boolean allowBreak(
                Level world,
                Player player,
                BlockPos pos,
                BlockState state,
                @Nullable BlockEntity blockEntity
        );
    }

    @FunctionalInterface
    interface AfterBlockBreakHandler {
        void onBreak(
                Level world,
                Player player,
                BlockPos pos,
                BlockState state,
                @Nullable BlockEntity blockEntity
        );
    }

    @FunctionalInterface
    interface AttackBlockHandler {
        InteractionResult onAttack(
                Player player,
                Level world,
                InteractionHand hand,
                BlockPos pos,
                Direction direction
        );
    }

    @FunctionalInterface
    interface UseBlockHandler {
        InteractionResult onUse(
                Player player,
                Level world,
                InteractionHand hand,
                BlockHitResult hitResult
        );
    }

    interface CreativeTabOutput {
        void accept(ItemLike item);
    }

    @FunctionalInterface
    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    interface SpawnPredicate<T extends net.minecraft.world.entity.Entity> {
        boolean test(
                EntityType<T> type,
                ServerLevelAccessor level,
                EntitySpawnReason reason,
                BlockPos pos,
                RandomSource random
        );
    }
}
