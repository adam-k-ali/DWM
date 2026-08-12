package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.network.OpenPlayerLocatorScreen;
import com.adamkali.dwm.network.OpenWaypointScreen;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * First Doctor TARDIS console. Mesh is drawn by {@code FirstDoctorConsoleBlockEntityRenderer}.
 */
public class FirstDoctorConsoleBlock extends BaseEntityBlock {
    private static final MapCodec<FirstDoctorConsoleBlock> CODEC = simpleCodec(FirstDoctorConsoleBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Approximate hexagonal pedestal: ~1.6×1.6 footprint, ~1.25 blocks tall. */
    public static final VoxelShape COLLISION_SHAPE = Shapes.box(-0.3, 0.0, -0.3, 1.3, 1.25, 1.3);

    /** Outline includes Panel3 selectors so raycast can target them. */
    public static final VoxelShape OUTLINE_SHAPE = Shapes.box(-0.5, 0.0, -0.5, 1.5, 1.6, 1.5);

    public FirstDoctorConsoleBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** True when players must not break this block (survival or creative left-click). */
    public static boolean isPlayerBreakDenied(BlockState state) {
        return state.is(DWMBlocks.FIRST_DOCTOR_CONSOLE);
    }

    @Override
    protected MapCodec<FirstDoctorConsoleBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FirstDoctorConsoleBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Direction facing = state.getValue(FACING);
        FirstDoctorConsoleControls.Panel3Control panel3 =
                FirstDoctorConsoleControls.resolvePanel3LookHit(facing, pos, player);
        FirstDoctorConsoleControls.Panel6Control panel6 =
                FirstDoctorConsoleControls.resolvePanel6LookHit(facing, pos, player);
        if (panel3 == null && panel6 == null) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof FirstDoctorConsoleBlockEntity console)
                || !(world instanceof ServerLevel serverWorld)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        UUID tardisId = console.getTardisId();
        if (tardisId == null) {
            player.sendOverlayMessage(Component.translatable(unavailableKey(panel3, panel6)));
            return InteractionResult.CONSUME;
        }

        // Prefer Panel3 when both panels somehow hit (unusual; different deck angles).
        if (panel3 != null) {
            return switch (panel3) {
                case BIOME -> handleBiomeSelector(world, pos, player, serverWorld, tardisId);
                case PLANET -> handlePlanetLocator(world, pos, player, serverWorld, tardisId);
                case WAYPOINT -> handleWaypointSelector(world, pos, serverPlayer, tardisId);
                case PLAYER -> handlePlayerLocator(world, pos, serverPlayer, serverWorld, tardisId);
            };
        }

        return switch (panel6) {
            case LEVER -> handleMaterialisationLever(world, pos, player, serverWorld, tardisId);
        };
    }

    private static String unavailableKey(
            @Nullable FirstDoctorConsoleControls.Panel3Control panel3,
            @Nullable FirstDoctorConsoleControls.Panel6Control panel6
    ) {
        if (panel3 != null) {
            return switch (panel3) {
                case PLANET -> "dwm.console.dimension_unavailable";
                case WAYPOINT -> "dwm.console.waypoint_unavailable";
                case PLAYER -> "dwm.console.player_locator_unavailable";
                case BIOME -> "dwm.console.biome_unavailable";
            };
        }
        return "dwm.console.travel_unavailable";
    }

    private static InteractionResult handleMaterialisationLever(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            UUID tardisId
    ) {
        TardisTravelPhase phase = TardisLogic.getTravelPhase(tardisId);
        if (phase == TardisTravelPhase.DEMATERIALISING) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_dematerialising"));
            return InteractionResult.CONSUME;
        }
        if (phase == TardisTravelPhase.MATERIALISING) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_materialising"));
            return InteractionResult.CONSUME;
        }

        InteractionResult result;
        String successKey;
        if (phase.awaitsMaterialise()) {
            result = TardisTravelService.requestMaterialise(tardisId, serverWorld.getServer());
            if (result == InteractionResult.FAIL) {
                player.sendOverlayMessage(Component.translatable("dwm.console.travel_player_offline"));
                return InteractionResult.CONSUME;
            }
            successKey = "dwm.console.travel_materialising";
        } else {
            result = TardisTravelService.startTravel(tardisId, serverWorld.getServer());
            successKey = "dwm.console.travel_dematerialising";
        }

        if (result == InteractionResult.SUCCESS) {
            player.sendOverlayMessage(Component.translatable(successKey));
            playClick(world, pos);
            return InteractionResult.SUCCESS;
        }
        if (result == InteractionResult.PASS) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_progress"));
            return InteractionResult.CONSUME;
        }
        player.sendOverlayMessage(Component.translatable("dwm.console.travel_unavailable"));
        return InteractionResult.CONSUME;
    }

    private static InteractionResult handleBiomeSelector(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }

        Optional<Identifier> selected = TardisLogic.cycleSelectedBiome(tardisId, serverWorld.getServer());
        if (selected.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.biome_unavailable"));
            return InteractionResult.CONSUME;
        }

        Component biomeName = Component.translatable(selected.get().toLanguageKey("biome"));
        player.sendOverlayMessage(Component.translatable("dwm.console.biome_selected", biomeName));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handlePlanetLocator(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }

        Optional<Identifier> selected = TardisLogic.cycleSelectedDimension(tardisId, serverWorld.getServer());
        if (selected.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.dimension_unavailable"));
            return InteractionResult.CONSUME;
        }

        Component dimensionName = Component.translatable(selected.get().toLanguageKey("dimension"));
        player.sendOverlayMessage(Component.translatable("dwm.console.dimension_selected", dimensionName));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleWaypointSelector(
            Level world,
            BlockPos pos,
            ServerPlayer player,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        ServerPlayNetworking.send(player, OpenWaypointScreen.of(tardisId, model));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handlePlayerLocator(
            Level world,
            BlockPos pos,
            ServerPlayer player,
            ServerLevel serverWorld,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }
        List<PlayerLocatorLogic.PlayerEntry> players =
                PlayerLocatorLogic.listOnlineExcluding(serverWorld.getServer(), player.getUUID());
        ServerPlayNetworking.send(player, OpenPlayerLocatorScreen.of(tardisId, players));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static void playClick(Level world, BlockPos pos) {
        world.playSound(
                null,
                pos,
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.BLOCKS,
                0.4F,
                1.0F
        );
    }
}
