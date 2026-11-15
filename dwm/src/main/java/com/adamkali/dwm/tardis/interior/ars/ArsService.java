package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Grows Architectural Reconfiguration System pieces in a TARDIS interior: checks the
 * rules ({@link ArsLayoutLogic}), spends or refunds artron, updates {@link TardisDataModel#getArsPieces()}
 * and drives {@link ArsRoomPlacer}. Server thread only.
 *
 * <p>The world-facing methods take an explicit {@code (world, plotOrigin)} so GameTests can work away
 * from the {@code dwm:tardis} dimension; the {@link MinecraftServer} overloads resolve the real interior
 * level and {@link TardisPlotAllocator#plotOrigin}.
 */
public final class ArsService {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NO_TARDIS_KEY = "dwm.ars.no_tardis";
    public static final String IN_FLIGHT_KEY = "dwm.console.travel_in_flight";
    public static final String NO_INTERIOR_KEY = "dwm.ars.no_interior";
    public static final String MISSING_STRUCTURE_KEY = "dwm.ars.missing_structure";
    public static final String GROWN_KEY = "dwm.ars.grown";
    public static final String PIECE_NAME_PREFIX = "dwm.ars.piece.";

    /**
     * Outcome of {@link #grow}. {@code args} are translation arguments for
     * {@code translationKey}: on success the piece name ({@link Component}) and the artron spent; refusals carry none.
     */
    public record Result(boolean success, String translationKey, List<Object> args) {
        public Result {
            args = List.copyOf(args);
        }

        static Result refused(String translationKey) {
            return new Result(false, translationKey, List.of());
        }

        public Component message() {
            return Component.translatable(translationKey, args.toArray());
        }
    }

    private ArsService() {
    }

    /** Translation key of a piece's display name: {@code dwm.ars.piece.<path>}. */
    public static String pieceNameKey(Identifier pieceId) {
        return PIECE_NAME_PREFIX + pieceId.getPath();
    }

    /** Grows a piece in the TARDIS's plot in {@code dwm:tardis}. */
    public static Result grow(
            MinecraftServer server,
            UUID tardisId,
            Identifier pieceId,
            int cellX,
            int cellZ,
            int rotation,
            boolean instabuild
    ) {
        ServerLevel world = server == null ? null : server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (world == null || tardisId == null) {
            return Result.refused(NO_INTERIOR_KEY);
        }
        return grow(world, TardisPlotAllocator.plotOrigin(tardisId), tardisId, pieceId, cellX, cellZ, rotation, instabuild);
    }

    /**
     * Grows {@code pieceId} anchored at cell {@code (cellX, cellZ)} with {@code rotation} (clockwise quarter
     * turns). Refuses without changing anything when the TARDIS is unknown or in flight, the console room is
     * not generated at {@code plotOrigin}, the layout rules refuse, or the template is missing. Otherwise
     * spends the artron cost (free when {@code instabuild}), places the console annex and doorway for the
     * first piece, places the piece and opens every doorway it now shares.
     */
    public static Result grow(
            ServerLevel world,
            BlockPos plotOrigin,
            UUID tardisId,
            Identifier pieceId,
            int cellX,
            int cellZ,
            int rotation,
            boolean instabuild
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        @Nullable Result refusal = checkTardis(world, plotOrigin, tardisId, model);
        if (refusal != null) {
            return refusal;
        }
        Function<Identifier, ArsPieceDefinition> definitions = definitions(world);
        List<ArsPlacedPiece> layout = model.getArsPieces();
        ArsPlacedPiece piece = new ArsPlacedPiece(pieceId.toString(), cellX, cellZ, rotation);
        ArsLayoutLogic.PlaceResult placeResult =
                ArsLayoutLogic.canPlace(layout, definitions, piece, ArtronLogic.read(model), instabuild);
        if (placeResult != ArsLayoutLogic.PlaceResult.OK) {
            return Result.refused(placeResult.translationKey());
        }
        ArsPieceDefinition definition = definitions.apply(pieceId);
        Optional<StructureTemplate> template = ArsRoomPlacer.template(world, definition.structure());
        if (template.isEmpty()) {
            LOGGER.error("ARS piece {} has no structure template {}", pieceId, definition.structure());
            return Result.refused(MISSING_STRUCTURE_KEY);
        }
        int cost = instabuild ? 0 : definition.artronCost();
        if (!ArtronLogic.trySpend(model, cost, instabuild)) {
            return Result.refused(ArsLayoutLogic.NOT_ENOUGH_ARTRON_KEY);
        }

        boolean firstPiece = !hasResolvedPieces(layout, definitions);
        // Orphaned pieces whose definition vanished may have been taller than this one: clear full height.
        BoundingBox clearBox = ArsGeometry.pieceBox(piece, definition, ArsGrid.MAX_PIECE_HEIGHT);
        BoundingBox touched = clearBox.inflatedBy(1, 0, 1);
        if (firstPiece) {
            touched = BoundingBox.encapsulating(touched, ArsGeometry.annexBox());
            touched = BoundingBox.encapsulating(touched, ArsGeometry.consoleDoorwayBox());
        }
        ArsRoomPlacer.forceLoad(world, plotOrigin, touched);

        Set<ArsLayoutLogic.Link> linksBefore = ArsLayoutLogic.openLinks(layout, definitions);
        ArsRoomPlacer.clearBox(world, plotOrigin, clearBox);
        if (!ArsRoomPlacer.placePiece(world, plotOrigin, template.get(), piece, null)) {
            LOGGER.error("ARS piece {} template {} placed nothing", pieceId, definition.structure());
            ArtronLogic.refund(model, cost);
            return Result.refused(MISSING_STRUCTURE_KEY);
        }
        if (firstPiece) {
            if (!ArsRoomPlacer.placeAnnex(world, plotOrigin)) {
                LOGGER.error("Missing ARS console annex template {}", ArsGeometry.ANNEX_STRUCTURE_ID);
            }
            ArsRoomPlacer.carveConsoleDoorway(world, plotOrigin);
        }
        layout.add(piece);
        model.setChanged();

        Set<ArsLayoutLogic.Link> opened = new HashSet<>(ArsLayoutLogic.openLinks(layout, definitions));
        opened.removeAll(linksBefore);
        for (ArsLayoutLogic.Link link : opened) {
            ArsRoomPlacer.openDoorway(world, plotOrigin, link);
        }

        ArsRoomPlacer.enableLighting(world, plotOrigin, touched);
        PortalStreamSyncService.markBotiFootprintDirty(tardisId);
        return new Result(true, GROWN_KEY, List.of(Component.translatable(pieceNameKey(pieceId)), cost));
    }

    /**
     * Re-carves the console room's east doorway after the console room template is re-placed, when the
     * TARDIS has at least one resolved ARS piece. Used by the console room placer so a rebuild does not
     * seal the way into the ARS rooms.
     */
    public static void restoreConsoleDoorway(ServerLevel world, BlockPos plotOrigin, @Nullable UUID tardisId) {
        if (world == null || plotOrigin == null || tardisId == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || model.getArsPieces().isEmpty()) {
            return;
        }
        if (hasResolvedPieces(model.getArsPieces(), definitions(world))) {
            ArsRoomPlacer.carveConsoleDoorway(world, plotOrigin);
        }
    }

    /** Piece definitions from the world's synced datapack registry; {@code null} for unknown ids. */
    public static Function<Identifier, ArsPieceDefinition> definitions(ServerLevel world) {
        Optional<Registry<ArsPieceDefinition>> registry = world.registryAccess().lookup(ArsRegistries.PIECE);
        if (registry.isEmpty()) {
            return id -> null;
        }
        Registry<ArsPieceDefinition> pieces = registry.get();
        return pieces::getValue;
    }

    /** Whether the console room has been generated at {@code plotOrigin} (its console is in place). */
    public static boolean isInteriorGenerated(ServerLevel world, BlockPos plotOrigin) {
        return world.getBlockState(plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE))
                .is(DWMBlocks.FIRST_DOCTOR_CONSOLE);
    }

    /** Whether any piece is one {@link ArsLayoutLogic} does not ignore: valid rotation and a known definition. */
    static boolean hasResolvedPieces(List<ArsPlacedPiece> layout, Function<Identifier, ArsPieceDefinition> definitions) {
        for (ArsPlacedPiece piece : layout) {
            if (piece != null && ArsGrid.isValidRotation(piece.rotation)
                    && ArsLayoutLogic.definitionOf(piece, definitions) != null) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Result checkTardis(
            ServerLevel world,
            BlockPos plotOrigin,
            UUID tardisId,
            @Nullable TardisDataModel model
    ) {
        if (model == null || world == null || plotOrigin == null) {
            return Result.refused(NO_TARDIS_KEY);
        }
        if (TardisTravelService.isTraveling(tardisId)) {
            return Result.refused(IN_FLIGHT_KEY);
        }
        if (!isInteriorGenerated(world, plotOrigin)) {
            return Result.refused(NO_INTERIOR_KEY);
        }
        return null;
    }
}
