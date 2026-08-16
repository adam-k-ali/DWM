package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
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
 * Block entity for the First Doctor console. Holds {@code tardisId} for control interactions,
 * a synced chameleon variant for the Panel6 hologram preview, and synced console instruments
 * (stabilisers, cloak, door lock, and exterior environment readings).
 * Server tick maintains one {@link ConsoleControlInteractionEntity} per control.
 */
public class FirstDoctorConsoleBlockEntity extends BlockEntity {
    /** Search radius so outer-deck controls (outside the 1×1 cell) are found. */
    private static final double CONTROL_SEARCH_PADDING = 2.5;

    private static final int READOUT_INTERVAL_TICKS = 20;

    private @Nullable UUID tardisId;
    private TardisChameleonVariant syncedVariant = TardisChameleonVariant.TT_CAPSULE;
    private boolean syncedStabilisersEnabled = true;
    private boolean syncedCloaked;
    private boolean syncedDoorsLocked;
    private boolean syncedNoSignal = true;
    private float syncedOxygen;
    private float syncedPressure;
    private float syncedTemperature;
    private float syncedRadiation;

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

    public TardisChameleonVariant getSyncedVariant() {
        return syncedVariant == null ? TardisChameleonVariant.TT_CAPSULE : syncedVariant;
    }

    /**
     * Updates the hologram variant and notifies tracking clients when on the server.
     */
    public void setSyncedVariant(@Nullable TardisChameleonVariant variant) {
        this.syncedVariant = variant == null ? TardisChameleonVariant.TT_CAPSULE : variant;
        setChanged();
        notifyClients();
    }

    public boolean isSyncedStabilisersEnabled() {
        return syncedStabilisersEnabled;
    }

    /**
     * Updates the stabilisers dial pose and notifies tracking clients when on the server.
     */
    public void setSyncedStabilisersEnabled(boolean enabled) {
        this.syncedStabilisersEnabled = enabled;
        setChanged();
        notifyClients();
    }

    public boolean isSyncedCloaked() {
        return syncedCloaked;
    }

    public void setSyncedCloaked(boolean cloaked) {
        this.syncedCloaked = cloaked;
        setChanged();
        notifyClients();
    }

    public boolean isSyncedDoorsLocked() {
        return syncedDoorsLocked;
    }

    public void setSyncedDoorsLocked(boolean locked) {
        this.syncedDoorsLocked = locked;
        setChanged();
        notifyClients();
    }

    public ExteriorEnvironmentReadout.Reading syncedReading() {
        if (syncedNoSignal) {
            return ExteriorEnvironmentReadout.Reading.none();
        }
        return new ExteriorEnvironmentReadout.Reading(
                false, syncedOxygen, syncedPressure, syncedTemperature, syncedRadiation);
    }

    public void setSyncedReading(ExteriorEnvironmentReadout.Reading reading) {
        boolean noSignal = reading == null || reading.noSignal();
        float oxygen = noSignal ? 0.0F : reading.oxygen();
        float pressure = noSignal ? 0.0F : reading.pressure();
        float temperature = noSignal ? 0.0F : reading.temperature();
        float radiation = noSignal ? 0.0F : reading.radiation();
        if (this.syncedNoSignal == noSignal
                && this.syncedOxygen == oxygen
                && this.syncedPressure == pressure
                && this.syncedTemperature == temperature
                && this.syncedRadiation == radiation) {
            return;
        }
        this.syncedNoSignal = noSignal;
        this.syncedOxygen = oxygen;
        this.syncedPressure = pressure;
        this.syncedTemperature = temperature;
        this.syncedRadiation = radiation;
        setChanged();
        notifyClients();
    }

    private void refreshLinkedState(ServerLevel interiorWorld) {
        refreshSecurityState();
        refreshEnvironmentReadout(interiorWorld);
    }

    private void refreshSecurityState() {
        if (tardisId == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return;
        }
        if (syncedCloaked != model.cloaked) {
            setSyncedCloaked(model.cloaked);
        }
        if (syncedDoorsLocked != model.doorsLocked) {
            setSyncedDoorsLocked(model.doorsLocked);
        }
    }

    private void refreshEnvironmentReadout(ServerLevel interiorWorld) {
        if (tardisId == null) {
            setSyncedReading(ExteriorEnvironmentReadout.Reading.none());
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            setSyncedReading(ExteriorEnvironmentReadout.Reading.none());
            return;
        }
        boolean inFlight = TardisTravelService.isTraveling(tardisId);
        ServerLevel exterior = resolveExterior(interiorWorld, model.exteriorDimension);
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        setSyncedReading(ExteriorEnvironmentReadout.sample(exterior, exteriorPos, inFlight));
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
        output.putString("syncedVariant", getSyncedVariant().getId().toString());
        output.putBoolean("syncedStabilisersEnabled", syncedStabilisersEnabled);
        output.putBoolean("syncedCloaked", syncedCloaked);
        output.putBoolean("syncedDoorsLocked", syncedDoorsLocked);
        output.putBoolean("syncedNoSignal", syncedNoSignal);
        output.putFloat("syncedOxygen", syncedOxygen);
        output.putFloat("syncedPressure", syncedPressure);
        output.putFloat("syncedTemperature", syncedTemperature);
        output.putFloat("syncedRadiation", syncedRadiation);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        syncedVariant = parseVariant(input.getStringOr("syncedVariant", ""));
        syncedStabilisersEnabled = input.getBooleanOr("syncedStabilisersEnabled", true);
        syncedCloaked = input.getBooleanOr("syncedCloaked", false);
        syncedDoorsLocked = input.getBooleanOr("syncedDoorsLocked", false);
        syncedNoSignal = input.getBooleanOr("syncedNoSignal", true);
        syncedOxygen = input.getFloatOr("syncedOxygen", 0.0F);
        syncedPressure = input.getFloatOr("syncedPressure", 0.0F);
        syncedTemperature = input.getFloatOr("syncedTemperature", 0.0F);
        syncedRadiation = input.getFloatOr("syncedRadiation", 0.0F);
    }

    private static TardisChameleonVariant parseVariant(String id) {
        if (id == null || id.isBlank()) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        try {
            return TardisChameleonVariant.fromId(identifier);
        } catch (IllegalArgumentException ignored) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
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
