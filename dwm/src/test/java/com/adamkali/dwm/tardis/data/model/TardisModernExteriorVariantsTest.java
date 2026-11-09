package com.adamkali.dwm.tardis.data.model;

import com.adamkali.dwm.DWMReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the 2005/2010/2014/2018 exteriors are wired to an existing mesh and a texture of the declared size. */
class TardisModernExteriorVariantsTest {
    private static final List<TardisChameleonVariant> MODERN = List.of(
            TardisChameleonVariant.TARDIS_2005,
            TardisChameleonVariant.TARDIS_2010,
            TardisChameleonVariant.TARDIS_2014,
            TardisChameleonVariant.TARDIS_2018);

    private static final Path ASSETS = Path.of("src/client/resources/assets/dwm");

    @Test
    void idsRoundTripThroughFromId() {
        for (TardisChameleonVariant variant : MODERN) {
            assertSame(variant, TardisChameleonVariant.fromId(variant.getId()));
        }
    }

    @Test
    void modernVariantsAreAppendedAfterTheDoctorBoxes() {
        TardisChameleonVariant[] all = TardisChameleonVariant.values();
        int first = TardisChameleonVariant.SEVENTH_DOCTOR_BOX.ordinal() + 1;
        for (int i = 0; i < MODERN.size(); i++) {
            assertSame(MODERN.get(i), all[first + i], "saves store enum names/order must stay stable");
        }
    }

    @Test
    void meshAndTextureExistAndMatch() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            String id = variant.getId().getPath();
            Path model = ASSETS.resolve("models/entity/" + id + ".json");
            assertTrue(Files.isRegularFile(model), "missing model " + model);

            JsonObject json = JsonParser.parseString(Files.readString(model, StandardCharsets.UTF_8)).getAsJsonObject();
            Identifier texture = Identifier.parse(json.get("texture").getAsString());
            assertEquals(DWMReference.MOD_ID, texture.getNamespace());

            Path png = ASSETS.resolve(texture.getPath());
            assertTrue(Files.isRegularFile(png), "missing texture " + png);
            try (InputStream in = Files.newInputStream(png)) {
                var image = ImageIO.read(in);
                assertNotNull(image);
                assertEquals(json.get("texture_width").getAsInt(), image.getWidth());
                assertEquals(json.get("texture_height").getAsInt(), image.getHeight());
            }
        }
    }
}
