package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import com.adamkali.dwm.blueprint.model.BlueprintBlockState;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves blueprint block definitions to {@link BlockState}s.
 */
public final class BlueprintBlockResolver {
    private BlueprintBlockResolver() {
    }

    /**
     * @return empty if the id is malformed or not registered
     */
    public static Optional<BlockState> resolve(BlueprintBlock definition) {
        Identifier id = Identifier.tryParse(definition.id());
        if (id == null) {
            return Optional.empty();
        }
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        if (block.isEmpty()) {
            return Optional.empty();
        }
        BlockState state = block.get().defaultBlockState();
        BlueprintBlockState props = definition.state();
        if (props != null && props.facing() != null) {
            Direction facing = parseFacing(props.facing());
            if (facing == null) {
                return Optional.empty();
            }
            BlockState withFacing = applyFacing(state, facing);
            if (withFacing != null) {
                state = withFacing;
            }
            // Block has no compatible facing property — ignore facing rather than fail placement.
        }
        return Optional.of(state);
    }

    @Nullable
    private static Direction parseFacing(String facing) {
        return switch (facing) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> null;
        };
    }

    /**
     * Applies {@code facing} to the first enum property named {@code facing} that accepts the value.
     *
     * @return null if no compatible facing property exists
     */
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyFacing(BlockState state, Direction facing) {
        for (Property<?> property : state.getProperties()) {
            if (!"facing".equals(property.getName())) {
                continue;
            }
            if (!(property instanceof EnumProperty<?> enumProp)) {
                continue;
            }
            if (!enumProp.getValueClass().isAssignableFrom(Direction.class)) {
                continue;
            }
            EnumProperty directionProp = (EnumProperty) enumProp;
            if (!directionProp.getPossibleValues().contains(facing)) {
                // e.g. horizontal-only facing with UP/DOWN requested — skip
                continue;
            }
            return state.setValue(directionProp, facing);
        }
        return null;
    }
}
