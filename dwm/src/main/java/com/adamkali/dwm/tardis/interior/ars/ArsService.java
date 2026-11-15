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
import net.minecraft.core.Direction;
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
 * Grows and jettisons Architectural Reconfiguration System pieces in a TARDIS interior: checks the
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
    public static final String PLAYER_INSIDE_KEY = "dwm.ars.player_inside";
    public static final String GROWN_KEY = "dwm.ars.grown";
    public static final String JETTISONED_KEY = "dwm.ars.jettisoned";
    public static final String PIECE_NAME_PREFIX = "dwm.ars.piece.";

    /**
     * Outcome of {@link #grow} or {@link #jettison}. {@code args} are translation arguments for
     * {@code translationKey}: on success the piece name ({@link Component}) and the artron spent or
     * refunded; refusals carry none.
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

    /** Jettisons the piece covering a cell in the TARDIS's plot in {@code dwm:tardis}. */
    public static Result jettison(MinecraftServer server, UUID tardisId, int cellX, int cellZ) {
        ServerLevel world = server == null ? null : server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (world == null || tardisId == null) {
            return Result.refused(NO_INTERIOR_KEY);
        }
        return jettison(world, TardisPlotAllocator.plotOrigin(tardisId), tardisId, cellX, cellZ);
    }

    /**
     * Jettisons the resolved piece covering cell {@code (cellX, cellZ)}. Refuses without changing anything
     * when the TARDIS is unknown or in flight, the console room is not generated, the layout rules refuse
     * (no piece, or others depend on it), or a player is inside the piece (or inside the annex when this is
     * the last piece). Otherwise destroys everything in the piece (blocks, block-entity contents and
     * non-player entities, with no drops), re-seals the neighbours' walls, removes the annex and restores the
     * console wall when no piece is left, and refunds half the cost.
     */
    public static Result jettison(ServerLevel world, BlockPos plotOrigin, UUID tardisId, int cellX, int cellZ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        @Nullable Result refusal = checkTardis(world, plotOrigin, tardisId, model);
        if (refusal != null) {
            return refusal;
        }
        Function<Identifier, ArsPieceDefinition> definitions = definitions(world);
        List<ArsPlacedPiece> layout = model.getArsPieces();
        ArsLayoutLogic.RemoveResult removeResult = ArsLayoutLogic.canRemove(layout, definitions, cellX, cellZ);
        if (removeResult != ArsLayoutLogic.RemoveResult.OK) {
            return Result.refused(removeResult.translationKey());
        }
        ArsPlacedPiece piece = ArsLayoutLogic.pieceAt(layout, definitions, cellX, cellZ).orElseThrow();
        ArsPieceDefinition definition = ArsLayoutLogic.definitionOf(piece, definitions);
        Identifier pieceId = Identifier.parse(piece.pieceId);
        BoundingBox box = ArsGeometry.pieceBox(piece, definition, ArsGrid.MAX_PIECE_HEIGHT);
        boolean lastPiece = resolvedCount(layout, definitions) == 1;
        if (ArsRoomPlacer.hasPlayerInside(world, plotOrigin, box)
                || (lastPiece && ArsRoomPlacer.hasPlayerInside(world, plotOrigin, ArsGeometry.annexBox()))) {
            return Result.refused(PLAYER_INSIDE_KEY);
        }

        BoundingBox touched = box.inflatedBy(1, 0, 1);
        if (lastPiece) {
            touched = BoundingBox.encapsulating(touched, ArsGeometry.annexBox());
            touched = BoundingBox.encapsulating(touched, ArsGeometry.consoleDoorwayBox());
        }
        ArsRoomPlacer.forceLoad(world, plotOrigin, touched);

        Set<ArsLayoutLogic.Link> linksBefore = ArsLayoutLogic.openLinks(layout, definitions);
        Set<ArsCell> removedCells = new HashSet<>(ArsLayoutLogic.occupiedCells(piece, definition));
        ArsRoomPlacer.discardNonPlayerEntities(world, plotOrigin, box);
        ArsRoomPlacer.clearBox(world, plotOrigin, box);
        layout.removeIf(placed -> placed == piece);
        model.setChanged();

        Set<ArsLayoutLogic.Link> closed = new HashSet<>(linksBefore);
        closed.removeAll(ArsLayoutLogic.openLinks(layout, definitions));
        for (ArsLayoutLogic.Link link : closed) {
            sealSurvivingSide(world, plotOrigin, layout, definitions, link, removedCells);
        }
        if (!hasResolvedPieces(layout, definitions)) {
            ArsRoomPlacer.clearAnnex(world, plotOrigin);
            if (!ArsRoomPlacer.restoreConsoleWall(world, plotOrigin)) {
                LOGGER.error("Missing console room template {}; console wall not restored",
                        TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
            }
        }

        int refund = ArsLayoutLogic.refundFor(definition);
        ArtronLogic.refund(model, refund);
        ArsRoomPlacer.enableLighting(world, plotOrigin, touched);
        PortalStreamSyncService.markBotiFootprintDirty(tardisId);
        return new Result(true, JETTISONED_KEY, List.of(Component.translatable(pieceNameKey(pieceId)), refund));
    }

    /**
     * Re-seals the wall layer on the side of {@code link} that survives a jettison, by re-placing the owning
     * piece's template bounded to that layer. The root link's surviving side is the annex, whose doorway is
     * permanently open, so nothing is sealed there.
     */
    private static void sealSurvivingSide(
            ServerLevel world,
            BlockPos plotOrigin,
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions,
            ArsLayoutLogic.Link link,
            Set<ArsCell> removedCells
    ) {
        ArsCell across = link.cell().relative(link.side());
        boolean removedOwnsLinkCell = removedCells.contains(link.cell());
        ArsCell survivor = removedOwnsLinkCell ? across : link.cell();
        Direction survivorSide = removedOwnsLinkCell ? link.side().getOpposite() : link.side();
        Optional<ArsPlacedPiece> owner = ArsLayoutLogic.pieceAt(layout, definitions, survivor.x(), survivor.z());
        if (owner.isEmpty()) {
            return;
        }
        ArsPieceDefinition ownerDefinition = ArsLayoutLogic.definitionOf(owner.get(), definitions);
        Optional<StructureTemplate> template = ArsRoomPlacer.template(world, ownerDefinition.structure());
        if (template.isEmpty()) {
            LOGGER.error("Cannot re-seal ARS wall at {}: missing template {}", survivor, ownerDefinition.structure());
            return;
        }
        ArsRoomPlacer.placePiece(world, plotOrigin, template.get(), owner.get(),
                ArsGeometry.apertureBox(survivor, survivorSide));
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
        return resolvedCount(layout, definitions) > 0;
    }

    /** Number of pieces {@link ArsLayoutLogic} does not ignore. */
    static int resolvedCount(List<ArsPlacedPiece> layout, Function<Identifier, ArsPieceDefinition> definitions) {
        int count = 0;
        for (ArsPlacedPiece piece : layout) {
            if (piece != null && ArsGrid.isValidRotation(piece.rotation)
                    && ArsLayoutLogic.definitionOf(piece, definitions) != null) {
                count++;
            }
        }
        return count;
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
