package com.adamkali.dwm.forge;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Opens Forge/vanilla registries so Fabric-style {@code Registry.register} calls in
 * {@link com.adamkali.dwm.DwmCommon} can run during mod construction.
 *
 * <p>Forge locks {@code NamespacedWrapper} against direct vanilla registration and freezes
 * mapped registries before mod constructors. Until DWM migrates to DeferredRegister /
 * RegisterEvent, this bootstrap mirrors the window Forge opens around RegisterEvent.
 *
 * <p>Early registration can also skip loader bake hooks that call
 * {@link BlockState#initCache()}; {@link #initBlockStateCachesAfterRegistration()} fills
 * occlusion shape caches so chunk meshing does not NPE on DWM neighbors.
 */
final class ForgeRegistryBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("dwm");

    private ForgeRegistryBootstrap() {
    }

    static void unlockForFabricStyleRegistration() {
        GameData.unfreezeData();
        for (var entry : RegistryManager.ACTIVE.getRegistries().values()) {
            if (entry instanceof ForgeRegistry<?> forgeRegistry) {
                forgeRegistry.unfreeze();
            }
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (registry instanceof MappedRegistry<?> mapped) {
                mapped.unfreeze();
            }
            clearNamespacedWrapperLock(registry);
        }
    }

    static void initBlockStateCachesAfterRegistration() {
        int states = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !DWMReference.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
                states++;
            }
        }
        LOGGER.debug("Initialized occlusion caches for {} DWM block states", states);
    }

    private static void clearNamespacedWrapperLock(Registry<?> registry) {
        Class<?> type = registry.getClass();
        while (type != null && type != Object.class) {
            try {
                Field locked = type.getDeclaredField("locked");
                locked.setAccessible(true);
                if (locked.getType() == boolean.class && locked.getBoolean(registry)) {
                    locked.setBoolean(registry, false);
                    LOGGER.debug("Cleared Forge registry lock on {}", registry.key());
                }
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException exception) {
                LOGGER.warn("Could not clear Forge registry lock on {}: {}", registry.key(), exception.toString());
                return;
            }
        }
    }
}
