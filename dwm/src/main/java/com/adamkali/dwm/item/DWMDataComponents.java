package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class DWMDataComponents {
    public static final @NonNull DataComponentType<UUID> BOUND_TARDIS_ID = register(
            "bound_tardis_id",
            DataComponentType.<UUID>builder()
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
                    .build()
    );

    public static final @NonNull DataComponentType<SonicState> SONIC_STATE = register(
            "sonic_state",
            DataComponentType.<SonicState>builder()
                    .persistent(SonicState.CODEC)
                    .networkSynchronized(SonicState.STREAM_CODEC)
                    .build()
    );

    private DWMDataComponents() {
    }

    public static void initialize() {
        // Loads static component registrations.
    }

    @SuppressWarnings("null")
    private static <T> @NonNull DataComponentType<T> register(
            @NonNull String id,
            @NonNull DataComponentType<T> componentType
    ) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, id),
                componentType
        );
    }
}
