package com.adamkali.dwm.world.structure;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A structure template authored as character-grid layers, so worldgen pieces can be reviewed and
 * regenerated without an in-game structure block.
 *
 * <p>Layers run bottom-up (y = 0 first). Each layer lists rows north to south (z), and each row
 * lists cells west to east (x). A space is structure void (the cell is left out, so terrain stays);
 * every other character must be in the legend, including air for cells that should be carved.
 */
public record TemplateLayout(String path, List<List<String>> layers, Map<Character, Cell> legend) {
    /** Template paths live under {@code data/dwm/structure/}. */
    public static final Path STRUCTURE_ROOT = Path.of("src/main/resources/data/dwm/structure");

    public record Cell(BlockState state, CompoundTag nbt) {
    }

    public int sizeX() {
        return layers.getFirst().getFirst().length();
    }

    public int sizeY() {
        return layers.size();
    }

    public int sizeZ() {
        return layers.getFirst().size();
    }

    public Path file() {
        return STRUCTURE_ROOT.resolve(path + ".nbt");
    }

    /**
     * Builds the template NBT in vanilla's canonical form by round-tripping the raw layout through
     * {@link StructureTemplate#load} and {@link StructureTemplate#save}, so the output matches what a
     * structure block would save.
     */
    public CompoundTag toNbt() {
        Map<BlockState, Integer> palette = new LinkedHashMap<>();
        ListTag blocks = new ListTag();
        for (int y = 0; y < sizeY(); y++) {
            List<String> rows = layers.get(y);
            for (int z = 0; z < sizeZ(); z++) {
                String row = rows.get(z);
                for (int x = 0; x < sizeX(); x++) {
                    char symbol = row.charAt(x);
                    if (symbol == ' ') {
                        continue;
                    }
                    Cell cell = legend.get(symbol);
                    if (cell == null) {
                        throw new IllegalStateException(path + ": no legend entry for '" + symbol + "' at " + x + "," + y + "," + z);
                    }
                    CompoundTag block = new CompoundTag();
                    block.put("pos", intList(x, y, z));
                    block.putInt("state", palette.computeIfAbsent(cell.state(), state -> palette.size()));
                    if (cell.nbt() != null) {
                        block.put("nbt", cell.nbt().copy());
                    }
                    blocks.add(block);
                }
            }
        }

        ListTag paletteTag = new ListTag();
        palette.keySet().forEach(state -> paletteTag.add(NbtUtils.writeBlockState(state)));

        CompoundTag raw = new CompoundTag();
        raw.put("size", intList(sizeX(), sizeY(), sizeZ()));
        raw.put("palette", paletteTag);
        raw.put("blocks", blocks);
        raw.put("entities", new ListTag());

        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK, raw);
        return template.save(new CompoundTag());
    }

    private static ListTag intList(int... values) {
        ListTag list = new ListTag();
        for (int value : values) {
            list.add(IntTag.valueOf(value));
        }
        return list;
    }

    public static Builder builder(String path) {
        return new Builder(path);
    }

    public static final class Builder {
        private final String path;
        private final Map<Character, Cell> legend = new LinkedHashMap<>();
        private final List<List<String>> layers = new ArrayList<>();

        private Builder(String path) {
            this.path = path;
        }

        public Builder legend(char symbol, BlockState state) {
            return legend(symbol, state, null);
        }

        public Builder legend(char symbol, BlockState state, CompoundTag nbt) {
            if (symbol == ' ' || legend.putIfAbsent(symbol, new Cell(state, nbt)) != null) {
                throw new IllegalArgumentException(path + ": legend symbol '" + symbol + "' is reserved or reused");
            }
            return this;
        }

        public Builder layer(String... rows) {
            layers.add(List.of(rows));
            return this;
        }

        public TemplateLayout build() {
            if (layers.isEmpty()) {
                throw new IllegalStateException(path + ": template has no layers");
            }
            int width = layers.getFirst().getFirst().length();
            int depth = layers.getFirst().size();
            for (int y = 0; y < layers.size(); y++) {
                List<String> rows = layers.get(y);
                if (rows.size() != depth) {
                    throw new IllegalStateException(path + ": layer " + y + " has " + rows.size() + " rows, expected " + depth);
                }
                for (int z = 0; z < depth; z++) {
                    if (rows.get(z).length() != width) {
                        throw new IllegalStateException(path + ": layer " + y + " row " + z + " has width " + rows.get(z).length() + ", expected " + width);
                    }
                }
            }
            return new TemplateLayout(path, List.copyOf(layers), Map.copyOf(legend));
        }
    }
}
