package com.adamkali.dwm.tardis.data.model;

import com.adamkali.dwm.DWMReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the 2005/2010/2014/2018 exteriors: wired to an existing mesh, a texture that is a whole-number
 * upscale of the declared UV space, door leaves that fill the BOTI aperture, and UV nets inside the texture.
 */
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
                int w = json.get("texture_width").getAsInt();
                int h = json.get("texture_height").getAsInt();
                // The PNG may be a whole-number upscale of the declared UV space (like the Doctor boxes).
                assertEquals(0, image.getWidth() % w, id + " PNG width must be a multiple of texture_width");
                assertEquals(image.getWidth() / w * h, image.getHeight(), id + " PNG must keep the declared aspect");
            }
        }
    }

    @Test
    void doorLeavesAreRootPartsThatFillTheAperture() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            JsonObject json = readModel(variant);
            PortalAperture aperture = variant.getAperture();
            float ax0 = aperture.x0() * 16, ax1 = aperture.x1() * 16, ay0 = aperture.y0() * 16, ay1 = aperture.y1() * 16;
            for (String name : List.of("LeftDoor", "rightDoor")) {
                JsonObject door = rootPart(json, name);
                assertNotNull(door, variant + " needs a root " + name);
                JsonArray pivot = door.getAsJsonArray("pivot");
                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minH = Float.MAX_VALUE, maxH = -Float.MAX_VALUE;
                for (JsonElement e : door.getAsJsonArray("cubes")) {
                    JsonObject cube = e.getAsJsonObject();
                    JsonArray origin = cube.getAsJsonArray("origin");
                    JsonArray size = cube.getAsJsonArray("size");
                    float x0 = pivot.get(0).getAsFloat() + origin.get(0).getAsFloat();
                    float y0 = pivot.get(1).getAsFloat() + origin.get(1).getAsFloat();
                    minX = Math.min(minX, x0);
                    maxX = Math.max(maxX, x0 + size.get(0).getAsFloat());
                    // model y grows downward from the ground at y=24
                    maxH = Math.max(maxH, 24.0f - y0);
                    minH = Math.min(minH, 24.0f - y0 - size.get(1).getAsFloat());
                }
                assertEquals(ay0, minH, 0.01f, variant + " " + name + " bottom must meet the aperture");
                assertEquals(ay1, maxH, 0.01f, variant + " " + name + " top must meet the aperture");
                if (name.equals("LeftDoor")) {
                    assertEquals(ax1, maxX, 0.01f, variant + " LeftDoor must reach the hinge edge of the aperture");
                    assertTrue(minX >= -0.01f, variant + " LeftDoor must not cross the centre line");
                } else {
                    assertEquals(ax0, minX, 0.01f, variant + " rightDoor must reach the hinge edge of the aperture");
                    // the fixed leaf carries the centre astragal, which laps the meeting stile slightly
                    assertTrue(maxX <= 0.26f, variant + " rightDoor must stop at the centre astragal");
                }
            }
        }
    }

    @Test
    void everyCubeUnwrapFitsTheDeclaredTexture() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            JsonObject json = readModel(variant);
            int w = json.get("texture_width").getAsInt();
            int h = json.get("texture_height").getAsInt();
            for (JsonElement p : json.getAsJsonArray("parts")) {
                for (JsonElement e : p.getAsJsonObject().getAsJsonArray("cubes")) {
                    JsonObject cube = e.getAsJsonObject();
                    JsonArray uv = cube.getAsJsonArray("uv");
                    JsonArray size = cube.getAsJsonArray("size");
                    float sx = size.get(0).getAsFloat(), sy = size.get(1).getAsFloat(), sz = size.get(2).getAsFloat();
                    float u1 = uv.get(0).getAsFloat() + 2 * (sx + sz);
                    float v1 = uv.get(1).getAsFloat() + sz + sy;
                    assertTrue(u1 <= w + 1e-3f && v1 <= h + 1e-3f, variant + " cube net overflows the texture: " + cube);
                }
            }
        }
    }

    private static JsonObject readModel(TardisChameleonVariant variant) throws IOException {
        Path model = ASSETS.resolve("models/entity/" + variant.getId().getPath() + ".json");
        return JsonParser.parseString(Files.readString(model, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject rootPart(JsonObject json, String name) {
        for (JsonElement p : json.getAsJsonArray("parts")) {
            if (name.equals(p.getAsJsonObject().get("name").getAsString())) {
                return p.getAsJsonObject();
            }
        }
        return null;
    }
}
