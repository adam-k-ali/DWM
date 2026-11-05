package com.adamkali.dwm.block.definition;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.data.definition.DefinitionFiles;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Registry of JSON block definitions ({@code data/dwm/block/definition/*.json}). Loaded from the jar at init;
 * {@code /reload} does not apply. See {@code docs/feature-block-item-definitions.md}.
 */
public final class BlockDefinitions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> MERGE_KEYS = Set.of("properties");

    private static final Map<Identifier, BlockDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean initialized;

    private BlockDefinitions() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(parse(DefinitionFiles.readAll("block")));
        initialized = true;
        LOGGER.info("Loaded {} block definition(s)", DEFINITIONS.size());
    }

    /** Resolves parents, skips abstract templates and decodes. Exposed for tests. */
    public static Map<Identifier, BlockDefinition> parse(Map<Identifier, JsonObject> raw) {
        Map<Identifier, BlockDefinition> parsed = new LinkedHashMap<>();
        DefinitionFiles.resolveParents(raw, MERGE_KEYS).forEach((fileId, json) -> {
            if (DefinitionFiles.isAbstract(json)) {
                return;
            }
            json.remove(DefinitionFiles.ABSTRACT);
            try {
                BlockDefinition definition = BlockDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                if (!fileId.equals(definition.block())) {
                    throw new IllegalStateException("Block id mismatch: file " + fileId + " vs json " + definition.block());
                }
                parsed.put(definition.block(), definition);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to load block definition " + fileId, e);
            }
        });
        return parsed;
    }

    public static Map<Identifier, BlockDefinition> all() {
        initialize();
        return Map.copyOf(DEFINITIONS);
    }

    public static Optional<BlockDefinition> get(Identifier id) {
        initialize();
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    /** Registers the block (and its item, unless {@code "item": false}) described by {@code dwm:<path>}. */
    public static Block register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
        BlockDefinition definition = get(id).orElseThrow(() -> new IllegalStateException("Missing block definition for " + id));
        Block base = definition.base().map(baseId -> BuiltInRegistries.BLOCK.getOptional(baseId).orElseThrow(
                () -> new IllegalStateException(id + " base " + baseId + " is not registered yet; register it first")))
                .orElse(null);
        BlockBehaviour.Properties properties = base != null
                ? BlockBehaviour.Properties.ofLegacyCopy(base)
                : BlockBehaviour.Properties.of();
        definition.properties().apply(properties);
        Function<BlockBehaviour.Properties, Block> factory = factory(definition, base);
        return definition.item()
                ? DWMBlocks.registerBlock(factory, properties, path)
                : DWMBlocks.registerBlockWithoutItem(factory, properties, path);
    }

    /**
     * Registers definitions that no Java field claimed, so a data-only block needs just its JSON. Definitions are
     * registered in file-name order; a stairs/slab/wall must therefore sort after its base or be claimed in Java.
     */
    public static void registerUnclaimed() {
        initialize();
        for (BlockDefinition definition : DEFINITIONS.values()) {
            if (BuiltInRegistries.BLOCK.containsKey(definition.block())) {
                continue;
            }
            Block block = register(definition.block().getPath());
            definition.creativeTab().ifPresent(tab -> CreativeModeTabEvents
                    .modifyOutputEvent(ResourceKey.<CreativeModeTab>create(Registries.CREATIVE_MODE_TAB, tab))
                    .register(content -> content.accept(block)));
            LOGGER.debug("Registered data-only block {}", definition.block());
        }
    }

    private static Function<BlockBehaviour.Properties, Block> factory(BlockDefinition definition, Block base) {
        return switch (definition.type()) {
            case BLOCK -> Block::new;
            case STAIRS -> settings -> new StairBlock(base.defaultBlockState(), settings);
            case SLAB -> SlabBlock::new;
            case WALL -> WallBlock::new;
            case COLORED_FALLING -> {
                ColorRGBA dust = new ColorRGBA(definition.fallingDustColor().orElseThrow());
                yield settings -> new ColoredFallingBlock(dust, settings);
            }
        };
    }
}
