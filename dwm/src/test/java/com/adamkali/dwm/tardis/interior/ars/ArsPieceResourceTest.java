package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Resource contract for ARS pieces: every {@code data/dwm/tardis/ars_piece/*.json} decodes with
 * {@link ArsPieceDefinition#CODEC}, and its committed structure template is a closed shell sized to its footprint
 * whose connector apertures are wall-palette plugs with walkable air behind them. Also pins the console annex.
 */
class ArsPieceResourceTest {
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Path PIECE_ROOT = DATA.resolve(DWMReference.MOD_ID).resolve("tardis/ars_piece");

    private static final Set<String> EXPECTED_PIECES = Set.of(
            "corridor_straight", "corridor_corner", "corridor_t", "corridor_cross", "corridor_dead_end",
            "bedroom", "library", "ars_chamber");

    /** Blocks a closed doorway plug may be made of: the TARDIS wall and roundel families, any colour. */
    private static final Pattern WALL_PALETTE =
            Pattern.compile("dwm:[a-z_]+_(tardis_wall|roundel_a|roundel_b|big_roundel_a|big_roundel_b)");

    private static final Identifier CONSOLE_ANNEX = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis/ars/console_annex");
    /** Plot-local origin the annex is placed at: immediately east of the console room's east wall. */
    private static final BlockPos ANNEX_ORIGIN = new BlockPos(11, ArsGrid.FLOOR_Y, 6);

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        // Loading DWMBlocks registers the wall and decor blocks; unknown ids would otherwise decode as air.
        assertNotNull(DWMBlocks.WHITE_ROUNDEL_A);
        assertEquals(10, DWMBlocks.WHITE_ROUNDEL_A.defaultBlockState().getLightEmission(),
                "roundel light comes from its JSON definition; the light check below relies on it");
    }

    @Test
    void expectedPiecesExistAndNoneAreOrphaned() throws IOException {
        Map<String, ArsPieceDefinition> pieces = loadPieces();
        assertEquals(new TreeSet<>(EXPECTED_PIECES), new TreeSet<>(pieces.keySet()));
        for (var entry : pieces.entrySet()) {
            Identifier structure = entry.getValue().structure();
            assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis/ars/" + entry.getKey()), structure,
                    entry.getKey() + " should point at its own template");
            assertTrue(Files.isRegularFile(structureFile(structure)), entry.getKey() + ": missing " + structureFile(structure));
        }
    }

    @Test
    void templatesMatchFootprintAndAreClosedShells() throws IOException {
        for (var entry : loadPieces().entrySet()) {
            String name = entry.getKey();
            ArsPieceDefinition def = entry.getValue();
            Template template = Template.load(def.structure());
            assertEquals(def.width() * ArsGrid.CELL_SIZE, template.sizeX, name + " X size");
            assertEquals(def.depth() * ArsGrid.CELL_SIZE, template.sizeZ, name + " Z size");
            assertTrue(template.sizeY > ArsGrid.APERTURE_MAX_Y + 1 && template.sizeY <= ArsGrid.MAX_PIECE_HEIGHT,
                    name + " height " + template.sizeY);
            for (int x = 0; x < template.sizeX; x++) {
                for (int y = 0; y < template.sizeY; y++) {
                    for (int z = 0; z < template.sizeZ; z++) {
                        boolean boundary = x == 0 || x == template.sizeX - 1 || y == 0 || y == template.sizeY - 1
                                || z == 0 || z == template.sizeZ - 1;
                        BlockState state = template.at(x, y, z);
                        if (boundary && state != null && state.isAir()) {
                            fail(name + ": air on the outer boundary at " + x + "," + y + "," + z);
                        }
                    }
                }
            }
        }
    }

    @Test
    void connectorAperturesArePluggedWithAirBehind() throws IOException {
        for (var entry : loadPieces().entrySet()) {
            String name = entry.getKey();
            ArsPieceDefinition def = entry.getValue();
            Template template = Template.load(def.structure());
            for (ArsConnector connector : def.connectors()) {
                for (Aperture block : aperture(connector)) {
                    String where = name + " " + connector + " at " + block.outer;
                    BlockState plug = template.at(block.outer);
                    assertNotNull(plug, where + ": plug is structure void");
                    String id = BuiltInRegistries.BLOCK.getKey(plug.getBlock()).toString();
                    assertTrue(WALL_PALETTE.matcher(id).matches(), where + ": plug is " + id + ", not wall palette");
                    assertTrue(plug.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), where + ": plug not a full cube");

                    BlockState inner = template.at(block.inner);
                    assertTrue(inner != null && inner.isAir(), where + ": inner block " + block.inner + " is " + inner);
                    for (BlockPos pos : List.of(block.outer, block.inner)) {
                        BlockState floor = template.at(pos.getX(), ArsGrid.FLOOR_Y, pos.getZ());
                        assertTrue(floor != null && floor.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO),
                                where + ": no solid floor under " + pos);
                    }
                }
            }
        }
    }

    @Test
    void allDoorwaysAreConnectedByWalkableAir() throws IOException {
        for (var entry : loadPieces().entrySet()) {
            String name = entry.getKey();
            ArsPieceDefinition def = entry.getValue();
            Template template = Template.load(def.structure());
            List<BlockPos> inner = new ArrayList<>();
            for (ArsConnector connector : def.connectors()) {
                aperture(connector).stream()
                        .filter(block -> block.outer.getY() == ArsGrid.APERTURE_MIN_Y)
                        .forEach(block -> inner.add(block.inner));
            }
            boolean[][] reached = walkableFrom(template, inner.getFirst());
            for (BlockPos pos : inner) {
                assertTrue(reached[pos.getX()][pos.getZ()], name + ": doorway space " + pos
                        + " is not reachable through 2-high air from " + inner.getFirst());
            }
        }
    }

    @Test
    void everyPieceIsLit() throws IOException {
        for (var entry : loadPieces().entrySet()) {
            Template template = Template.load(entry.getValue().structure());
            assertTrue(template.blocks.values().stream().anyMatch(state -> state.getLightEmission() > 0),
                    entry.getKey() + " has no light-emitting block");
        }
    }

    @Test
    void consoleAnnexJoinsConsoleRoomToRootCell() throws IOException {
        Template annex = Template.load(CONSOLE_ANNEX);
        assertEquals(5, annex.sizeX);
        assertEquals(6, annex.sizeY);
        assertEquals(9, annex.sizeZ);
        int east = annex.sizeX - 1;
        int south = annex.sizeZ - 1;
        int ceiling = annex.sizeY - 1;
        // The annex ends right where the root cell's west face begins, and its doorway faces that face's aperture.
        assertEquals(ArsGrid.cellMinBlockX(ArsGrid.ROOT_CELL_X), ANNEX_ORIGIN.getX() + annex.sizeX);
        assertEquals(Direction.WEST, ArsGrid.ROOT_SIDE);
        int doorMinZ = ArsGrid.cellMinBlockZ(ArsGrid.ROOT_CELL_Z) + ArsGrid.APERTURE_MIN_OFFSET - ANNEX_ORIGIN.getZ();
        int doorMaxZ = ArsGrid.cellMinBlockZ(ArsGrid.ROOT_CELL_Z) + ArsGrid.APERTURE_MAX_OFFSET - ANNEX_ORIGIN.getZ();
        assertEquals(4, doorMinZ);
        assertEquals(7, doorMaxZ);

        for (int x = 0; x < annex.sizeX; x++) {
            for (int z = 0; z < annex.sizeZ; z++) {
                assertSolid(annex, x, 0, z, "floor");
                assertSolid(annex, x, ceiling, z, "ceiling");
                for (int y = ArsGrid.APERTURE_MIN_Y; y <= ArsGrid.APERTURE_MAX_Y; y++) {
                    boolean doorway = x == east && z >= doorMinZ && z <= doorMaxZ;
                    boolean wall = z == 0 || z == south || x == east;
                    BlockState state = annex.at(x, y, z);
                    if (doorway || !wall) {
                        assertTrue(state != null && state.isAir(), "annex " + x + "," + y + "," + z + " should be air, is " + state);
                    } else {
                        assertSolid(annex, x, y, z, "wall");
                    }
                }
            }
        }
    }

    private static void assertSolid(Template template, int x, int y, int z, String what) {
        BlockState state = template.at(x, y, z);
        assertTrue(state != null && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO),
                what + " at " + x + "," + y + "," + z + " should be solid, is " + state);
    }

    /** One aperture block: on the cell's outermost layer on the connector side, and the block one step inward. */
    private record Aperture(BlockPos outer, BlockPos inner) {
    }

    private static List<Aperture> aperture(ArsConnector connector) {
        Direction side = connector.side();
        int minX = ArsGrid.cellMinBlockX(connector.cell().x());
        int minZ = ArsGrid.cellMinBlockZ(connector.cell().z());
        int last = ArsGrid.CELL_SIZE - 1;
        Direction inward = side.getOpposite();
        List<Aperture> blocks = new ArrayList<>();
        for (int along = ArsGrid.APERTURE_MIN_OFFSET; along <= ArsGrid.APERTURE_MAX_OFFSET; along++) {
            for (int y = ArsGrid.APERTURE_MIN_Y; y <= ArsGrid.APERTURE_MAX_Y; y++) {
                BlockPos outer = switch (side) {
                    case NORTH -> new BlockPos(minX + along, y, minZ);
                    case SOUTH -> new BlockPos(minX + along, y, minZ + last);
                    case WEST -> new BlockPos(minX, y, minZ + along);
                    case EAST -> new BlockPos(minX + last, y, minZ + along);
                    default -> throw new IllegalArgumentException("Vertical connector " + connector);
                };
                blocks.add(new Aperture(outer, outer.relative(inward)));
            }
        }
        return blocks;
    }

    /** 2D flood fill over columns whose blocks at y = 1 and y = 2 are both air. */
    private static boolean[][] walkableFrom(Template template, BlockPos start) {
        boolean[][] reached = new boolean[template.sizeX][template.sizeZ];
        Deque<int[]> queue = new ArrayDeque<>();
        reached[start.getX()][start.getZ()] = true;
        queue.add(new int[] {start.getX(), start.getZ()});
        while (!queue.isEmpty()) {
            int[] at = queue.poll();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int x = at[0] + direction.getStepX();
                int z = at[1] + direction.getStepZ();
                if (x < 0 || z < 0 || x >= template.sizeX || z >= template.sizeZ || reached[x][z]) {
                    continue;
                }
                if (isAir(template.at(x, 1, z)) && isAir(template.at(x, 2, z))) {
                    reached[x][z] = true;
                    queue.add(new int[] {x, z});
                }
            }
        }
        return reached;
    }

    private static boolean isAir(BlockState state) {
        return state != null && state.isAir();
    }

    private static Map<String, ArsPieceDefinition> loadPieces() throws IOException {
        Map<String, ArsPieceDefinition> pieces = new LinkedHashMap<>();
        List<Path> files;
        try (Stream<Path> stream = Files.list(PIECE_ROOT)) {
            files = stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
        }
        assertFalse(files.isEmpty(), "No ARS piece JSON under " + PIECE_ROOT);
        for (Path file : files) {
            String name = file.getFileName().toString().replace(".json", "");
            try (Reader reader = Files.newBufferedReader(file)) {
                ArsPieceDefinition def = ArsPieceDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .getOrThrow(error -> new AssertionError(file + ": " + error));
                pieces.put(name, def);
            }
        }
        return pieces;
    }

    private static Path structureFile(Identifier structure) {
        return DATA.resolve(structure.getNamespace()).resolve("structure").resolve(structure.getPath() + ".nbt");
    }

    /** A committed structure template's blocks by position; positions it leaves out (structure void) are absent. */
    private record Template(int sizeX, int sizeY, int sizeZ, Map<BlockPos, BlockState> blocks) {
        static Template load(Identifier structure) throws IOException {
            Path file = structureFile(structure);
            assertTrue(Files.isRegularFile(file), "Missing structure template " + file);
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            ListTag size = tag.getListOrEmpty("size");
            ListTag paletteTag = tag.getListOrEmpty("palette");
            List<BlockState> palette = new ArrayList<>();
            for (int i = 0; i < paletteTag.size(); i++) {
                CompoundTag entry = paletteTag.getCompoundOrEmpty(i);
                Identifier id = Identifier.parse(entry.getStringOr("Name", ""));
                assertTrue(BuiltInRegistries.BLOCK.containsKey(id), structure + ": unknown block " + id);
                palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, entry));
            }
            Map<BlockPos, BlockState> blocks = new HashMap<>();
            ListTag blockList = tag.getListOrEmpty("blocks");
            for (int i = 0; i < blockList.size(); i++) {
                CompoundTag block = blockList.getCompoundOrEmpty(i);
                ListTag pos = block.getListOrEmpty("pos");
                blocks.put(new BlockPos(pos.getIntOr(0, 0), pos.getIntOr(1, 0), pos.getIntOr(2, 0)),
                        palette.get(block.getIntOr("state", 0)));
            }
            return new Template(size.getIntOr(0, 0), size.getIntOr(1, 0), size.getIntOr(2, 0), Map.copyOf(blocks));
        }

        /** The block at a position, or {@code null} for structure void. */
        BlockState at(int x, int y, int z) {
            return blocks.get(new BlockPos(x, y, z));
        }

        BlockState at(BlockPos pos) {
            return blocks.get(pos);
        }
    }
}
