package com.adamkali.dwm.datagen;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorItemModelAssemblerTest {
    private static final double DELTA = 1e-6;
    private static final Path BLOCK_MODELS = Path.of("src/client/resources/assets/dwm/models/block");

    @Test
    void stacksSegmentsWithYOffsets() {
        JSONObject stacked = DoorItemModelAssembler.assembleTemplate(List.of(
                segment(0, 16),
                segment(0, 16)
        ));
        JSONArray elements = stacked.getJSONArray("elements");
        assertEquals(2, elements.length());
        assertEquals(0.0, elements.getJSONObject(0).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(16.0, elements.getJSONObject(0).getJSONArray("to").getDouble(1), DELTA);
        assertEquals(16.0, elements.getJSONObject(1).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(32.0, elements.getJSONObject(1).getJSONArray("to").getDouble(1), DELTA);
    }

    @Test
    void fitsThreeSegmentsIntoElementBounds() {
        JSONObject stacked = DoorItemModelAssembler.assembleTemplate(List.of(
                segment(0, 16),
                segment(0, 16),
                segment(0, 16)
        ));
        JSONArray elements = stacked.getJSONArray("elements");
        double fit = 32.0 / 48.0;
        assertEquals(0.0, elements.getJSONObject(0).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(16.0 * fit, elements.getJSONObject(0).getJSONArray("to").getDouble(1), DELTA);
        assertEquals(16.0 * fit, elements.getJSONObject(1).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(32.0 * fit, elements.getJSONObject(1).getJSONArray("to").getDouble(1), DELTA);
        assertEquals(32.0 * fit, elements.getJSONObject(2).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(32.0, elements.getJSONObject(2).getJSONArray("to").getDouble(1), DELTA);
        DoorItemModelAssembler.Aabb aabb = DoorItemModelAssembler.aabb(elements);
        assertTrue(aabb.maxY() <= DoorItemModelAssembler.MAX_ELEMENT_COORD + DELTA);
        assertCoordsInBounds(elements);
    }

    @Test
    void twoSegmentDoorKeepsHeight32() {
        JSONObject stacked = DoorItemModelAssembler.assembleTemplate(List.of(segment(0, 16), segment(0, 16)));
        JSONArray elements = stacked.getJSONArray("elements");
        assertEquals(0.0, elements.getJSONObject(0).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(16.0, elements.getJSONObject(1).getJSONArray("from").getDouble(1), DELTA);
        assertEquals(32.0, elements.getJSONObject(1).getJSONArray("to").getDouble(1), DELTA);
        DoorItemModelAssembler.Aabb aabb = DoorItemModelAssembler.aabb(elements);
        assertEquals(32.0, aabb.maxY(), DELTA);
        assertCoordsInBounds(elements);
    }

    @Test
    void displayUsesBlockLikeRecipe() {
        JSONObject stacked = DoorItemModelAssembler.assembleTemplate(List.of(segment(0, 16), segment(0, 16)));
        assertEquals("front", stacked.getString("gui_light"));
        JSONObject display = stacked.getJSONObject("display");
        assertFalse(display.has("thirdperson_lefthand"));
        assertRotation(display.getJSONObject("gui"), 0, 270, 0);
        assertRotation(display.getJSONObject("fixed"), 0, 90, 0);
        assertRotation(display.getJSONObject("ground"), 0, 90, 0);
        assertRotation(display.getJSONObject("thirdperson_righthand"), 75, 90, 0);
        assertRotation(display.getJSONObject("firstperson_righthand"), 0, 90, 0);
        assertRotation(display.getJSONObject("firstperson_lefthand"), 0, 270, 0);

        assertUniformScale(display.getJSONObject("gui"), 0.625);
        assertUniformScale(display.getJSONObject("fixed"), 0.5);
        assertUniformScale(display.getJSONObject("ground"), 0.25);
        assertUniformScale(display.getJSONObject("thirdperson_righthand"), 0.375);
        assertUniformScale(display.getJSONObject("firstperson_righthand"), 0.40);
        assertUniformScale(display.getJSONObject("firstperson_lefthand"), 0.40);

        JSONArray elements = stacked.getJSONArray("elements");
        DoorItemModelAssembler.Aabb aabb = DoorItemModelAssembler.aabb(elements);
        assertEquals(DoorItemModelAssembler.ITEM_CENTER, (aabb.minX() + aabb.maxX()) / 2.0, DELTA);
        assertEquals(DoorItemModelAssembler.ITEM_CENTER, (aabb.minZ() + aabb.maxZ()) / 2.0, DELTA);
        assertTranslation(display.getJSONObject("gui"), 0.0, -5.0, 0.0);
        assertTranslation(display.getJSONObject("fixed"), 0.0, -4.0, 0.0);
        assertTranslation(display.getJSONObject("ground"), 0.0, 3.0, 0.0);
        assertTranslation(display.getJSONObject("thirdperson_righthand"), 0.0, 2.5, 0.0);
        assertTranslation(display.getJSONObject("firstperson_righthand"), 0.0, 0.0, 0.0);
        assertTranslation(display.getJSONObject("firstperson_lefthand"), 0.0, 0.0, 0.0);
    }

    @Test
    void displayScaleClampsToMinimum() {
        assertEquals(0.15, DoorItemModelAssembler.displayScale(0.25, 0.15, 32.0), DELTA);
        assertEquals(0.3125, DoorItemModelAssembler.displayScale(0.625, 0.2, 32.0), DELTA);
    }

    @Test
    void resolveBlockModelsDirFindsAshWrapper() {
        Path dir = DoorItemModelAssembler.resolveBlockModelsDir();
        assertTrue(java.nio.file.Files.isRegularFile(dir.resolve("ash_door_bottom_left.json")));
    }

    @Test
    void assembleAshUsesAshTemplateAndBlockTexture() throws Exception {
        DoorItemModelAssembler.AssembledDoorItem assembled =
                DoorItemModelAssembler.assemble(BLOCK_MODELS, "ash");
        assertEquals("ash", assembled.shapeId());
        assertEquals("item/template_ash_door_item", assembled.templateModelPath());
        assertEquals("dwm:item/template_ash_door_item", assembled.wrapper().getString("parent"));
        assertEquals("dwm:block/ash_door", assembled.wrapper().getJSONObject("textures").getString("door"));
        JSONArray elements = assembled.template().getJSONArray("elements");
        assertTrue(elements.length() > 1);
        assertEquals(0.0, minY(elements), DELTA);
        assertEquals(32.0, maxY(elements), DELTA);
        assertCoordsInBounds(elements);
        DoorItemModelAssembler.Aabb aabb = DoorItemModelAssembler.aabb(elements);
        assertEquals(DoorItemModelAssembler.ITEM_CENTER, (aabb.minX() + aabb.maxX()) / 2.0, 1e-6);
        assertEquals(DoorItemModelAssembler.ITEM_CENTER, (aabb.minZ() + aabb.maxZ()) / 2.0, 1e-6);
        assertFalse(assembled.template().getJSONObject("display").has("thirdperson_lefthand"));
    }

    @Test
    void assembleDarkAshReusesAshShape() throws Exception {
        DoorItemModelAssembler.AssembledDoorItem assembled =
                DoorItemModelAssembler.assemble(BLOCK_MODELS, "dark_ash");
        assertEquals("ash", assembled.shapeId());
        assertEquals("dwm:block/dark_ash_door", assembled.wrapper().getJSONObject("textures").getString("door"));
    }

    @Test
    void assembleCardinalFitsThreeSegments() throws Exception {
        DoorItemModelAssembler.AssembledDoorItem assembled =
                DoorItemModelAssembler.assemble(BLOCK_MODELS, "cardinal");
        assertEquals("cardinal", assembled.shapeId());
        assertEquals("item/template_cardinal_door_item", assembled.templateModelPath());
        assertEquals("dwm:block/cardinal_door", assembled.wrapper().getJSONObject("textures").getString("door"));
        JSONArray elements = assembled.template().getJSONArray("elements");
        DoorItemModelAssembler.Aabb aabb = DoorItemModelAssembler.aabb(elements);
        assertTrue(aabb.maxY() <= DoorItemModelAssembler.MAX_ELEMENT_COORD + DELTA);
        assertCoordsInBounds(elements);
        assertUniformScale(assembled.template().getJSONObject("display").getJSONObject("gui"), 0.625);
    }

    private static JSONObject segment(double y0, double y1) {
        JSONArray from = new JSONArray();
        from.put(0.0);
        from.put(y0);
        from.put(0.0);
        JSONArray to = new JSONArray();
        to.put(1.0);
        to.put(y1);
        to.put(16.0);
        JSONObject element = new JSONObject();
        element.put("from", from);
        element.put("to", to);
        JSONObject model = new JSONObject();
        JSONArray elements = new JSONArray();
        elements.put(element);
        model.put("elements", elements);
        return model;
    }

    private static void assertRotation(JSONObject context, double x, double y, double z) {
        JSONArray rotation = context.getJSONArray("rotation");
        assertEquals(x, rotation.getDouble(0), DELTA);
        assertEquals(y, rotation.getDouble(1), DELTA);
        assertEquals(z, rotation.getDouble(2), DELTA);
    }

    private static void assertTranslation(JSONObject context, double x, double y, double z) {
        JSONArray translation = context.getJSONArray("translation");
        assertEquals(x, translation.getDouble(0), DELTA);
        assertEquals(y, translation.getDouble(1), DELTA);
        assertEquals(z, translation.getDouble(2), DELTA);
    }

    private static void assertUniformScale(JSONObject context, double expected) {
        JSONArray scale = context.getJSONArray("scale");
        assertEquals(expected, scale.getDouble(0), DELTA);
        assertEquals(expected, scale.getDouble(1), DELTA);
        assertEquals(expected, scale.getDouble(2), DELTA);
    }

    private static void assertCoordsInBounds(JSONArray elements) {
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            assertVecInBounds(element.getJSONArray("from"));
            assertVecInBounds(element.getJSONArray("to"));
        }
    }

    private static void assertVecInBounds(JSONArray vec) {
        for (int i = 0; i < vec.length(); i++) {
            double value = vec.getDouble(i);
            assertTrue(value >= DoorItemModelAssembler.MIN_ELEMENT_COORD - DELTA, "coord " + value);
            assertTrue(value <= DoorItemModelAssembler.MAX_ELEMENT_COORD + DELTA, "coord " + value);
        }
    }

    private static double minY(JSONArray elements) {
        return DoorItemModelAssembler.aabb(elements).minY();
    }

    private static double maxY(JSONArray elements) {
        return DoorItemModelAssembler.aabb(elements).maxY();
    }
}
