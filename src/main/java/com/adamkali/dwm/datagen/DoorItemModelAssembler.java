package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stacks closed left-hinge custom door block segments into a single item model
 * and applies block-like {@code display} transforms.
 */
public final class DoorItemModelAssembler {
    public static final double SEGMENT_HEIGHT = 16.0;
    public static final double MIN_ELEMENT_COORD = -16.0;
    public static final double MAX_ELEMENT_COORD = 32.0;
    public static final double ITEM_CENTER = 8.0;
    /**
     * Closed left-hinge templates are west-facing (thin in X). Yaw 90° puts the door
     * face toward the camera with no extra 45° isometric.
     */
    public static final double DISPLAY_YAW_OFFSET = 90.0;

    private static final Pattern TEMPLATE_SHAPE = Pattern.compile("^dwm:block/template_(.+)_door_bottom_left$");
    private static final String[] SEGMENT_SUFFIXES = {"bottom_left", "middle_left", "top_left"};

    private DoorItemModelAssembler() {
    }

    public record Aabb(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public double width() {
            return maxX - minX;
        }

        public double height() {
            return maxY - minY;
        }

        public double depth() {
            return maxZ - minZ;
        }

        public double maxDim() {
            return Math.max(width(), Math.max(height(), depth()));
        }

        public double tx() {
            return ITEM_CENTER - (minX + maxX) / 2.0;
        }

        public double ty() {
            return ITEM_CENTER - maxY / 2.0;
        }

        public double tz() {
            return ITEM_CENTER - (minZ + maxZ) / 2.0;
        }
    }

    public record AssembledDoorItem(String woodId, String shapeId, JSONObject template, JSONObject wrapper) {
        public String templateModelPath() {
            return "item/template_" + shapeId + "_door_item";
        }

        public String itemModelPath() {
            return "item/" + woodId + "_door";
        }
    }

    public static AssembledDoorItem assemble(Path blockModelsDir, String woodId) throws IOException {
        JSONObject bottomWrapper = loadJson(segmentPath(blockModelsDir, woodId, "bottom_left"));
        String shapeId = shapeIdFromParent(bottomWrapper.getString("parent"));
        List<JSONObject> segments = loadSegments(blockModelsDir, woodId);
        JSONObject template = assembleTemplate(segments);
        String doorTexture = bottomWrapper.getJSONObject("textures").getString("door");
        JSONObject wrapper = wrapper(DWMReference.MOD_ID + ":item/template_" + shapeId + "_door_item", doorTexture);
        return new AssembledDoorItem(woodId, shapeId, template, wrapper);
    }

    /**
     * Datagen often runs with {@code user.dir} under {@code run/} or {@code build/}, not the repo root.
     * Walk parents until the hand-authored door wrappers are found.
     */
    public static Path resolveBlockModelsDir() {
        Path[] relatives = {
                Path.of("dwm-common/src/client/resources/assets/dwm/models/block"),
                Path.of("src/client/resources/assets/dwm/models/block")
        };
        Path dir = Path.of(System.getProperty("user.dir", "")).toAbsolutePath().normalize();
        while (dir != null) {
            for (Path relative : relatives) {
                Path candidate = dir.resolve(relative);
                if (Files.isRegularFile(candidate.resolve("ash_door_bottom_left.json"))) {
                    return candidate;
                }
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(
                "Could not locate custom door block models from user.dir=" + System.getProperty("user.dir")
        );
    }

    public static String shapeIdFromParent(String parent) {
        Matcher matcher = TEMPLATE_SHAPE.matcher(parent);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Expected dwm:block/template_{shape}_door_bottom_left, got: " + parent);
        }
        return matcher.group(1);
    }

    public static JSONObject wrapper(String parent, String doorTexture) {
        JSONObject textures = new JSONObject();
        textures.put("door", doorTexture);
        JSONObject wrapper = new JSONObject();
        wrapper.put("parent", parent);
        wrapper.put("textures", textures);
        return wrapper;
    }

    public static JSONObject assembleTemplate(List<JSONObject> segmentModels) {
        if (segmentModels == null || segmentModels.isEmpty()) {
            throw new IllegalArgumentException("At least one door segment is required");
        }
        JSONArray elements = new JSONArray();
        for (int i = 0; i < segmentModels.size(); i++) {
            JSONObject segment = segmentModels.get(i);
            JSONArray segmentElements = segment.getJSONArray("elements");
            double yOffset = SEGMENT_HEIGHT * i;
            for (int e = 0; e < segmentElements.length(); e++) {
                JSONObject copy = new JSONObject(segmentElements.getJSONObject(e).toString());
                offsetY(copy, yOffset);
                elements.put(copy);
            }
        }
        double fitScale = fitScale(elements);
        if (fitScale != 1.0) {
            scaleElements(elements, fitScale);
        }
        Aabb aabb = aabb(elements);
        offsetXZ(elements, aabb.tx(), aabb.tz());
        aabb = aabb(elements);
        JSONObject template = new JSONObject();
        template.put("ambientocclusion", false);
        // Face-on inventory pose; side lighting is aimed at isometric blocks and shades this slab.
        template.put("gui_light", "front");
        JSONObject textures = new JSONObject();
        textures.put("particle", "#door");
        template.put("textures", textures);
        template.put("elements", elements);
        template.put("display", display(aabb));
        return template;
    }

    public static JSONObject display(Aabb aabb) {
        // Height-based maxDim halves the 16-wide door face vs a block item. Scale to the face.
        double faceDim = Math.max(Math.max(aabb.width(), aabb.depth()), 1.0);
        double yawTx = -aabb.tz();
        double tyModel = aabb.ty();
        double yawTz = aabb.tx();
        JSONObject display = new JSONObject();
        double guiScale = displayScale(0.625, 0.2, faceDim);
        double fixedScale = displayScale(0.5, 0.2, faceDim);
        double groundScale = displayScale(0.25, 0.15, faceDim);
        double thirdpersonScale = displayScale(0.375, 0.15, faceDim);
        double firstpersonScale = displayScale(0.40, 0.15, faceDim);
        display.put("gui", context(
                new double[]{0, DISPLAY_YAW_OFFSET + 180.0, 0},
                yawTx * guiScale,
                tyModel * guiScale,
                yawTz * guiScale,
                guiScale
        ));
        display.put("fixed", context(
                new double[]{0, DISPLAY_YAW_OFFSET, 0},
                yawTx * fixedScale,
                tyModel * fixedScale,
                yawTz * fixedScale,
                fixedScale
        ));
        display.put("ground", context(
                new double[]{0, DISPLAY_YAW_OFFSET, 0},
                yawTx * groundScale,
                3.0,
                yawTz * groundScale,
                groundScale
        ));
        display.put("thirdperson_righthand", context(
                new double[]{75, DISPLAY_YAW_OFFSET, 0},
                yawTx * thirdpersonScale,
                2.5,
                yawTz * thirdpersonScale,
                thirdpersonScale
        ));
        display.put("firstperson_righthand", context(
                new double[]{0, DISPLAY_YAW_OFFSET, 0},
                yawTx * firstpersonScale,
                0.0,
                yawTz * firstpersonScale,
                firstpersonScale
        ));
        display.put("firstperson_lefthand", context(
                new double[]{0, DISPLAY_YAW_OFFSET + 180.0, 0},
                yawTx * firstpersonScale,
                0.0,
                yawTz * firstpersonScale,
                firstpersonScale
        ));
        return display;
    }

    public static double displayScale(double vanillaScale, double minScale, double maxDim) {
        double scale = vanillaScale * 16.0 / maxDim;
        return Math.min(vanillaScale, Math.max(minScale, scale));
    }

    public static Aabb aabb(JSONArray elements) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            minX = Math.min(minX, Math.min(element.getJSONArray("from").getDouble(0), element.getJSONArray("to").getDouble(0)));
            minY = Math.min(minY, Math.min(element.getJSONArray("from").getDouble(1), element.getJSONArray("to").getDouble(1)));
            minZ = Math.min(minZ, Math.min(element.getJSONArray("from").getDouble(2), element.getJSONArray("to").getDouble(2)));
            maxX = Math.max(maxX, Math.max(element.getJSONArray("from").getDouble(0), element.getJSONArray("to").getDouble(0)));
            maxY = Math.max(maxY, Math.max(element.getJSONArray("from").getDouble(1), element.getJSONArray("to").getDouble(1)));
            maxZ = Math.max(maxZ, Math.max(element.getJSONArray("from").getDouble(2), element.getJSONArray("to").getDouble(2)));
        }
        return new Aabb(minX, minY, minZ, maxX, maxY, maxZ);
    }

    static List<JSONObject> loadSegments(Path blockModelsDir, String woodId) throws IOException {
        List<JSONObject> segments = new ArrayList<>();
        for (String suffix : SEGMENT_SUFFIXES) {
            Path wrapperPath = segmentPath(blockModelsDir, woodId, suffix);
            if (!Files.isRegularFile(wrapperPath)) {
                continue;
            }
            JSONObject wrapper = loadJson(wrapperPath);
            segments.add(resolveModel(blockModelsDir, wrapper));
        }
        if (segments.isEmpty()) {
            throw new IllegalStateException("No closed left-hinge door segments for " + woodId);
        }
        return segments;
    }

    static JSONObject resolveModel(Path blockModelsDir, JSONObject model) throws IOException {
        if (model.has("elements")) {
            return model;
        }
        if (!model.has("parent")) {
            throw new IllegalArgumentException("Door model has neither elements nor parent");
        }
        Path parentPath = resolveParentPath(blockModelsDir, model.getString("parent"));
        return resolveModel(blockModelsDir, loadJson(parentPath));
    }

    static Path resolveParentPath(Path blockModelsDir, String parent) {
        String path = parent.contains(":") ? parent.substring(parent.indexOf(':') + 1) : parent;
        if (path.startsWith("block/")) {
            path = path.substring("block/".length());
        }
        return blockModelsDir.resolve(path + ".json");
    }

    static Path segmentPath(Path blockModelsDir, String woodId, String suffix) {
        return blockModelsDir.resolve(woodId + "_door_" + suffix + ".json");
    }

    static JSONObject loadJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return new JSONObject(new JSONTokener(reader));
        }
    }

    private static void offsetY(JSONObject element, double yOffset) {
        if (yOffset == 0.0) {
            return;
        }
        addY(element.getJSONArray("from"), yOffset);
        addY(element.getJSONArray("to"), yOffset);
        if (element.has("rotation")) {
            JSONObject rotation = element.getJSONObject("rotation");
            if (rotation.has("origin")) {
                addY(rotation.getJSONArray("origin"), yOffset);
            }
        }
    }

    private static void addY(JSONArray vec, double yOffset) {
        vec.put(1, vec.getDouble(1) + yOffset);
    }

    private static void offsetXZ(JSONArray elements, double dx, double dz) {
        if (dx == 0.0 && dz == 0.0) {
            return;
        }
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            addXZ(element.getJSONArray("from"), dx, dz);
            addXZ(element.getJSONArray("to"), dx, dz);
            if (element.has("rotation")) {
                JSONObject rotation = element.getJSONObject("rotation");
                if (rotation.has("origin")) {
                    addXZ(rotation.getJSONArray("origin"), dx, dz);
                }
            }
        }
    }

    private static void addXZ(JSONArray vec, double dx, double dz) {
        vec.put(0, vec.getDouble(0) + dx);
        vec.put(2, vec.getDouble(2) + dz);
    }

    static double fitScale(JSONArray elements) {
        Aabb aabb = aabb(elements);
        double scale = 1.0;
        scale = capPositive(scale, aabb.maxX());
        scale = capPositive(scale, aabb.maxY());
        scale = capPositive(scale, aabb.maxZ());
        scale = capNegative(scale, aabb.minX());
        scale = capNegative(scale, aabb.minY());
        scale = capNegative(scale, aabb.minZ());
        return scale;
    }

    private static double capPositive(double scale, double value) {
        if (value > MAX_ELEMENT_COORD) {
            return Math.min(scale, MAX_ELEMENT_COORD / value);
        }
        return scale;
    }

    private static double capNegative(double scale, double value) {
        if (value < MIN_ELEMENT_COORD) {
            return Math.min(scale, MIN_ELEMENT_COORD / value);
        }
        return scale;
    }

    private static void scaleElements(JSONArray elements, double scale) {
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            scaleVec(element.getJSONArray("from"), scale);
            scaleVec(element.getJSONArray("to"), scale);
            if (element.has("rotation")) {
                JSONObject rotation = element.getJSONObject("rotation");
                if (rotation.has("origin")) {
                    scaleVec(rotation.getJSONArray("origin"), scale);
                }
            }
        }
    }

    private static void scaleVec(JSONArray vec, double scale) {
        for (int i = 0; i < vec.length(); i++) {
            vec.put(i, vec.getDouble(i) * scale);
        }
    }

    private static JSONObject context(double[] rotation, double tx, double ty, double tz, double scale) {
        JSONObject entry = new JSONObject();
        entry.put("rotation", vec(rotation[0], rotation[1], rotation[2]));
        entry.put("translation", vec(tx, ty, tz));
        entry.put("scale", vec(scale, scale, scale));
        return entry;
    }

    private static JSONArray vec(double x, double y, double z) {
        JSONArray arr = new JSONArray();
        arr.put(canonicalZero(x));
        arr.put(canonicalZero(y));
        arr.put(canonicalZero(z));
        return arr;
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }
}
