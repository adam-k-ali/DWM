package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.model.json.EntityModelCube;
import com.adamkali.dwm.model.json.EntityModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The laser leaves where each casing's gunstick tip actually is. */
class DalekMuzzleTest {
    /** Within a pixel: the Paradigm muzzle sits half a pixel behind its barrel cap. */
    private static final float TOLERANCE = 1.0F / 16.0F;

    private static final Map<DalekChassis, ModelLayerLocation> MESHES = Map.of(
            DalekChassis.CLASSIC_1963, DalekModel.LAYER_LOCATION,
            DalekChassis.TIME_WAR, TimeWarDalekModel.LAYER_LOCATION,
            DalekChassis.PARADIGM, ParadigmDalekModel.LAYER_LOCATION,
            DalekChassis.SPECIAL_WEAPONS, SpecialWeaponsDalekModel.LAYER_LOCATION
    );

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void chassisMuzzleMatchesTheGunstickTip() {
        for (DalekChassis chassis : DalekChassis.values()) {
            float[] tip = gunTip(MESHES.get(chassis));
            assertEquals((24.0F - tip[1]) / 16.0F, chassis.gunHeight(), TOLERANCE, chassis + " gun height");
            assertEquals(-tip[2] / 16.0F, chassis.gunForward(), TOLERANCE, chassis + " gun forward");
            assertEquals(tip[0] / 16.0F, chassis.gunSide(), TOLERANCE, chassis + " gun side");
        }
    }

    /** {@code [x, y, z]} model units: the gun pivot, pushed out to the front of its foremost cube. */
    private static float[] gunTip(ModelLayerLocation layer) {
        EntityModelPart shoulders = child(DalekMeshes.file(layer).parts(), "shoulders");
        EntityModelPart gun = child(shoulders.children(), "gun");
        float front = 0.0F;
        for (EntityModelPart part : gun.children()) {
            for (EntityModelCube cube : part.cubes()) {
                front = Math.min(front, cube.origin().z());
            }
        }
        for (EntityModelCube cube : gun.cubes()) {
            front = Math.min(front, cube.origin().z());
        }
        return new float[]{
                shoulders.pivot().x() + gun.pivot().x(),
                shoulders.pivot().y() + gun.pivot().y(),
                shoulders.pivot().z() + gun.pivot().z() + front
        };
    }

    private static EntityModelPart child(java.util.List<EntityModelPart> parts, String name) {
        return parts.stream().filter(part -> part.name().equals(name)).findFirst().orElseThrow();
    }
}
