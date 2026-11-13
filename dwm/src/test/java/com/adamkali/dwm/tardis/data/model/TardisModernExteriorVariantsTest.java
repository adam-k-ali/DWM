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
 * upscale of the declared UV space, door leaves that fill the BOTI aperture, UV nets inside the texture,
 * geometry on the texture-pixel grid with every face painted, the same envelope as the Doctor boxes, and a
 * roof that steps in from the corner posts instead of overhanging them.
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

    /** One texture pixel in model units (4 px per unit, like the 2nd-7th Doctor boxes). */
    private static final float GRID = 0.25f;

    @Test
    void everyCubeSitsOnThePixelGrid() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            forEachCube(readModel(variant), (pivot, cube) -> {
                JsonArray origin = cube.getAsJsonArray("origin");
                JsonArray size = cube.getAsJsonArray("size");
                for (int i = 0; i < 3; i++) {
                    float o = origin.get(i).getAsFloat() / GRID;
                    float s = size.get(i).getAsFloat() / GRID;
                    assertEquals(Math.round(o), o, 1e-3f, variant + " cube origin is off the pixel grid: " + cube);
                    assertEquals(Math.round(s), s, 1e-3f, variant + " cube size is off the pixel grid: " + cube);
                    assertTrue(s >= 1 - 1e-3f, variant + " cube is thinner than a texture pixel: " + cube);
                }
            });
        }
    }

    @Test
    void everyCubeFaceIsFullyPainted() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            JsonObject json = readModel(variant);
            Path png = ASSETS.resolve(Identifier.parse(json.get("texture").getAsString()).getPath());
            java.awt.image.BufferedImage image;
            try (InputStream in = Files.newInputStream(png)) {
                image = ImageIO.read(in);
            }
            int px = image.getWidth() / json.get("texture_width").getAsInt();
            forEachCube(json, (pivot, cube) -> {
                JsonArray uv = cube.getAsJsonArray("uv");
                JsonArray size = cube.getAsJsonArray("size");
                float u = uv.get(0).getAsFloat(), v = uv.get(1).getAsFloat();
                float w = size.get(0).getAsFloat(), h = size.get(1).getAsFloat(), d = size.get(2).getAsFloat();
                // vanilla box-UV net: {u0, v0, width, height} for down, up, west, north, east, south
                float[][] faces = {
                        {u + d, v, w, d}, {u + d + w, v, w, d},
                        {u, v + d, d, h}, {u + d, v + d, w, h}, {u + d + w, v + d, d, h}, {u + 2 * d + w, v + d, w, h}};
                for (float[] f : faces) {
                    int x0 = Math.round(f[0] * px), y0 = Math.round(f[1] * px);
                    int x1 = Math.round((f[0] + f[2]) * px), y1 = Math.round((f[1] + f[3]) * px);
                    for (int y = y0; y < y1; y++) {
                        for (int x = x0; x < x1; x++) {
                            if ((image.getRGB(x, y) >>> 24) != 0xFF) {
                                fail(variant + " has an unpainted texel at " + x + "," + y + " under " + cube);
                            }
                        }
                    }
                }
            });
        }
    }

    @Test
    void envelopeMatchesTheDoctorBoxes() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            float[] reach = {0, 0}; // {footprint half-width, height}
            forEachCube(readModel(variant), (pivot, cube) -> {
                JsonArray origin = cube.getAsJsonArray("origin");
                JsonArray size = cube.getAsJsonArray("size");
                // walls are whole quarter turns about the centre, so max(|x|, |z|) is rotation-proof
                for (int axis : new int[] {0, 2}) {
                    float a = pivot.get(axis).getAsFloat() + origin.get(axis).getAsFloat();
                    reach[0] = Math.max(reach[0], Math.max(Math.abs(a), Math.abs(a + size.get(axis).getAsFloat())));
                }
                reach[1] = Math.max(reach[1], 24.0f - pivot.get(1).getAsFloat() - origin.get(1).getAsFloat());
            });
            assertEquals(7.5f, reach[0], 0.01f, variant + " footprint must match the Doctor boxes");
            assertTrue(reach[1] <= 30.5f, variant + " is taller than the Doctor boxes allow: " + reach[1]);
        }
    }

    @Test
    void roofStepsInsideTheCornerPostsWithoutOverhang() throws IOException {
        for (TardisChameleonVariant variant : MODERN) {
            JsonObject json = readModel(variant);
            float posts = reach(rootPart(json, "posts"));
            float roof = reach(rootPart(json, "roof"));
            assertTrue(roof < posts, variant + " roof (±" + roof + ") must not overhang the posts (±" + posts + ")");
            assertTrue(reach(rootPart(json, "lamp")) < roof, variant + " lamp must sit inside the roof");
        }
    }

    /** Largest |x| or |z| any cube of the part reaches; the part must pivot on the box's centre line. */
    private static float reach(JsonObject part) {
        assertNotNull(part);
        JsonArray pivot = part.getAsJsonArray("pivot");
        assertEquals(0f, pivot.get(0).getAsFloat(), 1e-6f);
        assertEquals(0f, pivot.get(2).getAsFloat(), 1e-6f);
        float r = 0;
        for (JsonElement e : part.getAsJsonArray("cubes")) {
            JsonObject cube = e.getAsJsonObject();
            JsonArray origin = cube.getAsJsonArray("origin");
            JsonArray size = cube.getAsJsonArray("size");
            for (int axis : new int[] {0, 2}) {
                float a = origin.get(axis).getAsFloat();
                r = Math.max(r, Math.max(Math.abs(a), Math.abs(a + size.get(axis).getAsFloat())));
            }
        }
        return r;
    }

    private static void forEachCube(JsonObject json, java.util.function.BiConsumer<JsonArray, JsonObject> action) {
        for (JsonElement p : json.getAsJsonArray("parts")) {
            JsonObject part = p.getAsJsonObject();
            assertFalse(part.has("children"), "generated exteriors keep a flat part tree");
            for (JsonElement e : part.getAsJsonArray("cubes")) {
                action.accept(part.getAsJsonArray("pivot"), e.getAsJsonObject());
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
