package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioCoordinates;
import com.adamkali.screenplay.ScenarioException;
import com.adamkali.screenplay.ScenarioIds;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class WaitUntilPrimitive implements ScenarioPrimitive {
    private static final List<String> CONDITION_LIST = List.of(
            "visible",
            "notVisible",
            "holding",
            "notHolding",
            "block",
            "overlay",
            "toast"
    );
    private static final Set<String> CONDITIONS = Set.copyOf(CONDITION_LIST);
    private static final Set<String> BLOCK_KEYS = Set.of("id", "x", "y", "z");
    private static final Set<String> HOLDING_KEYS = Set.of("id");
    private static final Set<String> TOAST_KEYS = Set.of("type", "contains", "id");
    private static final Set<String> TOAST_TYPES = Set.of("advancement");
    private static final String ONE_OF = CONDITION_LIST.stream()
            .map(condition -> "'" + condition + "'")
            .collect(Collectors.collectingAndThen(Collectors.toList(), WaitUntilPrimitive::joinOneOf));

    @Override
    public String name() {
        return "waitUntil";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        try {
            return normalize(arguments);
        } catch (ScenarioException exception) {
            throw new ScenarioException(source + ": " + exception.getMessage());
        }
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Map<String, Object> arguments = context.arguments();
        if (arguments.containsKey("holding")) {
            return isHolding(context.client(), (String) arguments.get("holding"));
        }
        if (arguments.containsKey("notHolding")) {
            return !isHolding(context.client(), (String) arguments.get("notHolding"));
        }
        if (arguments.containsKey("block")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> block = (Map<String, Object>) arguments.get("block");
            return isBlock(context.client(), block);
        }
        if (arguments.containsKey("overlay")) {
            return matchesOverlay(context.client(), (String) arguments.get("overlay"));
        }
        if (arguments.containsKey("toast")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> toast = (Map<String, Object>) arguments.get("toast");
            return matchesToast(context.client(), toast);
        }
        boolean expectVisible = arguments.containsKey("visible");
        @SuppressWarnings("unchecked")
        Map<String, Object> selector = (Map<String, Object>) arguments.get(
                expectVisible ? "visible" : "notVisible");
        boolean matches = context.widgetFinder().matches(context.screen(), selector);
        return expectVisible == matches;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!CONDITIONS.contains(key)) {
                throw new ScenarioException("waitUntil does not accept '" + key + "'");
            }
        }
        long supplied = CONDITION_LIST.stream().filter(arguments::containsKey).count();
        if (supplied != 1) {
            throw new ScenarioException("waitUntil requires exactly one of " + ONE_OF);
        }
        Map<String, Object> validated = new LinkedHashMap<>();
        if (arguments.containsKey("holding")) {
            validated.put("holding", normalizeHolding(arguments.get("holding")));
            return validated;
        }
        if (arguments.containsKey("notHolding")) {
            validated.put("notHolding", normalizeHolding(arguments.get("notHolding")));
            return validated;
        }
        if (arguments.containsKey("block")) {
            validated.put("block", Map.copyOf(normalizeBlock(arguments.get("block"))));
            return validated;
        }
        if (arguments.containsKey("overlay")) {
            validated.put("overlay", normalizeOverlay(arguments.get("overlay")));
            return validated;
        }
        if (arguments.containsKey("toast")) {
            validated.put("toast", Map.copyOf(normalizeToast(arguments.get("toast"))));
            return validated;
        }
        String condition = arguments.containsKey("visible") ? "visible" : "notVisible";
        Map<String, Object> selector = nestedObject(arguments.get(condition), condition);
        validated.put(condition, Map.copyOf(SelectorPrimitive.validateSelector(
                selector, null, "waitUntil " + condition)));
        return validated;
    }

    private static String normalizeHolding(Object value) {
        if (value instanceof Map<?, ?>) {
            Map<String, Object> holding = nestedObject(value, "holding");
            for (String key : holding.keySet()) {
                if (!HOLDING_KEYS.contains(key)) {
                    throw new ScenarioException("waitUntil holding does not accept '" + key + "'");
                }
            }
            return ScenarioIds.normalize(holding.get("id"), "waitUntil holding");
        }
        return ScenarioIds.normalize(value, "waitUntil holding");
    }

    private static String normalizeOverlay(Object value) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("waitUntil overlay must be a non-empty string");
        }
        return string.trim();
    }

    private static Map<String, Object> normalizeToast(Object value) {
        Map<String, Object> toast = nestedObject(value, "toast");
        for (String key : toast.keySet()) {
            if (!TOAST_KEYS.contains(key)) {
                throw new ScenarioException("waitUntil toast does not accept '" + key + "'");
            }
        }
        if (!toast.containsKey("type")) {
            throw new ScenarioException("waitUntil toast requires 'type'");
        }
        if (!(toast.get("type") instanceof String typeString) || typeString.isBlank()) {
            throw new ScenarioException("waitUntil toast type must be 'advancement'");
        }
        String type = typeString.trim().toLowerCase(Locale.ROOT);
        if (!TOAST_TYPES.contains(type)) {
            throw new ScenarioException("waitUntil toast type must be 'advancement'");
        }
        boolean hasContains = toast.containsKey("contains");
        boolean hasId = toast.containsKey("id");
        if (!hasContains && !hasId) {
            throw new ScenarioException("waitUntil toast requires 'contains' and/or 'id'");
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("type", type);
        if (hasContains) {
            if (!(toast.get("contains") instanceof String contains) || contains.isBlank()) {
                throw new ScenarioException("waitUntil toast contains must be a non-empty string");
            }
            normalized.put("contains", contains.trim());
        }
        if (hasId) {
            normalized.put("id", ScenarioIds.normalize(toast.get("id"), "waitUntil toast id"));
        }
        return normalized;
    }

    private static Map<String, Object> normalizeBlock(Object value) {
        Map<String, Object> block = nestedObject(value, "block");
        for (String key : block.keySet()) {
            if (!BLOCK_KEYS.contains(key)) {
                throw new ScenarioException("waitUntil block does not accept '" + key + "'");
            }
        }
        for (String key : BLOCK_KEYS) {
            if (!block.containsKey(key)) {
                throw new ScenarioException("waitUntil block requires '" + key + "'");
            }
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("id", ScenarioIds.normalize(block.get("id"), "waitUntil block id"));
        for (String axis : List.of("x", "y", "z")) {
            ScenarioCoordinates.Component component = ScenarioCoordinates.parse(
                    block.get(axis), "waitUntil block " + axis);
            normalized.put(axis, component.authored());
        }
        return normalized;
    }

    private static boolean isHolding(Minecraft client, String itemId) {
        LocalPlayer player = client.player;
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return "minecraft:air".equals(itemId);
        }
        Identifier actual = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId.equals(actual.toString());
    }

    private static boolean isBlock(Minecraft client, Map<String, Object> block) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return false;
        }
        BlockPos origin = player.blockPosition();
        BlockPos pos = new BlockPos(
                ScenarioCoordinates.parse(block.get("x"), "waitUntil block x").resolve(origin.getX()),
                ScenarioCoordinates.parse(block.get("y"), "waitUntil block y").resolve(origin.getY()),
                ScenarioCoordinates.parse(block.get("z"), "waitUntil block z").resolve(origin.getZ())
        );
        BlockState state = client.level.getBlockState(pos);
        Identifier actual = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return ((String) block.get("id")).equals(actual.toString());
    }

    private static boolean matchesOverlay(Minecraft client, String expected) {
        Gui gui = client.gui;
        if (gui == null) {
            return false;
        }
        Hud hud = gui.hud;
        if (hud == null || hud.overlayMessageTime <= 0) {
            return false;
        }
        Component message = hud.overlayMessageString;
        if (message == null) {
            return false;
        }
        return message.getString().contains(expected);
    }

    private static boolean matchesToast(Minecraft client, Map<String, Object> toast) {
        Gui gui = client.gui;
        if (gui == null) {
            return false;
        }
        ToastManager toastManager = gui.toastManager;
        if (toastManager == null || toastManager.visibleToasts.isEmpty()) {
            return false;
        }
        String type = (String) toast.get("type");
        if (!"advancement".equals(type)) {
            return false;
        }
        String contains = (String) toast.get("contains");
        String id = (String) toast.get("id");
        for (Object instance : toastManager.visibleToasts) {
            if (!(instance instanceof ToastManager.ToastInstance<?> toastInstance)) {
                continue;
            }
            Toast visible = toastInstance.getToast();
            if (!(visible instanceof AdvancementToast advancementToast)) {
                continue;
            }
            AdvancementHolder advancement = advancementToast.advancement;
            if (id != null && !id.equals(advancement.id().toString())) {
                continue;
            }
            if (contains != null) {
                String title = advancement.value().display()
                        .map(display -> display.getTitle().getString())
                        .orElse("");
                if (!title.contains(contains)) {
                    continue;
                }
            }
            return true;
        }
        return false;
    }

    private static Map<String, Object> nestedObject(Object value, String condition) {
        if (value instanceof List<?> list) {
            if (list.size() != 1) {
                throw new ScenarioException("waitUntil " + condition
                        + " expects one argument object, but received " + list.size());
            }
            value = list.getFirst();
        }
        if (!(value instanceof Map<?, ?> raw)) {
            throw new ScenarioException("waitUntil '" + condition + "' must be an object");
        }
        Map<String, Object> object = new LinkedHashMap<>();
        raw.forEach((key, entryValue) -> {
            if (!(key instanceof String stringKey)) {
                throw new ScenarioException("waitUntil '" + condition + "' object keys must be strings");
            }
            object.put(stringKey, entryValue);
        });
        return object;
    }

    private static String joinOneOf(List<String> values) {
        if (values.size() < 2) {
            return String.join(", ", values);
        }
        return String.join(", ", values.subList(0, values.size() - 1)) + ", or " + values.getLast();
    }
}
