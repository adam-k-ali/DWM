package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.ConsoleDisplayState;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the First Doctor console. Holds {@code tardisId} for control interactions
 * and a synced {@link ConsoleDisplayState} for client instruments / HUD / BER.
 * Server tick maintains one {@link ConsoleControlInteractionEntity} per control.
 */
public class FirstDoctorConsoleBlockEntity extends BlockEntity {
    /** Search radius so outer-deck controls (outside the 1×1 cell) are found. */
    private static final double CONTROL_SEARCH_PADDING = 2.5;

    private static final int READOUT_INTERVAL_TICKS = 20;

    private @Nullable UUID tardisId;
    private ConsoleDisplayState synced = ConsoleDisplayState.defaults();

    public FirstDoctorConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(
            Level world,
            BlockPos pos,
            BlockState state,
            FirstDoctorConsoleBlockEntity console
    ) {
        console.maintainControlEntities(world, pos, state);
        if (world.getGameTime() % READOUT_INTERVAL_TICKS == 0 && world instanceof ServerLevel serverWorld) {
            console.refreshLinkedState(serverWorld);
        }
    }

    private void maintainControlEntities(Level world, BlockPos pos, BlockState state) {
        Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);
        AABB search = new AABB(pos).inflate(CONTROL_SEARCH_PADDING);
        List<ConsoleControlInteractionEntity> existing = world.getEntitiesOfClass(
                ConsoleControlInteractionEntity.class,
                search,
                entity -> entity.isBoundTo(pos)
        );

        EnumMap<LookTarget, ConsoleControlInteractionEntity> byTarget = new EnumMap<>(LookTarget.class);
        for (ConsoleControlInteractionEntity entity : existing) {
            LookTarget target = entity.getLookTarget();
            if (target == LookTarget.NONE) {
                entity.discard();
                continue;
            }
            ConsoleControlInteractionEntity prior = byTarget.putIfAbsent(target, entity);
            if (prior != null) {
                entity.discard();
            }
        }

        for (LookTarget target : LookTarget.interactiveValues()) {
            FirstDoctorConsoleControls.InteractionPose pose =
                    FirstDoctorConsoleControls.interactionPose(target, pos, facing);
            if (pose == null) {
                continue;
            }
            ConsoleControlInteractionEntity entity = byTarget.get(target);
            if (entity == null || entity.isRemoved()) {
                entity = new ConsoleControlInteractionEntity(DWMEntityTypes.CONSOLE_CONTROL, world);
                entity.bind(pos, target, pose);
                world.addFreshEntity(entity);
            } else {
                entity.applyPose(pose);
            }
        }
    }

    private void discardControlEntities() {
        if (level == null || level.isClientSide()) {
            return;
        }
        AABB search = new AABB(worldPosition).inflate(CONTROL_SEARCH_PADDING);
        List<ConsoleControlInteractionEntity> existing = level.getEntitiesOfClass(
                ConsoleControlInteractionEntity.class,
                search,
                entity -> entity.isBoundTo(worldPosition)
        );
        for (ConsoleControlInteractionEntity entity : existing) {
            entity.discard();
        }
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        setChanged();
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    public ConsoleDisplayState syncedDisplay() {
        return synced;
    }

    /**
     * Replaces the synced display snapshot and notifies tracking clients when on the server.
     * No-ops when the snapshot is equal to the current value.
     */
    public void setSyncedDisplay(ConsoleDisplayState next) {
        ConsoleDisplayState safe = next == null ? ConsoleDisplayState.defaults() : next;
        if (Objects.equals(this.synced, safe)) {
            return;
        }
        this.synced = safe;
        setChanged();
        notifyClients();
    }

    private void refreshLinkedState(ServerLevel interiorWorld) {
        TardisDataModel model = tardisId == null ? null : TardisDataLoader.get(tardisId);
        ExteriorEnvironmentReadout.Reading reading = sampleReading(interiorWorld, model);
        setSyncedDisplay(ConsoleDisplayState.from(model, reading));
    }

    private ExteriorEnvironmentReadout.Reading sampleReading(
            ServerLevel interiorWorld,
            @Nullable TardisDataModel model
    ) {
        if (tardisId == null || model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return ExteriorEnvironmentReadout.Reading.none();
        }
        boolean inFlight = TardisTravelService.isTraveling(tardisId);
        ServerLevel exterior = resolveExterior(interiorWorld, model.exteriorDimension);
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        return ExteriorEnvironmentReadout.sample(exterior, exteriorPos, inFlight);
    }

    private static @Nullable ServerLevel resolveExterior(ServerLevel interiorWorld, String dimensionId) {
        Identifier identifier = Identifier.tryParse(dimensionId);
        if (identifier == null) {
            return null;
        }
        return interiorWorld.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, identifier));
    }

    private void notifyClients() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setRemoved() {
        discardControlEntities();
        super.setRemoved();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        discardControlEntities();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (tardisId != null) {
            output.store("tardisId", UUIDUtil.CODEC, tardisId);
        }
        synced.write(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        synced = ConsoleDisplayState.read(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
