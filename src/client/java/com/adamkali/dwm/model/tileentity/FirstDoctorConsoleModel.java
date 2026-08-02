// Made with Blockbench (converted from first_base_console.bbmodel)
// Exported for Minecraft version 1.17+ for Yarn

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class FirstDoctorConsoleModel extends EntityModel<TardisRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "first_doctor_console"), "main");
    public static final Identifier TEXTURE_LOCATION = Identifier.of(DWMReference.MOD_ID, "textures/entity/first_white_base_console.png");

    /** Peak vertical travel of {@code time_rotor} in model units while in flight. */
    public static final float ROTOR_BOB_AMPLITUDE = 6.5f;

    /** Radians per tick for one full up/down cycle (~3 seconds at 20 TPS). */
    public static final float ROTOR_BOB_SPEED = (float) (Math.PI / 30.0);

    private final ModelPart timeRotor;

    public FirstDoctorConsoleModel(ModelPart root) {
        super(root);
        this.timeRotor = root.getChild("time_rotor");
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
        if (!active) {
            return 0.0f;
        }
        // cos: 1 → -1 → 1 maps to offset 0 → -amplitude → 0
        float downAmount = (1.0f - (float) Math.cos(timeTicks * ROTOR_BOB_SPEED)) * 0.5f;
        return -downAmount * ROTOR_BOB_AMPLITUDE;
    }

    public static float rotorPivotY(float bobOffset) {
        return bobOffset;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData Base = modelPartData.addChild("Base",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-6.0F, 20.0F, -5.0F, 12.0F, 11.0F, 10.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -5.0F, 0.0F));
        ModelPartData bone4 = Base.addChild("bone4",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-0.5F, 7.0F, 0.0F));
        ModelPartData CORNER = Base.addChild("CORNER",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 5.0F, 0.0F));
        ModelPartData bone32 = CORNER.addChild("bone32",
                ModelPartBuilder.create(),
                ModelTransform.of(-0.5F, -5.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        ModelPartData bone33 = bone32.addChild("bone33",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.5F, 0.0F, 0.0F));
        ModelPartData bone34 = bone33.addChild("bone34",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.5F, 1.096267F, -4.356726F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone35 = bone32.addChild("bone35",
                ModelPartBuilder.create(),
                ModelTransform.of(0.5F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData bone36 = bone35.addChild("bone36",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.25359F, 0.856375F, -4.642617F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone37 = bone32.addChild("bone37",
                ModelPartBuilder.create(),
                ModelTransform.of(0.5F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData bone38 = bone37.addChild("bone38",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.8F, 1.196267F, -4.356726F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone31 = CORNER.addChild("bone31",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, -5.0F, 0.0F));
        ModelPartData bone26 = bone31.addChild("bone26",
                ModelPartBuilder.create(),
                ModelTransform.NONE);
        ModelPartData bone25 = bone26.addChild("bone25",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.5F, 1.096267F, -4.356726F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone27 = bone31.addChild("bone27",
                ModelPartBuilder.create(),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData bone28 = bone27.addChild("bone28",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.25359F, 0.856375F, -4.642617F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone29 = bone31.addChild("bone29",
                ModelPartBuilder.create(),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData bone30 = bone29.addChild("bone30",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.813397F, 1.33974F, -4.185741F, 11.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        ModelPartData CORNER2 = Base.addChild("CORNER2",
                ModelPartBuilder.create(),
                ModelTransform.of(0.0F, 25.0F, 0.0F, 0.0F, -3.141593F, -3.141593F));
        ModelPartData bone39 = CORNER2.addChild("bone39",
                ModelPartBuilder.create(),
                ModelTransform.of(-0.5F, -5.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        ModelPartData bone40 = bone39.addChild("bone40",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.5F, 0.0F, 0.0F));
        ModelPartData bone41 = bone40.addChild("bone41",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.5F, -0.903733F, -4.356726F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone42 = bone39.addChild("bone42",
                ModelPartBuilder.create(),
                ModelTransform.of(0.5F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData bone43 = bone42.addChild("bone43",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.25359F, -1.143625F, -4.642617F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone44 = bone39.addChild("bone44",
                ModelPartBuilder.create(),
                ModelTransform.of(0.5F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData bone45 = bone44.addChild("bone45",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.8F, -0.803733F, -4.356726F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone46 = CORNER2.addChild("bone46",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, -5.0F, 0.0F));
        ModelPartData bone47 = bone46.addChild("bone47",
                ModelPartBuilder.create(),
                ModelTransform.NONE);
        ModelPartData bone48 = bone47.addChild("bone48",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.5F, -0.903733F, -4.356726F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone49 = bone46.addChild("bone49",
                ModelPartBuilder.create(),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData bone50 = bone49.addChild("bone50",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-5.25359F, -1.143625F, -4.642617F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone51 = bone46.addChild("bone51",
                ModelPartBuilder.create(),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData bone52 = bone51.addChild("bone52",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.813397F, -0.66026F, -4.185741F, 11.0F, 7.0F, 3.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        ModelPartData bone7 = Base.addChild("bone7",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 7.0F, 0.0F, 0.0F, -1.570796F, 0.0F));
        ModelPartData bone6 = Base.addChild("bone6",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 7.0F, 0.0F, 0.0F, -0.523599F, 0.0F));
        ModelPartData bone5 = Base.addChild("bone5",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 7.0F, 0.0F, 0.0F, 0.523599F, 0.0F));
        ModelPartData bone3 = Base.addChild("bone3",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 7.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        ModelPartData bone2 = Base.addChild("bone2",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 7.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        ModelPartData Panel1 = modelPartData.addChild("Panel1",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 14.0F, 0.0F));
        ModelPartData bone = Panel1.addChild("bone",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone23 = Panel1.addChild("bone23",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone24 = bone23.addChild("bone24",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData Panel2 = modelPartData.addChild("Panel2",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 14.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData bone8 = Panel2.addChild("bone8",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone21 = Panel2.addChild("bone21",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone22 = bone21.addChild("bone22",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData Panel3 = modelPartData.addChild("Panel3",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 14.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData bone9 = Panel3.addChild("bone9",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone19 = Panel3.addChild("bone19",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone20 = bone19.addChild("bone20",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData Panel4 = modelPartData.addChild("Panel4",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 14.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        ModelPartData bone10 = Panel4.addChild("bone10",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone17 = Panel4.addChild("bone17",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone18 = bone17.addChild("bone18",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData Panel5 = modelPartData.addChild("Panel5",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 14.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        ModelPartData bone11 = Panel5.addChild("bone11",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(6.9F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone15 = Panel5.addChild("bone15",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone16 = bone15.addChild("bone16",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData Panel6 = modelPartData.addChild("Panel6",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 14.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        ModelPartData bone12 = Panel6.addChild("bone12",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        ModelPartData bone13 = Panel6.addChild("bone13",
                ModelPartBuilder.create(),
                ModelTransform.of(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        ModelPartData bone14 = bone13.addChild("bone14",
                ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new Dilation(0.0F)),
                ModelTransform.of(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        ModelPartData time_rotor = modelPartData.addChild("time_rotor",
                ModelPartBuilder.create(),
                ModelTransform.NONE);
        ModelPartData Time_middle = time_rotor.addChild("Time_middle",
                ModelPartBuilder.create()
                .uv(0, 29).cuboid(-0.5F, -6.267647F, 2.747059F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F))
                .uv(2, 29).cuboid(3.0F, -6.267647F, -0.752941F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 29).cuboid(-4.0F, -6.267647F, -0.752941F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(0.5F, -0.267647F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(27, 33).cuboid(0.5F, -2.267647F, -0.252941F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(-1.5F, -0.267647F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(27, 33).cuboid(-1.5F, -2.267647F, -0.252941F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-0.5F, -3.767647F, -0.952941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(0.2F, -3.067647F, -0.952941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-1.2F, -3.067647F, -0.952941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-0.5F, -3.067647F, -1.552941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-0.5F, -3.067647F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(0.5F, -2.667647F, 0.247059F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-1.5F, -2.667647F, 0.247059F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-1.5F, -2.667647F, -1.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(0.5F, -2.667647F, -1.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-0.5F, -2.667647F, -2.452941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(14, 38).cuboid(-0.5F, -1.667647F, -2.352941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(14, 38).cuboid(-0.5F, -1.667647F, 0.847059F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-0.5F, -2.667647F, 0.947059F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(1.2F, -2.667647F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 38).cuboid(-2.2F, -2.667647F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(0.5F, 0.232353F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(-1.5F, 0.232353F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(0.5F, 1.032353F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(-1.5F, 1.032353F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(0.5F, 1.532353F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(-1.5F, 1.532353F, -0.752941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(0.5F, 2.332353F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 34).cuboid(1.0F, 3.332353F, -0.252941F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(18, 39).cuboid(0.5F, 5.332353F, 0.247059F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(6, 38).cuboid(-1.5F, 2.332353F, -0.252941F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 34).cuboid(-1.0F, 3.332353F, -0.252941F, 0.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(19, 38).cuboid(-1.5F, 5.332353F, 0.247059F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 31.267647F, 0.252941F));
        ModelPartData rotor6 = time_rotor.addChild("rotor6",
                ModelPartBuilder.create()
                .uv(26, 27).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        ModelPartData rotor5 = time_rotor.addChild("rotor5",
                ModelPartBuilder.create()
                .uv(26, 27).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        ModelPartData rotor4 = time_rotor.addChild("rotor4",
                ModelPartBuilder.create()
                .uv(26, 27).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        ModelPartData rotor3 = time_rotor.addChild("rotor3",
                ModelPartBuilder.create()
                .uv(27, 27).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        ModelPartData rotor2 = time_rotor.addChild("rotor2",
                ModelPartBuilder.create()
                .uv(26, 26).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        ModelPartData rotor = time_rotor.addChild("rotor",
                ModelPartBuilder.create()
                .uv(26, 27).cuboid(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new Dilation(0.0F))
                .uv(26, 26).cuboid(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.NONE);
        return TexturedModelData.of(modelData, 40, 40);
    }

    @Override
    public void setAngles(TardisRenderState state) {
        this.timeRotor.resetTransform();
        this.timeRotor.pivotY = rotorPivotY(state.getRotorBobOffset());
    }
}
