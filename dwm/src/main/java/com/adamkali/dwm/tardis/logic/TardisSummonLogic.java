package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Stattenheim remote: sneak-use a block to summon the caller's owned TARDIS to that site.
 */
public final class TardisSummonLogic {
    private TardisSummonLogic() {
    }

    public enum Result {
        SUMMONED,
        NO_TARDIS,
        IN_PROGRESS,
        INVALID_LANDING,
        UNAVAILABLE,
        CIRCUIT_BROKEN,
        EMPTY_TANK
    }

    public static String overlayKey(Result result) {
        return overlayKey(result, null);
    }

    public static String overlayKey(Result result, @Nullable TardisDataModel model) {
        return switch (result) {
            case SUMMONED -> "dwm.stattenheim.summoned";
            case NO_TARDIS -> "dwm.stattenheim.no_tardis";
            case IN_PROGRESS -> "dwm.stattenheim.in_progress";
            case INVALID_LANDING -> "dwm.stattenheim.invalid_landing";
            case UNAVAILABLE -> "dwm.stattenheim.unavailable";
            case CIRCUIT_BROKEN -> CircuitFittedLogic.CIRCUIT_BROKEN_KEY;
            case EMPTY_TANK -> ArtronLogic.spendRefuseKey(model);
        };
    }

    /**
     * Phase/ownership gate used before world landing search.
     */
    public static Result preview(@Nullable TardisDataModel model) {
        if (model == null) {
            return Result.NO_TARDIS;
        }
        if (CircuitFittedLogic.isBroken(model, TardisCircuit.REMOTE_SUMMON)) {
            return Result.CIRCUIT_BROKEN;
        }
        TardisTravelPhase phase = model.getTravelPhase();
        if (phase == TardisTravelPhase.DEMATERIALISING || phase == TardisTravelPhase.MATERIALISING) {
            return Result.IN_PROGRESS;
        }
        if (phase == TardisTravelPhase.IDLE && !model.hasExteriorLocation) {
            return Result.UNAVAILABLE;
        }
        if (phase == TardisTravelPhase.IDLE || phase == TardisTravelPhase.IN_FLIGHT) {
            return Result.SUMMONED;
        }
        return Result.UNAVAILABLE;
    }

    public static BlockPos landingOrigin(BlockPos clickedPos, Direction clickedFace) {
        return clickedPos.relative(clickedFace);
    }

    /**
     * Horizontal direction from {@code landing} toward the player, used as shell door facing.
     */
    public static Direction doorFacingToward(
            BlockPos landing,
            double playerX,
            double playerZ,
            Direction horizontalFallback
    ) {
        Direction fallback = horizontalFallback != null && horizontalFallback.getAxis().isHorizontal()
                ? horizontalFallback
                : Direction.NORTH;
        if (landing == null) {
            return fallback;
        }
        double dx = playerX - (landing.getX() + 0.5);
        double dz = playerZ - (landing.getZ() + 0.5);
        if (dx * dx + dz * dz < 1.0e-6) {
            return fallback;
        }
        float yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
        return Direction.fromYRot(yaw);
    }

    public static Result summon(
            Player player,
            ServerLevel world,
            BlockPos clickedPos,
            Direction clickedFace
    ) {
        if (player == null || world == null || clickedPos == null || clickedFace == null) {
            return Result.UNAVAILABLE;
        }
        Optional<TardisDataModel> owned = TardisDataLoader.findOwnedBy(player.getUUID());
        TardisDataModel model = owned.orElse(null);
        Result preview = preview(model);
        if (preview != Result.SUMMONED) {
            return preview;
        }

        BlockPos origin = landingOrigin(clickedPos, clickedFace);
        Direction doorFacing = doorFacingToward(origin, player.getX(), player.getZ(), player.getDirection());
        Optional<BlockPos> resolved = LandingSiteLogic.findLandingAtOrNearby(world, origin, doorFacing);
        if (resolved.isEmpty()) {
            return Result.INVALID_LANDING;
        }
        BlockPos landing = resolved.get();
        doorFacing = doorFacingToward(landing, player.getX(), player.getZ(), player.getDirection());
        if (!LandingSiteLogic.isValidLanding(world, landing, doorFacing)) {
            Optional<BlockPos> retried = LandingSiteLogic.findLandingAtOrNearby(world, landing, doorFacing);
            if (retried.isEmpty()) {
                return Result.INVALID_LANDING;
            }
            landing = retried.get();
        }
        int facingRotation = TardisExteriorFacing.facingRotationForDoor(doorFacing);
        String destinationDimension = world.dimension().identifier().toString();

        TardisLogic.slamDoorsClosed(model.uuid, world.getServer());
        TardisTravelPhase phase = model.getTravelPhase();
        if (phase == TardisTravelPhase.IN_FLIGHT) {
            InteractionResult result = TardisTravelService.materialiseAt(
                    model.uuid,
                    world.getServer(),
                    world,
                    landing,
                    facingRotation
            );
            if (result == InteractionResult.SUCCESS) {
                return Result.SUMMONED;
            }
            if (TardisTravelService.FAIL_INVALID_LANDING.equals(TardisTravelService.peekLastMaterialiseFailureReason())) {
                return Result.INVALID_LANDING;
            }
            return Result.UNAVAILABLE;
        }

        InteractionResult started = TardisTravelService.startSummonTravel(
                model.uuid,
                world.getServer(),
                destinationDimension,
                landing,
                facingRotation,
                player.getAbilities().instabuild
        );
        if (started == InteractionResult.SUCCESS) {
            return Result.SUMMONED;
        }
        if (started == InteractionResult.PASS) {
            return Result.IN_PROGRESS;
        }
        if (TardisTravelService.FAIL_INSUFFICIENT_ARTRON.equals(TardisTravelService.peekLastTravelFailureReason())) {
            return Result.EMPTY_TANK;
        }
        return Result.UNAVAILABLE;
    }
}
