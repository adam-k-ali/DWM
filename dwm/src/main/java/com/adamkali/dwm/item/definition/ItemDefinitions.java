package com.adamkali.dwm.item.definition;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.data.definition.DefinitionFiles;
import com.adamkali.dwm.item.DWMItems;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Registry of JSON item definitions ({@code data/dwm/item/definition/*.json}). Loaded from the jar at init;
 * {@code /reload} does not apply. See {@code docs/feature-block-item-definitions.md}.
 */
public final class ItemDefinitions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> MERGE_KEYS = Set.of("properties");

    private static final Map<Identifier, ItemDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean initialized;

    private ItemDefinitions() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(parse(DefinitionFiles.readAll("item")));
        initialized = true;
        LOGGER.info("Loaded {} item definition(s)", DEFINITIONS.size());
    }

    /** Resolves parents, skips abstract templates and decodes. Exposed for tests. */
    public static Map<Identifier, ItemDefinition> parse(Map<Identifier, JsonObject> raw) {
        Map<Identifier, ItemDefinition> parsed = new LinkedHashMap<>();
        DefinitionFiles.resolveParents(raw, MERGE_KEYS).forEach((fileId, json) -> {
            if (DefinitionFiles.isAbstract(json)) {
                return;
            }
            json.remove(DefinitionFiles.ABSTRACT);
            try {
                ItemDefinition definition = ItemDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                if (!fileId.equals(definition.item())) {
                    throw new IllegalStateException("Item id mismatch: file " + fileId + " vs json " + definition.item());
                }
                parsed.put(definition.item(), definition);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to load item definition " + fileId, e);
            }
        });
        return parsed;
    }

    public static Map<Identifier, ItemDefinition> all() {
        initialize();
        return Map.copyOf(DEFINITIONS);
    }

    public static Optional<ItemDefinition> get(Identifier id) {
        initialize();
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    /** Registers the item described by {@code dwm:<path>}. */
    public static Item register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
        ItemDefinition definition = get(id).orElseThrow(() -> new IllegalStateException("Missing item definition for " + id));
        Item.Properties properties = definition.properties().apply(new Item.Properties(), definition.type());
        return DWMItems.register(factory(definition), properties, path);
    }

    /** Registers definitions that no Java field claimed, so a data-only item needs just its JSON. */
    public static void registerUnclaimed() {
        initialize();
        for (ItemDefinition definition : DEFINITIONS.values()) {
            if (!BuiltInRegistries.ITEM.containsKey(definition.item())) {
                register(definition.item().getPath());
                LOGGER.debug("Registered data-only item {}", definition.item());
            }
        }
    }

    private static Function<Item.Properties, Item> factory(ItemDefinition definition) {
        ItemProperties.Tool tool = definition.properties().tool().orElse(null);
        return switch (definition.type()) {
            case ITEM, SWORD, PICKAXE, ARMOR -> Item::new;
            case SHOVEL -> props -> new ShovelItem(tool.material(), tool.attackDamage(), tool.attackSpeed(), props);
            case AXE -> props -> new AxeItem(tool.material(), tool.attackDamage(), tool.attackSpeed(), props);
            case HOE -> props -> new HoeItem(tool.material(), tool.attackDamage(), tool.attackSpeed(), props);
        };
    }
}
