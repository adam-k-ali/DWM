package com.adamkali.dwm.world.village;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

/**
 * Maps vanilla plains-village blocks onto Ash wood and Gallifrey stone,
 * copying blockstate properties via {@link Block#withPropertiesOf(BlockState)}.
 */
public final class GallifreyVillagePalette {
    public static final float MOSSIFY_CHANCE = 0.1F;

    private static final Map<Block, Block> REPLACEMENTS = Map.ofEntries(
            Map.entry(Blocks.GRASS_BLOCK, DWMBlocks.GALLIFREY_GRASS_BLOCK),
            Map.entry(Blocks.DIRT, DWMBlocks.GALLIFREY_DIRT),
            Map.entry(Blocks.DIRT_PATH, DWMBlocks.GALLIFREY_COARSE_DIRT),

            Map.entry(Blocks.OAK_PLANKS, DWMBlocks.ASH_PLANKS),
            Map.entry(Blocks.OAK_LOG, DWMBlocks.ASH_LOG),
            Map.entry(Blocks.OAK_WOOD, DWMBlocks.ASH_WOOD),
            Map.entry(Blocks.STRIPPED_OAK_LOG, DWMBlocks.STRIPPED_ASH_LOG),
            Map.entry(Blocks.STRIPPED_OAK_WOOD, DWMBlocks.STRIPPED_ASH_WOOD),
            Map.entry(Blocks.OAK_STAIRS, DWMBlocks.ASH_STAIRS),
            Map.entry(Blocks.OAK_SLAB, DWMBlocks.ASH_SLAB),
            Map.entry(Blocks.OAK_FENCE, DWMBlocks.ASH_FENCE),
            Map.entry(Blocks.OAK_FENCE_GATE, DWMBlocks.ASH_FENCE_GATE),
            Map.entry(Blocks.OAK_DOOR, DWMBlocks.ASH_DOOR),
            Map.entry(Blocks.OAK_TRAPDOOR, DWMBlocks.ASH_TRAPDOOR),
            Map.entry(Blocks.OAK_LEAVES, DWMBlocks.ASH_LEAVES),
            Map.entry(Blocks.OAK_SAPLING, DWMBlocks.ASH_SAPLING),
            Map.entry(Blocks.POTTED_OAK_SAPLING, DWMBlocks.POTTED_ASH_SAPLING),

            Map.entry(Blocks.COBBLESTONE, DWMBlocks.GALLIFREY_COBBLESTONE),
            Map.entry(Blocks.COBBLESTONE_STAIRS, DWMBlocks.GALLIFREY_COBBLESTONE_STAIRS),
            Map.entry(Blocks.COBBLESTONE_SLAB, DWMBlocks.GALLIFREY_COBBLESTONE_SLAB),
            Map.entry(Blocks.COBBLESTONE_WALL, DWMBlocks.GALLIFREY_COBBLESTONE_WALL),
            Map.entry(Blocks.MOSSY_COBBLESTONE, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE),
            Map.entry(Blocks.MOSSY_COBBLESTONE_STAIRS, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_STAIRS),
            Map.entry(Blocks.MOSSY_COBBLESTONE_SLAB, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_SLAB),
            Map.entry(Blocks.MOSSY_COBBLESTONE_WALL, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_WALL),

            Map.entry(Blocks.STONE, DWMBlocks.GALLIFREY_STONE),
            Map.entry(Blocks.SMOOTH_STONE, DWMBlocks.GALLIFREY_SMOOTH_STONE),
            Map.entry(Blocks.SMOOTH_STONE_SLAB, DWMBlocks.GALLIFREY_SMOOTH_STONE_SLAB),
            Map.entry(Blocks.STONE_BRICKS, DWMBlocks.GALLIFREY_STONE_BRICKS),
            Map.entry(Blocks.STONE_BRICK_STAIRS, DWMBlocks.GALLIFREY_STONE_BRICK_STAIRS),
            Map.entry(Blocks.STONE_BRICK_SLAB, DWMBlocks.GALLIFREY_STONE_BRICK_SLAB),
            Map.entry(Blocks.STONE_BRICK_WALL, DWMBlocks.GALLIFREY_STONE_BRICK_WALL),
            Map.entry(Blocks.MOSSY_STONE_BRICKS, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS),
            Map.entry(Blocks.MOSSY_STONE_BRICK_STAIRS, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICK_STAIRS),
            Map.entry(Blocks.MOSSY_STONE_BRICK_SLAB, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICK_SLAB),
            Map.entry(Blocks.MOSSY_STONE_BRICK_WALL, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICK_WALL),
            Map.entry(Blocks.CRACKED_STONE_BRICKS, DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS),
            Map.entry(Blocks.CHISELED_STONE_BRICKS, DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS),

            Map.entry(Blocks.POTTED_DANDELION, DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE),
            Map.entry(Blocks.POTTED_POPPY, DWMBlocks.POTTED_MOONLIGHT_BLOOM),

            Map.entry(Blocks.DYED_TERRACOTTA.white(), DWMBlocks.CITADEL_TILE),
            Map.entry(Blocks.TERRACOTTA, DWMBlocks.CITADEL_WALL)
    );

    private static final Map<Block, Block> MOSSY_VARIANTS = Map.of(
            Blocks.COBBLESTONE, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE,
            Blocks.COBBLESTONE_STAIRS, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_STAIRS,
            Blocks.COBBLESTONE_SLAB, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_SLAB,
            Blocks.COBBLESTONE_WALL, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_WALL
    );

    private GallifreyVillagePalette() {
    }

    public static BlockState replace(BlockState state) {
        return replace(state, null);
    }

    public static BlockState replace(BlockState state, @Nullable RandomSource random) {
        Block block = state.getBlock();
        if (random != null) {
            Block mossy = MOSSY_VARIANTS.get(block);
            if (mossy != null && random.nextFloat() < MOSSIFY_CHANCE) {
                return mossy.withPropertiesOf(state);
            }
        }
        Block replacement = REPLACEMENTS.get(block);
        if (replacement == null) {
            return state;
        }
        return replacement.withPropertiesOf(state);
    }

    public static boolean mapsTo(Block source, Block expected) {
        return REPLACEMENTS.get(source) == expected;
    }

    public static boolean usesCardinalDoor() {
        return REPLACEMENTS.containsValue(DWMBlocks.CARDINAL_DOOR);
    }
}
