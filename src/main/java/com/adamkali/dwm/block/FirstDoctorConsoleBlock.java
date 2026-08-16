package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.network.OpenPlayerLocatorScreen;
import com.adamkali.dwm.network.OpenWaypointScreen;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.CloakLogic;
import com.adamkali.dwm.tardis.logic.CoordinateLockLogic;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.FastReturnLogic;
import com.adamkali.dwm.tardis.logic.FirstDoctorConsoleSync;
import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import com.adamkali.dwm.tardis.logic.StabiliserLogic;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.logic.TelepathicCircuitLogic;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    /**
     * Outline matches the pedestal, not a solid 3×3 volume. Control picking uses interaction
     * entities on the outer deck; a large outline box occludes those entities from outside.
     */
    public static final VoxelShape OUTLINE_SHAPE = COLLISION_SHAPE;

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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) {
            return null;
        }
        return createTickerHelper(
                type,
                DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY,
                FirstDoctorConsoleBlockEntity::serverTick
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        // Controls are activated via ConsoleControlInteractionEntity hitboxes, not look-ray AABBs.
        return InteractionResult.PASS;
    }

    /**
     * Routes an interaction-entity click to the matching console control handler.
     */
    public static InteractionResult activateControl(
            FirstDoctorConsoleControls.LookTarget target,
            Level world,
            BlockPos pos,
            Player player
    ) {
        if (target == FirstDoctorConsoleControls.LookTarget.NONE) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
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
            player.sendOverlayMessage(Component.translatable(unavailableKey(target)));
            return InteractionResult.CONSUME;
        }

        return switch (target) {
            case BIOME_SELECTOR -> handleBiomeSelector(world, pos, player, serverWorld, tardisId);
            case PLANET_LOCATOR -> handlePlanetLocator(world, pos, player, serverWorld, tardisId);
            case WAYPOINT_SELECTOR -> handleWaypointSelector(world, pos, serverPlayer, tardisId);
            case PLAYER_LOCATOR -> handlePlayerLocator(world, pos, serverPlayer, serverWorld, tardisId);
            case MATERIALISATION_LEVER -> handleMaterialisationLever(world, pos, player, serverWorld, tardisId);
            case CHAMELEON_CIRCUIT -> handleChameleonCircuit(world, pos, player, serverWorld, console, tardisId);
            case FAST_RETURN -> handleFastReturn(world, pos, player, tardisId);
            case STABILISERS -> handleStabilisers(world, pos, player, serverWorld, console, tardisId);
            case OXYGEN_READER -> handleReader(player, console, ExteriorEnvironmentReadout.Reading::oxygen, "dwm.console.oxygen");
            case PRESSURE_READER -> handleReader(player, console, ExteriorEnvironmentReadout.Reading::pressure, "dwm.console.pressure");
            case TEMPERATURE_READER -> handleReader(player, console, ExteriorEnvironmentReadout.Reading::temperature, "dwm.console.temperature");
            case RADIATION_READER -> handleReader(player, console, ExteriorEnvironmentReadout.Reading::radiation, "dwm.console.radiation");
            case REFUELER -> handleRefueler(player);
            case TELEPATHIC_CIRCUIT -> handleTelepathic(world, pos, player, tardisId);
            case CLOAK -> handleCloak(world, pos, player, serverWorld, console, tardisId);
            case DOOR_LOCK -> handleDoorLock(world, pos, player, serverWorld, console, tardisId);
            case COORDINATE_LOCK_X -> handleCoordinateLock(world, pos, player, serverWorld, console, tardisId, CoordinateLockLogic.Axis.X);
            case COORDINATE_LOCK_Y -> handleCoordinateLock(world, pos, player, serverWorld, console, tardisId, CoordinateLockLogic.Axis.Y);
            case COORDINATE_LOCK_Z -> handleCoordinateLock(world, pos, player, serverWorld, console, tardisId, CoordinateLockLogic.Axis.Z);
            case NONE -> InteractionResult.PASS;
        };
    }

    private static String unavailableKey(FirstDoctorConsoleControls.LookTarget target) {
        return switch (target) {
            case PLANET_LOCATOR -> "dwm.console.dimension_unavailable";
            case WAYPOINT_SELECTOR -> "dwm.console.waypoint_unavailable";
            case PLAYER_LOCATOR -> "dwm.console.player_locator_unavailable";
            case BIOME_SELECTOR -> "dwm.console.biome_unavailable";
            case CHAMELEON_CIRCUIT -> "dwm.console.chameleon_unavailable";
            case FAST_RETURN -> "dwm.console.fast_return_unavailable";
            case STABILISERS -> "dwm.console.stabilisers_unavailable";
            case OXYGEN_READER, PRESSURE_READER, TEMPERATURE_READER, RADIATION_READER ->
                    "dwm.console.reader_unavailable";
            case REFUELER -> "dwm.console.refueler_unavailable";
            case TELEPATHIC_CIRCUIT -> "dwm.console.telepathic_unavailable";
            case CLOAK -> "dwm.console.cloak_unavailable";
            case DOOR_LOCK -> "dwm.console.door_lock_unavailable";
            case COORDINATE_LOCK_X, COORDINATE_LOCK_Y, COORDINATE_LOCK_Z ->
                    "dwm.console.coordinate_lock_unavailable";
            case MATERIALISATION_LEVER, NONE -> "dwm.console.travel_unavailable";
        };
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
                String reason = TardisTravelService.peekLastMaterialiseFailureReason();
                if (TardisTravelService.FAIL_INVALID_LANDING.equals(reason)) {
                    player.sendOverlayMessage(Component.translatable("dwm.console.travel_invalid_landing"));
                } else {
                    player.sendOverlayMessage(Component.translatable("dwm.console.travel_player_offline"));
                }
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
        TardisDataModel model = TardisDataLoader.get(tardisId);
        UUID selectedPlayerUuid = model == null ? null : model.selectedPlayerUuid;
        ServerPlayNetworking.send(player, OpenPlayerLocatorScreen.of(tardisId, players, selectedPlayerUuid));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleChameleonCircuit(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            FirstDoctorConsoleBlockEntity console,
            UUID tardisId
    ) {
        Optional<TardisChameleonVariant> next =
                TardisLogic.cycleVariant(tardisId, serverWorld.getServer());
        if (next.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.chameleon_unavailable"));
            return InteractionResult.CONSUME;
        }
        // cycleVariant already syncs via FirstDoctorConsoleSync; keep local BE fresh for same-tick hologram.
        console.setSyncedVariant(next.get());
        Component variantName = Component.translatable(next.get().getId().toLanguageKey());
        player.sendOverlayMessage(Component.translatable("dwm.console.chameleon_selected", variantName));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleFastReturn(
            Level world,
            BlockPos pos,
            Player player,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.fast_return_unavailable"));
            return InteractionResult.CONSUME;
        }
        Optional<TardisExteriorLocation> selected = FastReturnLogic.cycle(model);
        if (selected.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.fast_return_empty"));
            return InteractionResult.CONSUME;
        }
        TardisExteriorLocation location = selected.get();
        int total = model.getLocationHistory().size();
        int indexDisplay = model.selectedFastReturnIndex + 1;
        Component dimensionName = dimensionDisplayName(location.dimension);
        player.sendOverlayMessage(Component.translatable(
                "dwm.console.fast_return_selected",
                indexDisplay,
                total,
                dimensionName,
                location.x,
                location.y,
                location.z
        ));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleStabilisers(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            FirstDoctorConsoleBlockEntity console,
            UUID tardisId
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.stabilisers_unavailable"));
            return InteractionResult.CONSUME;
        }
        boolean enabled = StabiliserLogic.toggle(model);
        console.setSyncedStabilisersEnabled(enabled);
        FirstDoctorConsoleSync.syncStabilisers(serverWorld.getServer(), tardisId, enabled);
        player.sendOverlayMessage(Component.translatable(
                enabled ? "dwm.console.stabilisers_on" : "dwm.console.stabilisers_off"));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleReader(
            Player player,
            FirstDoctorConsoleBlockEntity console,
            java.util.function.ToDoubleFunction<ExteriorEnvironmentReadout.Reading> value,
            String key
    ) {
        ExteriorEnvironmentReadout.Reading reading = console.syncedReading();
        if (reading.noSignal()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.reader_no_signal"));
            return InteractionResult.CONSUME;
        }
        int percent = Math.round(reading.needle((float) value.applyAsDouble(reading)) * 100.0F);
        player.sendOverlayMessage(Component.translatable(key, percent));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleRefueler(Player player) {
        player.sendOverlayMessage(Component.translatable("dwm.console.refueler_stable"));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleTelepathic(
            Level world,
            BlockPos pos,
            Player player,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return InteractionResult.CONSUME;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !(player instanceof ServerPlayer serverPlayer)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.telepathic_unavailable"));
            return InteractionResult.CONSUME;
        }
        TelepathicCircuitLogic.arm(model, serverPlayer.getUUID());
        TelepathicCircuitLogic.Destination destination = TelepathicCircuitLogic.resolveFor(serverPlayer);
        player.sendOverlayMessage(Component.translatable(
                destination.usedHome()
                        ? "dwm.console.telepathic_home"
                        : "dwm.console.telepathic_spawn"));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleCloak(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            FirstDoctorConsoleBlockEntity console,
            UUID tardisId
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.cloak_unavailable"));
            return InteractionResult.CONSUME;
        }
        boolean cloaked = CloakLogic.toggle(model);
        console.setSyncedCloaked(cloaked);
        FirstDoctorConsoleSync.syncCloak(serverWorld.getServer(), tardisId, cloaked);
        player.sendOverlayMessage(Component.translatable(
                cloaked ? "dwm.console.cloak_on" : "dwm.console.cloak_off"));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleDoorLock(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            FirstDoctorConsoleBlockEntity console,
            UUID tardisId
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.door_lock_unavailable"));
            return InteractionResult.CONSUME;
        }
        boolean locked = DoorLockLogic.toggle(model);
        console.setSyncedDoorsLocked(locked);
        FirstDoctorConsoleSync.syncDoorsLocked(serverWorld.getServer(), tardisId, locked);
        player.sendOverlayMessage(Component.translatable(
                locked ? "dwm.console.doors_locked" : "dwm.console.doors_unlocked"));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleCoordinateLock(
            Level world,
            BlockPos pos,
            Player player,
            ServerLevel serverWorld,
            FirstDoctorConsoleBlockEntity console,
            UUID tardisId,
            CoordinateLockLogic.Axis axis
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.coordinate_lock_unavailable"));
            return InteractionResult.CONSUME;
        }
        boolean locked = CoordinateLockLogic.toggle(model, axis);
        console.setSyncedAxisLock(axis, locked);
        FirstDoctorConsoleSync.syncCoordinateLocks(serverWorld.getServer(), tardisId, model);
        String axisKey = switch (axis) {
            case X -> locked ? "dwm.console.lock_x_on" : "dwm.console.lock_x_off";
            case Y -> locked ? "dwm.console.lock_y_on" : "dwm.console.lock_y_off";
            case Z -> locked ? "dwm.console.lock_z_on" : "dwm.console.lock_z_off";
        };
        player.sendOverlayMessage(Component.translatable(axisKey));
        playClick(world, pos);
        return InteractionResult.SUCCESS;
    }

    private static Component dimensionDisplayName(@Nullable String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return Component.literal("?");
        }
        Identifier id = Identifier.tryParse(dimensionId);
        if (id == null) {
            return Component.literal(dimensionId);
        }
        return Component.translatable(id.toLanguageKey("dimension"));
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
