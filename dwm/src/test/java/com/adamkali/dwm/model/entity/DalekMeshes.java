package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.model.json.EntityModelCube;
import com.adamkali.dwm.model.json.EntityModelFile;
import com.adamkali.dwm.model.json.EntityModelJson;
import com.adamkali.dwm.model.json.EntityModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;

import java.util.ArrayList;
import java.util.List;

/** Measurements over a Dalek JSON mesh, shared by the casing tests. */
final class DalekMeshes {
    private DalekMeshes() {
    }

    /** {@code [minY, maxY, minX, maxX]} in model units over every cube, ignoring rest rotations. */
    static float[] bounds(ModelLayerLocation layer) {
        float[] out = {Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
        for (EntityModelPart part : file(layer).parts()) {
            accumulate(part, 0.0F, 0.0F, out);
        }
        return out;
    }

    static float height(ModelLayerLocation layer) {
        float[] b = bounds(layer);
        return b[1] - b[0];
    }

    static float width(ModelLayerLocation layer) {
        float[] b = bounds(layer);
        return b[3] - b[2];
    }

    static List<EntityModelCube> cubes(ModelLayerLocation layer) {
        List<EntityModelCube> out = new ArrayList<>();
        for (EntityModelPart part : file(layer).parts()) {
            collect(part, out);
        }
        return out;
    }

    static EntityModelFile file(ModelLayerLocation layer) {
        return EntityModelJson.loadClasspathFile(layer.model());
    }

    private static void accumulate(EntityModelPart part, float parentX, float parentY, float[] out) {
        float x = parentX + part.pivot().x();
        float y = parentY + part.pivot().y();
        for (EntityModelCube cube : part.cubes()) {
            out[0] = Math.min(out[0], y + cube.origin().y());
            out[1] = Math.max(out[1], y + cube.origin().y() + cube.size().y());
            out[2] = Math.min(out[2], x + cube.origin().x());
            out[3] = Math.max(out[3], x + cube.origin().x() + cube.size().x());
        }
        for (EntityModelPart child : part.children()) {
            accumulate(child, x, y, out);
        }
    }

    private static void collect(EntityModelPart part, List<EntityModelCube> out) {
        out.addAll(part.cubes());
        for (EntityModelPart child : part.children()) {
            collect(child, out);
        }
    }
}
