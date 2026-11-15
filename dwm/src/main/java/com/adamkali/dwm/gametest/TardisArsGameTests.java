package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsGeometry;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import com.adamkali.dwm.tardis.interior.ars.ArsService;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Server GameTests for growing and jettisoning ARS pieces. Each test builds a console room at its own
 * TARDIS's plot origin inside the GameTest level (not {@code dwm:tardis}): a layout spans dozens of blocks,
 * which would overwrite neighbouring tests at a test-relative origin, while distinct TARDIS ids get
 * distinct, far-apart plots.
 */
public class TardisArsGameTests {
    private static final String STRUCTURE = "fabric-gametest-api-v1:empty";

    /** One TARDIS with its console room placed at its plot origin in the GameTest level. */
    private record Plot(ServerLevel level, BlockPos origin, UUID id, TardisDataModel model) {
        BlockState at(int x, int y, int z) {
            return level.getBlockState(origin.offset(x, y, z));
        }
    }

    // --- Growing -------------------------------------------------------------------------------------

    @GameTest(structure = STRUCTURE)
    public void grow_RootCorridorPlacesAnnexAndOpensDoorways(GameTestHelper context) {
        Plot plot = plot(context);
        int before = ArtronLogic.read(plot.model());
        assertSolid(plot, ArsGeometry.consoleDoorwayBox(), "console east wall before the first piece");

        ArsService.Result result = grow(plot, "corridor_straight", 2, 1, 1);

        assertAir(plot, ArsGeometry.consoleDoorwayBox(), "console doorway");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.ROOT_LINK), "root doorway");
        // Annex floor and the walls around the doorway.
        assertSolid(plot, new BoundingBox(11, 0, 6, 15, 0, 14), "annex floor");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(2, 1), Direction.EAST), "unlinked east plug");
        check(plot.model().getArsPieces().equals(List.of(new ArsPlacedPiece("dwm:corridor_straight", 2, 1, 1))),
                "model layout " + plot.model().getArsPieces());
        check(ArtronLogic.read(plot.model()) == before - 5, "artron spent: " + ArtronLogic.read(plot.model()));
        check(result.args().get(1).equals(5), "result cost arg " + result.args());
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_LinkedPieceOpensBothWallLayersOnly(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_t", 2, 1, 0);
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(2, 1), Direction.EAST), "T east plug before link");

        grow(plot, "corridor_straight", 3, 1, 1);

        ArsLayoutLogic.Link shared = new ArsLayoutLogic.Link(new ArsCell(3, 1), Direction.WEST);
        assertAir(plot, ArsGeometry.doorwayBox(shared), "shared doorway, both layers");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(2, 1), Direction.NORTH), "T north plug (no neighbour)");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(3, 1), Direction.EAST), "straight east plug (no neighbour)");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RefusedChangesNothing(GameTestHelper context) {
        Plot plot = plot(context);
        BoundingBox island = new BoundingBox(40, 0, 40, 47, 7, 47);
        BoundingBox root = BoundingBox.encapsulating(ArsGeometry.annexBox(),
                ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:corridor_straight", 2, 1, 1),
                        definition(plot, "corridor_straight"), ArsGrid.MAX_PIECE_HEIGHT));
        Map<BlockPos, BlockState> islandBefore = snapshot(plot, island);
        Map<BlockPos, BlockState> rootBefore = snapshot(plot, root);
        Map<BlockPos, BlockState> wallBefore = snapshot(plot, ArsGeometry.consoleDoorwayBox());

        // Not connected to the console room.
        ArsService.Result notConnected = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                piece("corridor_cross"), 5, 5, 0, false);
        check(!notConnected.success() && ArsLayoutLogic.NOT_CONNECTED_KEY.equals(notConnected.translationKey()),
                "expected not connected, got " + notConnected);
        check(ArtronLogic.read(plot.model()) == ArtronLogic.CAPACITY, "artron changed on refusal");

        // Connected but unaffordable.
        plot.model().artron = 2;
        ArsService.Result poor = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                piece("corridor_straight"), 2, 1, 1, false);
        check(!poor.success() && ArsLayoutLogic.NOT_ENOUGH_ARTRON_KEY.equals(poor.translationKey()),
                "expected not enough artron, got " + poor);
        check(ArtronLogic.read(plot.model()) == 2, "artron changed on refusal: " + ArtronLogic.read(plot.model()));

        // Unknown piece.
        ArsService.Result unknown = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "no_such_piece"), 2, 1, 1, true);
        check(!unknown.success() && ArsLayoutLogic.UNKNOWN_PIECE_KEY.equals(unknown.translationKey()),
                "expected unknown piece, got " + unknown);

        check(plot.model().getArsPieces().isEmpty(), "refused grow added to the model");
        assertUnchanged(plot, islandBefore, "unconnected cell");
        assertUnchanged(plot, rootBefore, "root cell and annex");
        assertUnchanged(plot, wallBefore, "console wall");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RefusedWithoutConsoleRoom(GameTestHelper context) {
        redirectSaves(context);
        TardisDataModel model = TardisDataLoader.create();
        BlockPos origin = TardisPlotAllocator.plotOrigin(model.uuid);
        ArsService.Result result = ArsService.grow(context.getLevel(), origin, model.uuid,
                piece("corridor_straight"), 2, 1, 1, true);
        check(!result.success() && ArsService.NO_INTERIOR_KEY.equals(result.translationKey()),
                "expected no interior, got " + result);
        check(model.getArsPieces().isEmpty(), "model changed");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RotatedCornerOpensExpectedSides(GameTestHelper context) {
        Plot plot = plot(context);
        // Corner connectors north + east, turned 3 times clockwise: west (root) + north.
        grow(plot, "corridor_corner", 2, 1, 3);
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.ROOT_LINK), "root doorway");
        // No connector east or south: corridor walls close those sides (the outer layer may be structure void).
        assertClosedSide(plot, new ArsCell(2, 1), Direction.EAST, "corner east side");
        assertClosedSide(plot, new ArsCell(2, 1), Direction.SOUTH, "corner south side");
        // Its north plug is a closed doorway until something mates it: air directly inside.
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(2, 1), Direction.NORTH), "corner north plug");
        assertAir(plot, ArsGeometry.apertureBox(new ArsCell(2, 1), Direction.NORTH).moved(0, 0, 1), "inside north plug");

        grow(plot, "corridor_straight", 2, 0, 0);
        assertAir(plot, ArsGeometry.doorwayBox(new ArsLayoutLogic.Link(new ArsCell(2, 1), Direction.NORTH)), "north doorway");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RotatedBedroomOccupiesRotatedFootprint(GameTestHelper context) {
        Plot plot = plot(context);
        // 2x1 bedroom turned 3 times: 1x2 at cells (2, 0) and (2, 1), its north door now west of (2, 1).
        grow(plot, "bedroom", 2, 0, 3);
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.ROOT_LINK), "root doorway");
        assertSolid(plot, new BoundingBox(16, 0, 0, 23, 0, 15), "bedroom floor over the rotated footprint");
        assertSolid(plot, new BoundingBox(16, 5, 0, 23, 5, 15), "bedroom ceiling over the rotated footprint");
        assertAir(plot, new BoundingBox(16, 6, 0, 23, 6, 15), "above the bedroom");
        assertAir(plot, new BoundingBox(24, 0, 0, 24, 6, 15), "east of the footprint");
        assertAir(plot, new BoundingBox(16, 0, 16, 23, 6, 16), "south of the footprint");
        assertAir(plot, new BoundingBox(16, 0, -1, 23, 6, -1), "north of the footprint");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 200)
    public void grow_CorridorIsLit(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", 2, 1, 1);
        BlockPos inside = plot.origin().offset(19, 2, 11);
        if (!plot.level().getBlockState(inside).isAir()) {
            throw new AssertionError("Expected corridor air at " + inside);
        }
        context.succeedWhen(() -> {
            int light = plot.level().getBrightness(LightLayer.BLOCK, inside);
            if (light <= 0) {
                throw context.assertionException(Component.literal("No block light in grown corridor at " + inside));
            }
        });
    }

    @GameTest(structure = STRUCTURE)
    public void regenerateInterior_KeepsConsoleDoorwayOpen(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", 2, 1, 1);
        if (TardisInteriorService.regenerateInterior(plot.level(), plot.origin(), plot.id()) == null) {
            throw new AssertionError("regenerateInterior returned null");
        }
        assertAir(plot, ArsGeometry.consoleDoorwayBox(), "console doorway after rebuild");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.ROOT_LINK), "root doorway after rebuild");
        assertSolid(plot, new BoundingBox(11, 0, 6, 15, 0, 14), "annex floor after rebuild");
        context.succeed();
    }

    // --- Helpers -------------------------------------------------------------------------------------

    static void redirectSaves(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
    }

    private static Plot plot(GameTestHelper context) {
        redirectSaves(context);
        TardisDataModel model = TardisDataLoader.create();
        BlockPos origin = TardisPlotAllocator.plotOrigin(model.uuid);
        FirstDoctorConsoleRoomPlacer.place(context.getLevel(), origin, model.uuid);
        return new Plot(context.getLevel(), origin, model.uuid, model);
    }

    private static Identifier piece(String name) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, name);
    }

    private static ArsPieceDefinition definition(Plot plot, String name) {
        ArsPieceDefinition definition = ArsService.definitions(plot.level()).apply(piece(name));
        if (definition == null) {
            throw new AssertionError("Missing ARS piece definition dwm:" + name);
        }
        return definition;
    }

    private static ArsService.Result grow(Plot plot, String name, int cellX, int cellZ, int rotation) {
        ArsService.Result result = ArsService.grow(plot.level(), plot.origin(), plot.id(), piece(name), cellX, cellZ, rotation, false);
        if (!result.success()) {
            throw new AssertionError("Grow " + name + " at (" + cellX + ", " + cellZ + ") r" + rotation
                    + " refused: " + result.translationKey());
        }
        return result;
    }

    static Map<BlockPos, BlockState> snapshot(Plot plot, BoundingBox local) {
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        BlockPos.betweenClosedStream(local).forEach(pos -> states.put(pos.immutable(), plot.at(pos.getX(), pos.getY(), pos.getZ())));
        return states;
    }

    static void assertUnchanged(Plot plot, Map<BlockPos, BlockState> before, String what) {
        for (var entry : before.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState now = plot.at(pos.getX(), pos.getY(), pos.getZ());
            if (now != entry.getValue()) {
                throw new AssertionError(what + ": plot-local " + pos + " was " + entry.getValue() + ", now " + now);
            }
        }
    }

    static void assertAir(Plot plot, BoundingBox local, String what) {
        BlockPos.betweenClosedStream(local).forEach(pos -> {
            BlockState state = plot.at(pos.getX(), pos.getY(), pos.getZ());
            if (!state.isAir()) {
                throw new AssertionError(what + ": expected air at plot-local " + pos.immutable() + ", found " + state);
            }
        });
    }

    static void assertSolid(Plot plot, BoundingBox local, String what) {
        BlockPos.betweenClosedStream(local).forEach(pos -> {
            BlockState state = plot.at(pos.getX(), pos.getY(), pos.getZ());
            if (state.isAir()) {
                throw new AssertionError(what + ": expected a block at plot-local " + pos.immutable());
            }
        });
    }

    /**
     * No way out of {@code cell} across {@code side} in the aperture span: each line of blocks running from the
     * cell's second layer to its outermost layer on that side holds a block.
     */
    static void assertClosedSide(Plot plot, ArsCell cell, Direction side, String what) {
        BlockPos.betweenClosedStream(ArsGeometry.apertureBox(cell, side)).forEach(pos -> {
            BlockPos in = pos.relative(side.getOpposite());
            if (plot.at(pos.getX(), pos.getY(), pos.getZ()).isAir() && plot.at(in.getX(), in.getY(), in.getZ()).isAir()) {
                throw new AssertionError(what + ": open through plot-local " + pos.immutable());
            }
        });
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
