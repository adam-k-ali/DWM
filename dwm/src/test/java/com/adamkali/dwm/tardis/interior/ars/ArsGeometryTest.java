package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArsGeometryTest {
    /** A 2×1 room (the bedroom's footprint) with one connector. */
    private static final ArsPieceDefinition WIDE = new ArsPieceDefinition(
            ArsPieceKind.ROOM, Identifier.fromNamespaceAndPath("dwm", "tardis/ars/bedroom"), 2, 1,
            List.of(new ArsConnector(new ArsCell(0, 0), Direction.NORTH)), 20, 0x7FA8D9);
    /** A 3×2 room so width and depth differ in every rotation check. */
    private static final ArsPieceDefinition ODD = new ArsPieceDefinition(
            ArsPieceKind.ROOM, Identifier.fromNamespaceAndPath("dwm", "tardis/ars/odd"), 3, 2,
            List.of(new ArsConnector(new ArsCell(0, 0), Direction.NORTH)), 20, 0x7FA8D9);

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void rotationMapsToVanillaOrder() {
        assertEquals(Rotation.NONE, ArsGeometry.rotation(0));
        assertEquals(Rotation.CLOCKWISE_90, ArsGeometry.rotation(1));
        assertEquals(Rotation.CLOCKWISE_180, ArsGeometry.rotation(2));
        assertEquals(Rotation.COUNTERCLOCKWISE_90, ArsGeometry.rotation(3));
        assertThrows(IllegalArgumentException.class, () -> ArsGeometry.rotation(4));
        assertThrows(IllegalArgumentException.class, () -> ArsGeometry.rotation(-1));
    }

    @Test
    void pieceBoxCoversRotatedFootprintFromAnchor() {
        // Even rotations keep 2x1 (16 x 8 blocks), odd rotations swap it to 1x2 (8 x 16 blocks).
        assertBox(ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:bedroom", 3, 4, 0), WIDE, 6), 24, 0, 32, 39, 5, 39);
        assertBox(ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:bedroom", 3, 4, 1), WIDE, 6), 24, 0, 32, 31, 5, 47);
        assertBox(ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:bedroom", 3, 4, 2), WIDE, 6), 24, 0, 32, 39, 5, 39);
        assertBox(ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:bedroom", 3, 4, 3), WIDE, 6), 24, 0, 32, 31, 5, 47);
        assertBox(ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:corridor_straight", 2, 1, 1), single(), 16),
                16, 0, 8, 23, 15, 15);
    }

    @Test
    void templatePositionPutsRotatedBoundingBoxOnAnchor() {
        BlockPos anchor = new BlockPos(1000 + 24, 64, -2000 + 8);
        int sizeX = ODD.width() * ArsGrid.CELL_SIZE;
        int sizeZ = ODD.depth() * ArsGrid.CELL_SIZE;
        for (int rotation = 0; rotation < ArsGrid.ROTATION_COUNT; rotation++) {
            BlockPos position = ArsGeometry.templatePosition(anchor, rotation, sizeX, sizeZ);
            Rotation vanilla = ArsGeometry.rotation(rotation);
            // Same transform placeInWorld applies to each template block (zero pivot).
            BlockPos a = StructureTemplate.transform(BlockPos.ZERO, Mirror.NONE, vanilla, BlockPos.ZERO).offset(position);
            BlockPos b = StructureTemplate.transform(new BlockPos(sizeX - 1, 0, sizeZ - 1), Mirror.NONE, vanilla, BlockPos.ZERO)
                    .offset(position);
            BoundingBox placed = BoundingBox.fromCorners(a, b);
            BoundingBox expected = ArsGeometry.toWorld(
                    ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:odd", 0, 0, rotation), ODD, 1), anchor);
            assertBox(placed, expected.minX(), expected.minY(), expected.minZ(), expected.maxX(), expected.maxY(), expected.maxZ());
        }
    }

    @Test
    void apertureBoxIsCellOutermostLayerOffsetsTwoToFive() {
        ArsCell cell = new ArsCell(3, 2);
        assertBox(ArsGeometry.apertureBox(cell, Direction.NORTH), 26, 1, 16, 29, 4, 16);
        assertBox(ArsGeometry.apertureBox(cell, Direction.SOUTH), 26, 1, 23, 29, 4, 23);
        assertBox(ArsGeometry.apertureBox(cell, Direction.WEST), 24, 1, 18, 24, 4, 21);
        assertBox(ArsGeometry.apertureBox(cell, Direction.EAST), 31, 1, 18, 31, 4, 21);
        assertThrows(IllegalArgumentException.class, () -> ArsGeometry.apertureBox(cell, Direction.UP));
    }

    @Test
    void doorwayBoxSpansBothWallLayers() {
        // North link of (3, 2): its own layer z = 16 and the layer of (3, 1) facing it, z = 15.
        assertBox(ArsGeometry.doorwayBox(new ArsLayoutLogic.Link(new ArsCell(3, 2), Direction.NORTH)), 26, 1, 15, 29, 4, 16);
        // West link of (3, 2): x = 24 and the east layer of (2, 2), x = 23.
        assertBox(ArsGeometry.doorwayBox(new ArsLayoutLogic.Link(new ArsCell(3, 2), Direction.WEST)), 23, 1, 18, 24, 4, 21);
    }

    @Test
    void rootDoorwaysMeetTheirLobbies() {
        // East: the piece's west plug on x = 40 and the lobby's open east end on x = 39.
        BoundingBox eastRoot = ArsGeometry.doorwayBox(ArsLayoutLogic.EAST_ROOT_LINK);
        assertBox(eastRoot, 39, 1, 34, 40, 4, 37);
        BoundingBox eastLobby = ArsGeometry.EAST_ROOT.lobbyBox();
        assertBox(eastLobby, 38, 0, 33, 39, 5, 38);
        assertEquals(eastLobby.maxX(), eastRoot.minX());
        assertEquals(new BlockPos(38, 0, 33), ArsGeometry.EAST_ROOT.lobbyOrigin());
        // West: the piece's east plug on x = 23 and the lobby's open west end on x = 24.
        BoundingBox westRoot = ArsGeometry.doorwayBox(ArsLayoutLogic.WEST_ROOT_LINK);
        assertBox(westRoot, 23, 1, 34, 24, 4, 37);
        BoundingBox westLobby = ArsGeometry.WEST_ROOT.lobbyBox();
        assertBox(westLobby, 24, 0, 33, 26, 5, 38);
        assertEquals(westLobby.minX(), westRoot.maxX());
        assertEquals(new BlockPos(24, 0, 33), ArsGeometry.WEST_ROOT.lobbyOrigin());
        // Both lobbies stay inside the console room's reserved cells.
        for (ArsGeometry.RootEntrance entrance : ArsGeometry.ROOT_ENTRANCES) {
            BoundingBox lobby = entrance.lobbyBox();
            assertEquals(true, ArsGrid.isReserved(lobby.minX() / ArsGrid.CELL_SIZE, lobby.minZ() / ArsGrid.CELL_SIZE));
            assertEquals(true, ArsGrid.isReserved(lobby.maxX() / ArsGrid.CELL_SIZE, lobby.maxZ() / ArsGrid.CELL_SIZE));
        }
    }

    @Test
    void consoleDoorwaysAreInConsoleRoomEastAndWestWalls() {
        assertBox(ArsGeometry.EAST_ROOT.consoleDoorwayBox(), 37, 1, 34, 37, 3, 35);
        assertBox(ArsGeometry.WEST_ROOT.consoleDoorwayBox(), 27, 1, 34, 27, 3, 35);
        assertEquals(ArsGrid.CONSOLE_ORIGIN.getX() + FirstDoctorConsoleRoomLayout.SIZE_X - 1, ArsGeometry.CONSOLE_EAST_WALL_X);
        assertEquals(ArsGrid.CONSOLE_ORIGIN.getX(), ArsGeometry.CONSOLE_WEST_WALL_X);
        // Anchor-local z 7..8, both 2 wide and 3 high; along-face offsets 2..3 of root row cz = 4.
        assertEquals(ArsGrid.CONSOLE_ORIGIN.getZ() + 7, ArsGeometry.CONSOLE_DOORWAY_MIN_Z);
        assertEquals(ArsGrid.cellMinBlockZ(4) + 2, ArsGeometry.CONSOLE_DOORWAY_MIN_Z);
        assertEquals(ArsGrid.cellMinBlockZ(4) + 3, ArsGeometry.CONSOLE_DOORWAY_MAX_Z);
    }

    @Test
    void clampToWindowKeepsEdgePiecesInsideTheirWindow() {
        assertEquals(TardisPlotAllocator.PLOT_SPACING, ArsGrid.GRID_SIZE * ArsGrid.CELL_SIZE);
        int min = ArsGrid.windowMinBlock();
        int max = ArsGrid.windowMaxBlock();
        assertEquals(-4968, min);
        assertEquals(5031, max);
        // Cell (MAX, MAX): the 1-block halo around it would reach the next window.
        BoundingBox corner = ArsGeometry.pieceBox(
                        new ArsPlacedPiece("dwm:corridor_cross", ArsGrid.MAX_CELL, ArsGrid.MAX_CELL, 0), single(), 16)
                .inflatedBy(1, 0, 1);
        assertBox(ArsGeometry.clampToWindow(corner), max - 8, 0, max - 8, max, 15, max);
        // Row cz = MIN and a 2x1 piece on the last two columns: the halo would reach z = min - 1 and x = max + 1.
        BoundingBox north = ArsGeometry.pieceBox(
                        new ArsPlacedPiece("dwm:bedroom", ArsGrid.MAX_CELL - 1, ArsGrid.MIN_CELL, 0), WIDE, 16)
                .inflatedBy(1, 0, 1);
        assertBox(north, max - 16, 0, min - 1, max + 1, 15, min + 8);
        assertBox(ArsGeometry.clampToWindow(north), max - 16, 0, min, max, 15, min + 8);
        BlockPos anchor = new BlockPos(TardisPlotAllocator.PLOT_SPACING * 10, 64, TardisPlotAllocator.PLOT_SPACING * -3);
        BlockPos grid = ArsGrid.gridOrigin(anchor);
        BlockPos window = TardisPlotAllocator.windowOrigin(anchor);
        BoundingBox world = ArsGeometry.toWorld(ArsGeometry.clampToWindow(north), grid);
        // The clamped box stays inside this TARDIS's window on every side.
        assertEquals(window.getZ(), world.minZ());
        assertEquals(window.getX() + TardisPlotAllocator.PLOT_SPACING - 1, world.maxX());
        assertTrue(TardisPlotAllocator.isInsideWindow(new BlockPos(world.maxX(), 64, world.minZ()), anchor));
        // Pieces near the console room (and their halos) are unchanged.
        BoundingBox near = ArsGeometry.pieceBox(new ArsPlacedPiece("dwm:bedroom", 6, 0, 0), WIDE, 16).inflatedBy(1, 0, 1);
        assertBox(ArsGeometry.clampToWindow(near), 47, 0, -1, 64, 15, 8);
        assertBox(ArsGeometry.clampToWindow(new BoundingBox(10, 2, 20, 30, 5, 40)), 10, 2, 20, 30, 5, 40);
    }

    @Test
    void toWorldAndChunkBounds() {
        BlockPos origin = new BlockPos(-64, 64, 128);
        BoundingBox world = ArsGeometry.toWorld(new BoundingBox(0, 0, 0, 15, 5, 16), origin);
        assertBox(world, -64, 64, 128, -49, 69, 144);
        assertArrayEquals(new int[]{-4, -4, 8, 9}, ArsGeometry.chunkBounds(world));
        assertArrayEquals(new int[]{-1, 0, 0, 0}, ArsGeometry.chunkBounds(new BoundingBox(-1, 0, 0, 0, 0, 15)));
    }

    private static ArsPieceDefinition single() {
        return new ArsPieceDefinition(ArsPieceKind.CORRIDOR, Identifier.fromNamespaceAndPath("dwm", "tardis/ars/corridor_straight"),
                1, 1, List.of(new ArsConnector(new ArsCell(0, 0), Direction.NORTH)), 5, 0xB8C4D0);
    }

    private static void assertBox(BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        assertEquals(new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ), box, "box " + box);
    }
}
