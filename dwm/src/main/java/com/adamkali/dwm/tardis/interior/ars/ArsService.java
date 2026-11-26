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
import java.util.ArrayList;
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
 * <p>The world-facing methods take an explicit {@code (world, anchor)} so GameTests can work away
 * from the {@code dwm:tardis} dimension; the {@link MinecraftServer} overloads resolve the real interior
 * level and {@link TardisPlotAllocator#plotOrigin}. The anchor locates the console room; every ARS box is
 * grid-local and is placed from the grid origin ({@link ArsGrid#gridOrigin}, the plot's window origin).
 *
 * <p>Each root link ({@link ArsGeometry#ROOT_ENTRANCES}) has its own lobby and console doorway: they are placed
 * and carved when that link opens and removed (lobby cleared, console wall restored) when it closes.
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
     * not generated at {@code anchor}, the layout rules refuse, or the template is missing. Otherwise
     * spends the artron cost (free when {@code instabuild}), places the piece and opens every doorway it now
     * shares. For each root link the piece opens, it also places that side's lobby and carves that side's console
     * doorway.
     */
    public static Result grow(
            ServerLevel world,
            BlockPos anchor,
            UUID tardisId,
            Identifier pieceId,
            int cellX,
            int cellZ,
            int rotation,
            boolean instabuild
    ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        @Nullable Result refusal = checkTardis(world, anchor, tardisId, model);
        if (refusal != null) {
            return refusal;
        }
        BlockPos grid = ArsGrid.gridOrigin(anchor);
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

        Set<ArsLayoutLogic.Link> linksBefore = ArsLayoutLogic.openLinks(layout, definitions);
        List<ArsPlacedPiece> grown = new ArrayList<>(layout);
        grown.add(piece);
        List<ArsGeometry.RootEntrance> opening =
                rootsChanging(linksBefore, ArsLayoutLogic.openLinks(grown, definitions));
        // Orphaned pieces whose definition vanished may have been taller than this one: clear full height.
        BoundingBox clearBox = ArsGeometry.pieceBox(piece, definition, ArsGrid.MAX_PIECE_HEIGHT);
        BoundingBox touched = withRootEntrances(clearBox.inflatedBy(1, 0, 1), opening);
        ArsRoomPlacer.forceLoad(world, grid, touched);

        ArsRoomPlacer.clearBox(world, grid, clearBox);
        if (!ArsRoomPlacer.placePiece(world, grid, template.get(), piece, null)) {
            LOGGER.error("ARS piece {} template {} placed nothing", pieceId, definition.structure());
            ArtronLogic.refund(model, cost);
            return Result.refused(MISSING_STRUCTURE_KEY);
        }
        for (ArsGeometry.RootEntrance entrance : opening) {
            if (!ArsRoomPlacer.placeLobby(world, grid, entrance)) {
                LOGGER.error("Missing ARS console lobby template {}", entrance.lobbyTemplate());
            }
            ArsRoomPlacer.carveConsoleDoorway(world, grid, entrance);
        }
        layout.add(piece);
        model.setChanged();

        Set<ArsLayoutLogic.Link> opened = new HashSet<>(ArsLayoutLogic.openLinks(layout, definitions));
        opened.removeAll(linksBefore);
        for (ArsLayoutLogic.Link link : opened) {
            ArsRoomPlacer.openDoorway(world, grid, link);
        }

        ArsRoomPlacer.enableLighting(world, grid, touched);
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
     * (no piece, or others depend on it), or a player is inside the piece (or inside the lobby of a root link
     * that the jettison would close). Otherwise destroys everything in the piece (blocks, block-entity contents
     * and non-player entities, with no drops), re-seals the neighbours' walls, removes the lobby and restores the
     * console wall of each root link that closes, and refunds half the cost.
     */
    public static Result jettison(ServerLevel world, BlockPos anchor, UUID tardisId, int cellX, int cellZ) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        @Nullable Result refusal = checkTardis(world, anchor, tardisId, model);
        if (refusal != null) {
            return refusal;
        }
        BlockPos grid = ArsGrid.gridOrigin(anchor);
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
        Set<ArsLayoutLogic.Link> linksBefore = ArsLayoutLogic.openLinks(layout, definitions);
        List<ArsPlacedPiece> remaining = new ArrayList<>(layout);
        remaining.removeIf(placed -> placed == piece);
        Set<ArsLayoutLogic.Link> linksAfter = ArsLayoutLogic.openLinks(remaining, definitions);
        List<ArsGeometry.RootEntrance> closing = rootsChanging(linksAfter, linksBefore);
        if (ArsRoomPlacer.hasPlayerInside(world, grid, box)) {
            return Result.refused(PLAYER_INSIDE_KEY);
        }
        for (ArsGeometry.RootEntrance entrance : closing) {
            if (ArsRoomPlacer.hasPlayerInside(world, grid, entrance.lobbyBox())) {
                return Result.refused(PLAYER_INSIDE_KEY);
            }
        }

        BoundingBox touched = withRootEntrances(box.inflatedBy(1, 0, 1), closing);
        ArsRoomPlacer.forceLoad(world, grid, touched);

        Set<ArsCell> removedCells = new HashSet<>(ArsLayoutLogic.occupiedCells(piece, definition));
        ArsRoomPlacer.discardNonPlayerEntities(world, grid, box);
        ArsRoomPlacer.clearBox(world, grid, box);
        layout.removeIf(placed -> placed == piece);
        model.setChanged();

        Set<ArsLayoutLogic.Link> closed = new HashSet<>(linksBefore);
        closed.removeAll(linksAfter);
        for (ArsLayoutLogic.Link link : closed) {
            sealSurvivingSide(world, grid, layout, definitions, link, removedCells);
        }
        for (ArsGeometry.RootEntrance entrance : closing) {
            ArsRoomPlacer.clearLobby(world, grid, entrance);
            if (!ArsRoomPlacer.restoreConsoleWall(world, anchor, grid, entrance)) {
                LOGGER.error("Missing console room template {}; console wall not restored",
                        TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
            }
        }

        int refund = ArsLayoutLogic.refundFor(definition);
        ArtronLogic.refund(model, refund);
        ArsRoomPlacer.enableLighting(world, grid, touched);
        PortalStreamSyncService.markBotiFootprintDirty(tardisId);
        return new Result(true, JETTISONED_KEY, List.of(Component.translatable(pieceNameKey(pieceId)), refund));
    }

    /**
     * Re-seals the wall layer on the side of {@code link} that survives a jettison, by re-placing the owning
     * piece's template bounded to that layer. A root link's surviving side is the console room's reserved cell
     * (its lobby, which is removed separately), so nothing is sealed there.
     */
    private static void sealSurvivingSide(
            ServerLevel world,
            BlockPos grid,
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
        ArsRoomPlacer.placePiece(world, grid, template.get(), owner.get(),
                ArsGeometry.apertureBox(survivor, survivorSide));
    }

    /**
     * Re-carves the console room's doorway on each side whose root link a piece holds open, after the console
     * room template is re-placed at {@code anchor}. Used by the console room placer so a rebuild does not seal
     * the way into the ARS rooms.
     */
    public static void restoreConsoleDoorway(ServerLevel world, BlockPos anchor, @Nullable UUID tardisId) {
        if (world == null || anchor == null || tardisId == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || model.getArsPieces().isEmpty()) {
            return;
        }
        Set<ArsLayoutLogic.Link> open = ArsLayoutLogic.openLinks(model.getArsPieces(), definitions(world));
        BlockPos grid = ArsGrid.gridOrigin(anchor);
        for (ArsGeometry.RootEntrance entrance : ArsGeometry.ROOT_ENTRANCES) {
            if (open.contains(entrance.link())) {
                ArsRoomPlacer.carveConsoleDoorway(world, grid, entrance);
            }
        }
    }

    /** Root entrances whose link is open in {@code after} but not in {@code before}. */
    private static List<ArsGeometry.RootEntrance> rootsChanging(
            Set<ArsLayoutLogic.Link> before,
            Set<ArsLayoutLogic.Link> after
    ) {
        List<ArsGeometry.RootEntrance> changing = new ArrayList<>();
        for (ArsGeometry.RootEntrance entrance : ArsGeometry.ROOT_ENTRANCES) {
            if (!before.contains(entrance.link()) && after.contains(entrance.link())) {
                changing.add(entrance);
            }
        }
        return changing;
    }

    /** {@code box} grown to cover each entrance's lobby and console doorway, clamped to the window. */
    private static BoundingBox withRootEntrances(BoundingBox box, List<ArsGeometry.RootEntrance> entrances) {
        BoundingBox touched = box;
        for (ArsGeometry.RootEntrance entrance : entrances) {
            touched = BoundingBox.encapsulating(touched, entrance.lobbyBox());
            touched = BoundingBox.encapsulating(touched, entrance.consoleDoorwayBox());
        }
        return ArsGeometry.clampToWindow(touched);
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

    /** Whether the console room has been generated at {@code anchor} (its console is in place). */
    public static boolean isInteriorGenerated(ServerLevel world, BlockPos anchor) {
        return world.getBlockState(anchor.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE))
                .is(DWMBlocks.FIRST_DOCTOR_CONSOLE);
    }

    private static @Nullable Result checkTardis(
            ServerLevel world,
            BlockPos anchor,
            UUID tardisId,
            @Nullable TardisDataModel model
    ) {
        if (model == null || world == null || anchor == null) {
            return Result.refused(NO_TARDIS_KEY);
        }
        if (TardisTravelService.isTraveling(tardisId)) {
            return Result.refused(IN_FLIGHT_KEY);
        }
        if (!isInteriorGenerated(world, anchor)) {
            return Result.refused(NO_INTERIOR_KEY);
        }
        return null;
    }
}
