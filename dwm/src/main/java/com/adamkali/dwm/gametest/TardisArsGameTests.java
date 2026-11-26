package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;

/**
 * Server GameTests for growing and jettisoning ARS pieces. Each test builds a console room at its own
 * TARDIS's plot anchor inside the GameTest level (not {@code dwm:tardis}): a layout spans dozens of blocks,
 * which would overwrite neighbouring tests at a test-relative origin, while distinct TARDIS ids get
 * distinct, far-apart plots. Positions are grid-local (from the plot's window origin) unless named otherwise.
 */
public class TardisArsGameTests {
    private static final String STRUCTURE = "fabric-gametest-api-v1:empty";
    private static final ArsGeometry.RootEntrance EAST = ArsGeometry.EAST_ROOT;
    private static final ArsGeometry.RootEntrance WEST = ArsGeometry.WEST_ROOT;
    /** Root cells: (5, 4) east of the console room and (2, 4) west of it. */
    private static final int EAST_X = 5;
    private static final int WEST_X = 2;
    private static final int ROOT_Z = 4;

    /** One TARDIS with its console room placed at its plot anchor in the GameTest level. */
    private record Plot(ServerLevel level, BlockPos origin, BlockPos grid, UUID id, TardisDataModel model) {
        /** The block at a grid-local position. */
        BlockState at(int x, int y, int z) {
            return level.getBlockState(grid.offset(x, y, z));
        }

        /** World position of an anchor-local position (console room layout constants). */
        BlockPos anchorLocal(BlockPos local) {
            return origin.offset(local);
        }
    }

    // --- Growing -------------------------------------------------------------------------------------

    @GameTest(structure = STRUCTURE)
    public void grow_EastRootCorridorPlacesLobbyAndOpensDoorways(GameTestHelper context) {
        Plot plot = plot(context);
        int before = ArtronLogic.read(plot.model());
        assertSolid(plot, EAST.consoleDoorwayBox(), "console east wall before the root link opens");
        Map<BlockPos, BlockState> westSide = snapshot(plot, BoundingBox.encapsulating(WEST.lobbyBox(), WEST.consoleDoorwayBox()));

        ArsService.Result result = grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);

        assertAir(plot, EAST.consoleDoorwayBox(), "console east doorway");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK), "east root doorway");
        // Lobby floor and side walls around its passage.
        assertSolid(plot, floor(EAST.lobbyBox()), "east lobby floor");
        assertSolid(plot, new BoundingBox(38, 1, 33, 39, 4, 33), "east lobby north wall");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(EAST_X, ROOT_Z), Direction.EAST), "unlinked east plug");
        assertUnchanged(plot, westSide, "west side while only the east root opens");
        check(plot.model().getArsPieces().equals(List.of(new ArsPlacedPiece("dwm:corridor_straight", EAST_X, ROOT_Z, 1))),
                "model layout " + plot.model().getArsPieces());
        check(ArtronLogic.read(plot.model()) == before - 5, "artron spent: " + ArtronLogic.read(plot.model()));
        check(result.args().get(1).equals(5), "result cost arg " + result.args());
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_WestRootCorridorPlacesWestLobbyOnly(GameTestHelper context) {
        Plot plot = plot(context);
        assertSolid(plot, WEST.consoleDoorwayBox(), "console west wall (ash doors) before the west root opens");
        Map<BlockPos, BlockState> eastSide = snapshot(plot, BoundingBox.encapsulating(EAST.lobbyBox(), EAST.consoleDoorwayBox()));

        // A straight corridor turned east-west on (2, 4): its east connector faces the console room.
        grow(plot, "corridor_straight", WEST_X, ROOT_Z, 1);

        assertAir(plot, WEST.consoleDoorwayBox(), "console west doorway");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.WEST_ROOT_LINK), "west root doorway");
        assertAir(plot, new BoundingBox(24, 1, 34, 26, 4, 37), "west lobby passage");
        assertSolid(plot, floor(WEST.lobbyBox()), "west lobby floor");
        assertSolid(plot, new BoundingBox(24, 1, 38, 26, 4, 38), "west lobby south wall");
        assertSolid(plot, new BoundingBox(24, 5, 33, 26, 5, 38), "west lobby ceiling");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(WEST_X, ROOT_Z), Direction.WEST), "unlinked west plug");
        assertUnchanged(plot, eastSide, "east side while only the west root opens");
        check(ArsLayoutLogic.openLinks(plot.model().getArsPieces(), ArsService.definitions(plot.level()))
                .equals(java.util.Set.of(ArsLayoutLogic.WEST_ROOT_LINK)), "only the west root is open");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_LinkedPieceOpensBothWallLayersOnly(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_t", EAST_X, ROOT_Z, 0);
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(EAST_X, ROOT_Z), Direction.EAST), "T east plug before link");

        grow(plot, "corridor_straight", 6, ROOT_Z, 1);

        ArsLayoutLogic.Link shared = new ArsLayoutLogic.Link(new ArsCell(6, ROOT_Z), Direction.WEST);
        assertAir(plot, ArsGeometry.doorwayBox(shared), "shared doorway, both layers");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(EAST_X, ROOT_Z), Direction.NORTH), "T north plug (no neighbour)");
        assertSolid(plot, ArsGeometry.apertureBox(new ArsCell(6, ROOT_Z), Direction.EAST), "straight east plug (no neighbour)");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RefusedChangesNothing(GameTestHelper context) {
        Plot plot = plot(context);
        BoundingBox island = new BoundingBox(56, 0, 56, 63, 7, 63);
        BoundingBox root = BoundingBox.encapsulating(EAST.lobbyBox(),
                ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:corridor_straight", EAST_X, ROOT_Z, 1),
                        definition(plot, "corridor_straight"), ArsGrid.MAX_PIECE_HEIGHT));
        Map<BlockPos, BlockState> islandBefore = snapshot(plot, island);
        Map<BlockPos, BlockState> rootBefore = snapshot(plot, root);
        Map<BlockPos, BlockState> wallBefore = snapshot(plot, EAST.consoleDoorwayBox());

        // Not connected to the console room.
        ArsService.Result notConnected = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                piece("corridor_cross"), 7, 7, 0, false);
        check(!notConnected.success() && ArsLayoutLogic.NOT_CONNECTED_KEY.equals(notConnected.translationKey()),
                "expected not connected, got " + notConnected);
        check(ArtronLogic.read(plot.model()) == ArtronLogic.CAPACITY, "artron changed on refusal");

        // Connected but unaffordable.
        plot.model().artron = 2;
        ArsService.Result poor = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                piece("corridor_straight"), EAST_X, ROOT_Z, 1, false);
        check(!poor.success() && ArsLayoutLogic.NOT_ENOUGH_ARTRON_KEY.equals(poor.translationKey()),
                "expected not enough artron, got " + poor);
        check(ArtronLogic.read(plot.model()) == 2, "artron changed on refusal: " + ArtronLogic.read(plot.model()));

        // Unknown piece.
        ArsService.Result unknown = ArsService.grow(plot.level(), plot.origin(), plot.id(),
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "no_such_piece"), EAST_X, ROOT_Z, 1, true);
        check(!unknown.success() && ArsLayoutLogic.UNKNOWN_PIECE_KEY.equals(unknown.translationKey()),
                "expected unknown piece, got " + unknown);

        check(plot.model().getArsPieces().isEmpty(), "refused grow added to the model");
        assertUnchanged(plot, islandBefore, "unconnected cell");
        assertUnchanged(plot, rootBefore, "root cell and lobby");
        assertUnchanged(plot, wallBefore, "console wall");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RefusedWithoutConsoleRoom(GameTestHelper context) {
        redirectSaves(context);
        TardisDataModel model = TardisDataLoader.create();
        BlockPos origin = TardisPlotAllocator.plotOrigin(model.uuid);
        ArsService.Result result = ArsService.grow(context.getLevel(), origin, model.uuid,
                piece("corridor_straight"), EAST_X, ROOT_Z, 1, true);
        check(!result.success() && ArsService.NO_INTERIOR_KEY.equals(result.translationKey()),
                "expected no interior, got " + result);
        check(model.getArsPieces().isEmpty(), "model changed");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RotatedCornerOpensExpectedSides(GameTestHelper context) {
        Plot plot = plot(context);
        ArsCell root = new ArsCell(EAST_X, ROOT_Z);
        // Corner connectors north + east, turned 3 times clockwise: west (root) + north.
        grow(plot, "corridor_corner", EAST_X, ROOT_Z, 3);
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK), "root doorway");
        // No connector east or south: corridor walls close those sides (the outer layer may be structure void).
        assertClosedSide(plot, root, Direction.EAST, "corner east side");
        assertClosedSide(plot, root, Direction.SOUTH, "corner south side");
        // Its north plug is a closed doorway until something mates it: air directly inside.
        assertSolid(plot, ArsGeometry.apertureBox(root, Direction.NORTH), "corner north plug");
        assertAir(plot, ArsGeometry.apertureBox(root, Direction.NORTH).moved(0, 0, 1), "inside north plug");

        grow(plot, "corridor_straight", EAST_X, ROOT_Z - 1, 0);
        assertAir(plot, ArsGeometry.doorwayBox(new ArsLayoutLogic.Link(root, Direction.NORTH)), "north doorway");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void grow_RotatedBedroomOccupiesRotatedFootprint(GameTestHelper context) {
        Plot plot = plot(context);
        // 2x1 bedroom turned 3 times: 1x2 at cells (5, 3) and (5, 4), its north door now west of (5, 4).
        grow(plot, "bedroom", EAST_X, 3, 3);
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK), "root doorway");
        assertSolid(plot, new BoundingBox(40, 0, 24, 47, 0, 39), "bedroom floor over the rotated footprint");
        assertSolid(plot, new BoundingBox(40, 5, 24, 47, 5, 39), "bedroom ceiling over the rotated footprint");
        assertAir(plot, new BoundingBox(40, 6, 24, 47, 6, 39), "above the bedroom");
        assertAir(plot, new BoundingBox(48, 0, 24, 48, 6, 39), "east of the footprint");
        assertAir(plot, new BoundingBox(40, 0, 40, 47, 6, 40), "south of the footprint");
        assertAir(plot, new BoundingBox(40, 0, 23, 47, 6, 23), "north of the footprint");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 200)
    public void grow_CorridorIsLit(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        BlockPos inside = plot.grid().offset(43, 2, 35);
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
        Map<BlockPos, BlockState> westWall = snapshot(plot, WEST.consoleDoorwayBox());
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        if (TardisInteriorService.regenerateInterior(plot.level(), plot.origin(), plot.id()) == null) {
            throw new AssertionError("regenerateInterior returned null");
        }
        assertAir(plot, EAST.consoleDoorwayBox(), "console doorway after rebuild");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK), "root doorway after rebuild");
        assertSolid(plot, floor(EAST.lobbyBox()), "lobby floor after rebuild");
        assertUnchanged(plot, westWall, "closed west wall after rebuild");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void regenerateInterior_RecarvesBothOpenDoorways(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        grow(plot, "corridor_straight", WEST_X, ROOT_Z, 1);
        if (TardisInteriorService.regenerateInterior(plot.level(), plot.origin(), plot.id()) == null) {
            throw new AssertionError("regenerateInterior returned null");
        }
        for (ArsGeometry.RootEntrance entrance : ArsGeometry.ROOT_ENTRANCES) {
            assertAir(plot, entrance.consoleDoorwayBox(), entrance.link() + " console doorway after rebuild");
            assertAir(plot, ArsGeometry.doorwayBox(entrance.link()), entrance.link() + " root doorway after rebuild");
            assertSolid(plot, floor(entrance.lobbyBox()), entrance.link() + " lobby floor after rebuild");
        }
        context.succeed();
    }

    // --- Jettisoning ----------------------------------------------------------------------------------

    @GameTest(structure = STRUCTURE)
    public void jettison_LeafClearsBoxResealsNeighbourAndRefundsHalf(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_t", EAST_X, ROOT_Z, 0);
        BoundingBox tEast = ArsGeometry.apertureBox(new ArsCell(EAST_X, ROOT_Z), Direction.EAST);
        Map<BlockPos, BlockState> tEastBefore = snapshot(plot, tEast);
        // 2x1 bedroom turned 3 times: cells (6, 3) and (6, 4), door west of (6, 4) facing the T's east plug.
        grow(plot, "bedroom", 6, 3, 3);
        assertAir(plot, ArsGeometry.doorwayBox(new ArsLayoutLogic.Link(new ArsCell(6, ROOT_Z), Direction.WEST)), "bedroom doorway");
        BoundingBox bedroom = new BoundingBox(48, 0, 24, 55, ArsGrid.MAX_PIECE_HEIGHT - 1, 39);
        int stocked = stockContainers(plot, bedroom);
        check(stocked > 0, "expected the bedroom to contain a container");
        int artronBefore = ArtronLogic.read(plot.model());

        ArsService.Result result = ArsService.jettison(plot.level(), plot.origin(), plot.id(), 6, ROOT_Z);

        check(result.success(), "jettison refused: " + result.translationKey());
        assertAir(plot, bedroom, "jettisoned bedroom");
        assertUnchanged(plot, tEastBefore, "T east wall re-sealed");
        check(ArtronLogic.read(plot.model()) == artronBefore + 10, "refund: " + ArtronLogic.read(plot.model()));
        check(result.args().get(1).equals(10), "result refund arg " + result.args());
        check(plot.model().getArsPieces().equals(List.of(new ArsPlacedPiece("dwm:corridor_t", EAST_X, ROOT_Z, 0))),
                "model layout " + plot.model().getArsPieces());
        List<ItemEntity> items = plot.level().getEntitiesOfClass(ItemEntity.class,
                AABB.of(ArsGeometry.toWorld(bedroom, plot.grid())).inflate(4.0));
        check(items.isEmpty(), "jettison dropped items: " + items);
        // The console room is still connected.
        assertAir(plot, EAST.consoleDoorwayBox(), "console doorway");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_RefusedWhenOthersDependOnPiece(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_t", EAST_X, ROOT_Z, 0);
        grow(plot, "corridor_straight", 6, ROOT_Z, 1);
        BoundingBox both = new BoundingBox(40, 0, 32, 55, ArsGrid.MAX_PIECE_HEIGHT - 1, 39);
        Map<BlockPos, BlockState> before = snapshot(plot, both);
        int artron = ArtronLogic.read(plot.model());

        ArsService.Result result = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);

        check(!result.success() && ArsLayoutLogic.WOULD_DISCONNECT_KEY.equals(result.translationKey()),
                "expected would disconnect, got " + result);
        assertUnchanged(plot, before, "refused jettison");
        check(plot.model().getArsPieces().size() == 2, "model changed: " + plot.model().getArsPieces());
        check(ArtronLogic.read(plot.model()) == artron, "artron changed");
        ArsService.Result empty = ArsService.jettison(plot.level(), plot.origin(), plot.id(), 7, 7);
        check(!empty.success() && ArsLayoutLogic.NO_PIECE_KEY.equals(empty.translationKey()), "expected no piece, got " + empty);
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_LastPieceRemovesLobbyAndRestoresConsoleWall(GameTestHelper context) {
        Plot plot = plot(context);
        // Right after placement the doorway box holds exactly the console room template's blocks.
        Map<BlockPos, BlockState> consoleWall = snapshot(plot, EAST.consoleDoorwayBox());
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        assertAir(plot, EAST.consoleDoorwayBox(), "console doorway while a piece exists");

        ArsService.Result result = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);

        check(result.success(), "jettison refused: " + result.translationKey());
        assertUnchanged(plot, consoleWall, "console east wall");
        assertAir(plot, EAST.lobbyBox(), "east lobby");
        assertAir(plot, new BoundingBox(40, 0, 32, 47, ArsGrid.MAX_PIECE_HEIGHT - 1, 39), "root cell");
        check(plot.model().getArsPieces().isEmpty(), "model not empty");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_WestPieceRestoresAshDoorsAndKeepsEastOpen(GameTestHelper context) {
        Plot plot = plot(context);
        // The console room template's west wall: two decorative ash doors with wall above.
        Map<BlockPos, BlockState> westWall = snapshot(plot, WEST.consoleDoorwayBox());
        long doors = westWall.values().stream().filter(state -> state.is(DWMBlocks.ASH_DOOR)).count();
        check(doors == 4, "expected both halves of two ash doors in the west doorway, found " + doors);
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        grow(plot, "corridor_straight", WEST_X, ROOT_Z, 1);
        assertAir(plot, WEST.consoleDoorwayBox(), "west doorway while the west piece exists");

        ArsService.Result result = ArsService.jettison(plot.level(), plot.origin(), plot.id(), WEST_X, ROOT_Z);

        check(result.success(), "jettison refused: " + result.translationKey());
        assertUnchanged(plot, westWall, "ash doors restored from the console room template");
        assertAir(plot, WEST.lobbyBox(), "west lobby");
        assertAir(plot, new BoundingBox(16, 0, 32, 23, ArsGrid.MAX_PIECE_HEIGHT - 1, 39), "west root cell");
        // The east side is untouched.
        assertAir(plot, EAST.consoleDoorwayBox(), "east doorway");
        assertAir(plot, ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK), "east root doorway");
        assertSolid(plot, floor(EAST.lobbyBox()), "east lobby floor");
        check(plot.model().getArsPieces().equals(List.of(new ArsPlacedPiece("dwm:corridor_straight", EAST_X, ROOT_Z, 1))),
                "model layout " + plot.model().getArsPieces());
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_RootPieceWithOnlyIslandsLeftRemovesLobby(GameTestHelper context) {
        Plot plot = plot(context);
        Map<BlockPos, BlockState> consoleWall = snapshot(plot, EAST.consoleDoorwayBox());
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        // A resolved piece linked to nothing (as left behind when a piece between them loses its definition).
        ArsPlacedPiece island = new ArsPlacedPiece("dwm:corridor_dead_end", 7, 7, 0);
        plot.model().getArsPieces().add(island);

        ArsService.Result result = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);

        check(result.success(), "jettison refused: " + result.translationKey());
        check(plot.model().getArsPieces().equals(List.of(island)), "model layout " + plot.model().getArsPieces());
        assertAir(plot, EAST.lobbyBox(), "lobby once the root link closes");
        assertUnchanged(plot, consoleWall, "console east wall once the root link closes");

        if (TardisInteriorService.regenerateInterior(plot.level(), plot.origin(), plot.id()) == null) {
            throw new AssertionError("regenerateInterior returned null");
        }
        assertUnchanged(plot, consoleWall, "console east wall after rebuild with only an island");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_RefusedWhilePlayerInside(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        try {
            snapTo(player, plot.grid().offset(43, 1, 35));
            ArsService.Result inRoom = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);
            check(!inRoom.success() && ArsService.PLAYER_INSIDE_KEY.equals(inRoom.translationKey()),
                    "expected player inside (room), got " + inRoom);

            // The piece on the root link also takes the lobby with it.
            snapTo(player, plot.grid().offset(38, 1, 35));
            ArsService.Result inLobby = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);
            check(!inLobby.success() && ArsService.PLAYER_INSIDE_KEY.equals(inLobby.translationKey()),
                    "expected player inside (lobby), got " + inLobby);
            check(plot.model().getArsPieces().size() == 1, "model changed while refused");
            assertAir(plot, EAST.consoleDoorwayBox(), "console doorway while refused");

            snapTo(player, plot.anchorLocal(FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE));
            ArsService.Result away = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);
            check(away.success(), "jettison with the player in the console room refused: " + away.translationKey());
        } finally {
            plot.level().getServer().getPlayerList().remove(player);
        }
        context.succeed();
    }

    @GameTest(structure = STRUCTURE)
    public void jettison_PlayerInWestLobbyBlocksOnlyTheWestSide(GameTestHelper context) {
        Plot plot = plot(context);
        grow(plot, "corridor_straight", EAST_X, ROOT_Z, 1);
        grow(plot, "corridor_straight", WEST_X, ROOT_Z, 1);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        try {
            snapTo(player, plot.grid().offset(25, 1, 35));
            ArsService.Result west = ArsService.jettison(plot.level(), plot.origin(), plot.id(), WEST_X, ROOT_Z);
            check(!west.success() && ArsService.PLAYER_INSIDE_KEY.equals(west.translationKey()),
                    "expected player inside (west lobby), got " + west);
            assertAir(plot, WEST.consoleDoorwayBox(), "west doorway while refused");
            check(plot.model().getArsPieces().size() == 2, "model changed while refused");

            // The east side's last piece does not close the west lobby, so the player there does not block it.
            ArsService.Result east = ArsService.jettison(plot.level(), plot.origin(), plot.id(), EAST_X, ROOT_Z);
            check(east.success(), "east jettison refused by a player in the west lobby: " + east.translationKey());
            assertAir(plot, EAST.lobbyBox(), "east lobby");
            assertAir(plot, WEST.consoleDoorwayBox(), "west doorway after the east jettison");

            snapTo(player, plot.anchorLocal(FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE));
            ArsService.Result away = ArsService.jettison(plot.level(), plot.origin(), plot.id(), WEST_X, ROOT_Z);
            check(away.success(), "west jettison with the player in the console room refused: " + away.translationKey());
        } finally {
            plot.level().getServer().getPlayerList().remove(player);
        }
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
        return new Plot(context.getLevel(), origin, ArsGrid.gridOrigin(origin), model.uuid, model);
    }

    private static void snapTo(ServerPlayer player, BlockPos feet) {
        player.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
    }

    /** The bottom layer of a box. */
    private static BoundingBox floor(BoundingBox box) {
        return new BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.minY(), box.maxZ());
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
                throw new AssertionError(what + ": grid-local " + pos + " was " + entry.getValue() + ", now " + now);
            }
        }
    }

    static void assertAir(Plot plot, BoundingBox local, String what) {
        BlockPos.betweenClosedStream(local).forEach(pos -> {
            BlockState state = plot.at(pos.getX(), pos.getY(), pos.getZ());
            if (!state.isAir()) {
                throw new AssertionError(what + ": expected air at grid-local " + pos.immutable() + ", found " + state);
            }
        });
    }

    static void assertSolid(Plot plot, BoundingBox local, String what) {
        BlockPos.betweenClosedStream(local).forEach(pos -> {
            BlockState state = plot.at(pos.getX(), pos.getY(), pos.getZ());
            if (state.isAir()) {
                throw new AssertionError(what + ": expected a block at grid-local " + pos.immutable());
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
                throw new AssertionError(what + ": open through grid-local " + pos.immutable());
            }
        });
    }

    /** Puts a stack in the first slot of every container in the box; returns how many were stocked. */
    private static int stockContainers(Plot plot, BoundingBox local) {
        int[] stocked = {0};
        BlockPos.betweenClosedStream(ArsGeometry.toWorld(local, plot.grid())).forEach(pos -> {
            if (plot.level().getBlockEntity(pos) instanceof Container container && container.getContainerSize() > 0) {
                container.setItem(0, new ItemStack(Items.DIAMOND, 3));
                stocked[0]++;
            }
        });
        return stocked[0];
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
