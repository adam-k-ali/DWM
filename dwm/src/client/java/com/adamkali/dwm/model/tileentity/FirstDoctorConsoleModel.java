// Made with Blockbench (converted from first_base_console.bbmodel)
// Exported for Minecraft version 1.17+ for Yarn

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

public class FirstDoctorConsoleModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_console"), "main");

    /** Peak vertical travel of {@code time_rotor} in model units while in flight. */
    public static final float ROTOR_BOB_AMPLITUDE = 6.5f;

    /** Radians per tick for one full up/down cycle (~3 seconds at 20 TPS). */
    public static final float ROTOR_BOB_SPEED = (float) (Math.PI / 30.0);

    /** Speed multiplier while traveling with stabilisers off. Amplitude is unchanged. */
    public static final float ROTOR_BOB_UNSTABILISED_SPEED_FACTOR = 1.5f;

    /** Radians per tick for {@code Time_middle} Y-spin (~2 seconds per revolution at 20 TPS). */
    public static final float ROTOR_SPIN_SPEED = (float) (Math.PI * 2.0 / 40.0);

    public FirstDoctorConsoleModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }

    /**
     * Vertical pivot offset for the time rotor. Returns 0 when inactive.
     * When active, dips from the rest pose down to {@code -ROTOR_BOB_AMPLITUDE} and back —
     * never above the initial position.
     *
     * @param timeTicks age + tickDelta (or any continuous time base)
     * @param active    whether the TARDIS is traveling
     */
    public static float rotorBobOffset(float timeTicks, boolean active) {
        return rotorBobOffset(timeTicks, active, true);
    }

    /**
     * Like {@link #rotorBobOffset(float, boolean)} with optional unstabilised speed boost.
     * Amplitude stays {@link #ROTOR_BOB_AMPLITUDE}; only angular speed increases when
     * {@code stabilisersEnabled} is false.
     */
    public static float rotorBobOffset(float timeTicks, boolean active, boolean stabilisersEnabled) {
        if (!active) {
            return 0.0f;
        }
        float speed = ROTOR_BOB_SPEED;
        if (!stabilisersEnabled) {
            speed *= ROTOR_BOB_UNSTABILISED_SPEED_FACTOR;
        }
        // cos: 1 → -1 → 1 maps to offset 0 → -amplitude → 0
        float downAmount = (1.0f - (float) Math.cos(timeTicks * speed)) * 0.5f;
        return -downAmount * ROTOR_BOB_AMPLITUDE;
    }


    /**
     * Y-axis spin for {@code Time_middle} in radians. Returns 0 when inactive.
     *
     * @param timeTicks age + tickDelta (or any continuous time base)
     * @param active    whether the TARDIS is traveling
     */
    public static float rotorSpinRadians(float timeTicks, boolean active) {
        return rotorSpinRadians(timeTicks, active, true);
    }

    /**
     * Like {@link #rotorSpinRadians(float, boolean)} with optional unstabilised speed boost.
     * Uses the same {@link #ROTOR_BOB_UNSTABILISED_SPEED_FACTOR} as the bob.
     */
    public static float rotorSpinRadians(float timeTicks, boolean active, boolean stabilisersEnabled) {
        if (!active) {
            return 0.0f;
        }
        float speed = ROTOR_SPIN_SPEED;
        if (!stabilisersEnabled) {
            speed *= ROTOR_BOB_UNSTABILISED_SPEED_FACTOR;
        }
        return timeTicks * speed;
    }
}
