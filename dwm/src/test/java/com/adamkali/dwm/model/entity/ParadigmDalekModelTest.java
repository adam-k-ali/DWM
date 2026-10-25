package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelCube;
import com.adamkali.dwm.model.json.EntityModelFile;
import com.adamkali.dwm.model.json.EntityModelJson;
import com.adamkali.dwm.model.json.EntityModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParadigmDalekModelTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void bodyLayerKeepsTheDalekPartNames() {
        ModelPart root = EntityModelJson.loadClasspath(ParadigmDalekModel.LAYER_LOCATION.model()).bakeRoot();
        assertTrue(root.hasChild("skirt"));
        assertTrue(root.hasChild("shoulders"));
        assertTrue(root.getChild("shoulders").hasChild("gun"));
        assertTrue(root.getChild("shoulders").hasChild("plunger"));
        assertTrue(root.hasChild("neck"));
        assertTrue(root.hasChild("head"));
        assertTrue(root.getChild("head").hasChild("eyestalk"));
        assertTrue(root.getChild("head").hasChild("light_left"));
        assertTrue(root.getChild("head").hasChild("light_right"));
    }

    @Test
    void layerLocationUsesParadigmDalekId() {
        assertEquals("paradigm_dalek", ParadigmDalekModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", ParadigmDalekModel.LAYER_LOCATION.layer());
    }

    @Test
    void meshIsTallerAndWiderThanTheClassicDalek() {
        float[] paradigm = bounds(ParadigmDalekModel.LAYER_LOCATION);
        float[] classic = bounds(DalekModel.LAYER_LOCATION);
        float paradigmHeight = paradigm[1] - paradigm[0];
        float classicHeight = classic[1] - classic[0];
        assertTrue(paradigmHeight > classicHeight, paradigmHeight + " vs " + classicHeight);
        // Matches the 2.4-block hitbox to within a pixel.
        assertEquals(2.4F * 16.0F, paradigmHeight, 1.0F);
        assertTrue(paradigm[3] - paradigm[2] > classic[3] - classic[2], "Paradigm skirt should be wider");
    }

    /** {@code [minY, maxY, minX, maxX]} in model units over every cube, ignoring rest rotations. */
    private static float[] bounds(ModelLayerLocation layer) {
        EntityModelFile file = EntityModelJson.loadClasspathFile(layer.model());
        float[] out = {Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
        for (EntityModelPart part : file.parts()) {
            accumulate(part, 0.0F, 0.0F, out);
        }
        return out;
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
}
