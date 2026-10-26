package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps committed structure templates in sync with their Java layouts.
 *
 * <p>{@code ./gradlew generateStructureTemplates} sets {@code dwm.writeStructureTemplates} and runs
 * {@link #writeTemplates()}; the drift check runs in every {@code test} build.
 */
class StructureTemplateGeneratorTest {
    static final String WRITE_PROPERTY = "dwm.writeStructureTemplates";

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    @EnabledIfSystemProperty(named = WRITE_PROPERTY, matches = "true")
    void writeTemplates() throws IOException {
        for (TemplateLayout layout : DalekOutpostTemplates.all()) {
            Files.createDirectories(layout.file().getParent());
            NbtIo.writeCompressed(layout.toNbt(), layout.file());
        }
    }

    @Test
    void committedTemplatesMatchLayouts() throws IOException {
        for (TemplateLayout layout : DalekOutpostTemplates.all()) {
            assertTrue(Files.isRegularFile(layout.file()),
                    () -> layout.file() + " is missing; run ./gradlew generateStructureTemplates");
            CompoundTag committed = NbtIo.readCompressed(layout.file(), NbtAccounter.unlimitedHeap());
            assertEquals(layout.toNbt(), committed,
                    () -> layout.file() + " drifted from its layout; run ./gradlew generateStructureTemplates");
        }
    }
}
