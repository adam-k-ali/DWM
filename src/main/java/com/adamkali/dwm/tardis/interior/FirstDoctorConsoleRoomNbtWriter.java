package com.adamkali.dwm.tardis.interior;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtInt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Authors the First Doctor console-room structure template as compressed NBT without requiring
 * live block registries (stable string block ids only).
 */
public final class FirstDoctorConsoleRoomNbtWriter {
    private FirstDoctorConsoleRoomNbtWriter() {
    }

    public static void write(Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        NbtIo.writeCompressed(build(), outputPath);
    }

    public static NbtCompound build() {
        Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        NbtList palette = new NbtList();
        NbtList blocks = new NbtList();

        List<Placement> placements = new ArrayList<>();
        String floor = "dwm:white_tardis_wall";
        String wall = "dwm:white_roundel_a";
        String roundel = "dwm:white_big_roundel_a";
        String ceiling = "dwm:light_gray_tardis_wall";
        String console = "dwm:teal_big_roundel_a";
        String air = "minecraft:air";
        String light = "minecraft:light";
        int sizeX = FirstDoctorConsoleRoomPlacer.SIZE_X;
        int sizeY = FirstDoctorConsoleRoomPlacer.SIZE_Y;
        int sizeZ = FirstDoctorConsoleRoomPlacer.SIZE_Z;

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                placements.add(new Placement(x, 0, z, floor));
                placements.add(new Placement(x, sizeY - 1, z, ceiling));
                for (int y = 1; y < sizeY - 1; y++) {
                    boolean edge = x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1;
                    placements.add(new Placement(x, y, z, edge ? wall : air));
                }
            }
        }
        // Origin (4,1,0); slots 0..2 along +X when facing south; half lower/upper.
        for (String half : new String[]{"lower", "upper"}) {
            for (int slot = 0; slot < 3; slot++) {
                int x = 4 + slot;
                int y = half.equals("lower") ? 1 : 2;
                placements.add(new Placement(x, y, 0,
                        "dwm:tardis_interior_door[facing=south,half=" + half + ",slot=" + slot + ",open=true]"));
            }
        }
        placements.add(new Placement(5, 1, 5, console));
        placements.add(new Placement(5, 2, 5, roundel));
        placements.add(new Placement(6, 1, 5, floor));
        placements.add(new Placement(4, 1, 5, floor));
        placements.add(new Placement(5, 1, 6, floor));
        placements.add(new Placement(5, 1, 4, floor));
        placements.add(new Placement(5, 4, 5, light));
        placements.add(new Placement(0, 2, 5, roundel));
        placements.add(new Placement(sizeX - 1, 2, 5, roundel));
        placements.add(new Placement(5, 2, sizeZ - 1, roundel));

        for (Placement placement : placements) {
            int index = paletteIndex.computeIfAbsent(placement.stateKey, key -> {
                int i = palette.size();
                palette.add(paletteEntry(key));
                return i;
            });
            NbtCompound block = new NbtCompound();
            block.putIntArray("pos", new int[]{placement.x, placement.y, placement.z});
            block.putInt("state", index);
            blocks.add(block);
        }

        NbtCompound root = new NbtCompound();
        NbtList size = new NbtList();
        size.add(NbtInt.of(sizeX));
        size.add(NbtInt.of(sizeY));
        size.add(NbtInt.of(sizeZ));
        root.put("size", size);
        root.put("palette", palette);
        root.put("blocks", blocks);
        root.put("entities", new NbtList());
        root.putInt("DataVersion", 4189);
        return root;
    }

    private static NbtCompound paletteEntry(String stateKey) {
        NbtCompound entry = new NbtCompound();
        int bracket = stateKey.indexOf('[');
        if (bracket < 0) {
            entry.putString("Name", stateKey);
            return entry;
        }
        entry.putString("Name", stateKey.substring(0, bracket));
        NbtCompound props = new NbtCompound();
        String propsPart = stateKey.substring(bracket + 1, stateKey.length() - 1);
        for (String pair : propsPart.split(",")) {
            String[] kv = pair.split("=", 2);
            props.putString(kv[0], kv[1]);
        }
        entry.put("Properties", props);
        return entry;
    }

    private record Placement(int x, int y, int z, String stateKey) {
    }
}
