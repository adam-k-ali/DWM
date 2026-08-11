// Made with Blockbench (converted from first_base_console.bbmodel)
// Exported for Minecraft version 1.17+ for Yarn

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class FirstDoctorConsoleModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_console"), "main");
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/first_white_base_console.png");

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

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition Base = modelPartData.addOrReplaceChild("Base",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, 20.0F, -5.0F, 12.0F, 11.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition bone4 = Base.addOrReplaceChild("bone4",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, 7.0F, 0.0F));
        PartDefinition CORNER = Base.addOrReplaceChild("CORNER",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition bone32 = CORNER.addOrReplaceChild("bone32",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.5F, -5.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        PartDefinition bone33 = bone32.addOrReplaceChild("bone33",
                CubeListBuilder.create(),
                PartPose.offset(0.5F, 0.0F, 0.0F));
        PartDefinition bone34 = bone33.addOrReplaceChild("bone34",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, 1.096267F, -4.356726F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone35 = bone32.addOrReplaceChild("bone35",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition bone36 = bone35.addOrReplaceChild("bone36",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.25359F, 0.856375F, -4.642617F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone37 = bone32.addOrReplaceChild("bone37",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition bone38 = bone37.addOrReplaceChild("bone38",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.8F, 1.196267F, -4.356726F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone31 = CORNER.addOrReplaceChild("bone31",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition bone26 = bone31.addOrReplaceChild("bone26",
                CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition bone25 = bone26.addOrReplaceChild("bone25",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, 1.096267F, -4.356726F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone27 = bone31.addOrReplaceChild("bone27",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition bone28 = bone27.addOrReplaceChild("bone28",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.25359F, 0.856375F, -4.642617F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone29 = bone31.addOrReplaceChild("bone29",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition bone30 = bone29.addOrReplaceChild("bone30",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.813397F, 1.33974F, -4.185741F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        PartDefinition CORNER2 = Base.addOrReplaceChild("CORNER2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, 0.0F, -3.141593F, -3.141593F));
        PartDefinition bone39 = CORNER2.addOrReplaceChild("bone39",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.5F, -5.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        PartDefinition bone40 = bone39.addOrReplaceChild("bone40",
                CubeListBuilder.create(),
                PartPose.offset(0.5F, 0.0F, 0.0F));
        PartDefinition bone41 = bone40.addOrReplaceChild("bone41",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -0.903733F, -4.356726F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone42 = bone39.addOrReplaceChild("bone42",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition bone43 = bone42.addOrReplaceChild("bone43",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.25359F, -1.143625F, -4.642617F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone44 = bone39.addOrReplaceChild("bone44",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition bone45 = bone44.addOrReplaceChild("bone45",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.8F, -0.803733F, -4.356726F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone46 = CORNER2.addOrReplaceChild("bone46",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition bone47 = bone46.addOrReplaceChild("bone47",
                CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition bone48 = bone47.addOrReplaceChild("bone48",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -0.903733F, -4.356726F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone49 = bone46.addOrReplaceChild("bone49",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition bone50 = bone49.addOrReplaceChild("bone50",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.25359F, -1.143625F, -4.642617F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, -8.3F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone51 = bone46.addOrReplaceChild("bone51",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition bone52 = bone51.addOrReplaceChild("bone52",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.813397F, -0.66026F, -4.185741F, 11.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.459808F, 2.4F, -8.969615F, 0.698132F, 0.0F, 0.0F));
        PartDefinition bone7 = Base.addOrReplaceChild("bone7",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 7.0F, 0.0F, 0.0F, -1.570796F, 0.0F));
        PartDefinition bone6 = Base.addOrReplaceChild("bone6",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 7.0F, 0.0F, 0.0F, -0.523599F, 0.0F));
        PartDefinition bone5 = Base.addOrReplaceChild("bone5",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.5F, -1.0F, -4.0F, 17.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 7.0F, 0.0F, 0.0F, 0.523599F, 0.0F));
        PartDefinition bone3 = Base.addOrReplaceChild("bone3",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 7.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        PartDefinition bone2 = Base.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.5F, -1.0F, -1.0F, 25.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 7.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        PartDefinition Panel1 = modelPartData.addOrReplaceChild("Panel1",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition bone = Panel1.addOrReplaceChild("bone",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone23 = Panel1.addOrReplaceChild("bone23",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone24 = bone23.addOrReplaceChild("bone24",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition Panel2 = modelPartData.addOrReplaceChild("Panel2",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition bone8 = Panel2.addOrReplaceChild("bone8",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone21 = Panel2.addOrReplaceChild("bone21",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone22 = bone21.addOrReplaceChild("bone22",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition Panel3 = modelPartData.addOrReplaceChild("Panel3",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition bone9 = Panel3.addOrReplaceChild("bone9",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone19 = Panel3.addOrReplaceChild("bone19",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone20 = bone19.addOrReplaceChild("bone20",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition Panel4 = modelPartData.addOrReplaceChild("Panel4",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        PartDefinition bone10 = Panel4.addOrReplaceChild("bone10",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone17 = Panel4.addOrReplaceChild("bone17",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone18 = bone17.addOrReplaceChild("bone18",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition Panel5 = modelPartData.addOrReplaceChild("Panel5",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        PartDefinition bone11 = Panel5.addOrReplaceChild("bone11",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(6.9F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone15 = Panel5.addOrReplaceChild("bone15",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone16 = bone15.addOrReplaceChild("bone16",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition Panel6 = modelPartData.addOrReplaceChild("Panel6",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 13.0F, -6.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-13.0F, 3.9F, -22.5F, 26.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        PartDefinition bone12 = Panel6.addOrReplaceChild("bone12",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, 5.081269F, 5.338629F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 5.081269F, 5.338629F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, 4.081269F, -0.661371F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 4.081269F, -0.661371F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 4.081269F, -0.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, 3.081269F, -6.661371F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 3.081269F, -6.661371F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.714286F, -13.785714F, -0.261799F, 0.0F, 0.0F));
        PartDefinition bone13 = Panel6.addOrReplaceChild("bone13",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.0F, -12.0F, 7.0F, -0.349066F, 0.0F, 0.174533F));
        PartDefinition bone14 = bone13.addOrReplaceChild("bone14",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.587365F, 3.052499F, -14.234025F, 3.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 22.5F, -7.5F, 0.0F, 0.558505F, 0.0F));
        PartDefinition time_rotor = modelPartData.addOrReplaceChild("time_rotor",
                CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition Time_middle = time_rotor.addOrReplaceChild("Time_middle",
                CubeListBuilder.create()
                .texOffs(0, 29).addBox(-0.5F, -6.267647F, 2.747059F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 29).addBox(3.0F, -6.267647F, -0.752941F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-4.0F, -6.267647F, -0.752941F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(0.5F, -0.267647F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(27, 33).addBox(0.5F, -2.267647F, -0.252941F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(-1.5F, -0.267647F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(27, 33).addBox(-1.5F, -2.267647F, -0.252941F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-0.5F, -3.767647F, -0.952941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(0.2F, -3.067647F, -0.952941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-1.2F, -3.067647F, -0.952941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-0.5F, -3.067647F, -1.552941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-0.5F, -3.067647F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(0.5F, -2.667647F, 0.247059F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-1.5F, -2.667647F, 0.247059F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-1.5F, -2.667647F, -1.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(0.5F, -2.667647F, -1.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-0.5F, -2.667647F, -2.452941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 38).addBox(-0.5F, -1.667647F, -2.352941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 38).addBox(-0.5F, -1.667647F, 0.847059F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-0.5F, -2.667647F, 0.947059F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(1.2F, -2.667647F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 38).addBox(-2.2F, -2.667647F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(0.5F, 0.232353F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(-1.5F, 0.232353F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(0.5F, 1.032353F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(-1.5F, 1.032353F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(0.5F, 1.532353F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(-1.5F, 1.532353F, -0.752941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(0.5F, 2.332353F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 34).addBox(1.0F, 3.332353F, -0.252941F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 39).addBox(0.5F, 5.332353F, 0.247059F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(6, 38).addBox(-1.5F, 2.332353F, -0.252941F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 34).addBox(-1.0F, 3.332353F, -0.252941F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 38).addBox(-1.5F, 5.332353F, 0.247059F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 31.267647F, 0.252941F));
        PartDefinition rotor6 = time_rotor.addOrReplaceChild("rotor6",
                CubeListBuilder.create()
                .texOffs(26, 27).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.047198F, 0.0F));
        PartDefinition rotor5 = time_rotor.addOrReplaceChild("rotor5",
                CubeListBuilder.create()
                .texOffs(26, 27).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.094395F, 0.0F));
        PartDefinition rotor4 = time_rotor.addOrReplaceChild("rotor4",
                CubeListBuilder.create()
                .texOffs(26, 27).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.141593F, 0.0F));
        PartDefinition rotor3 = time_rotor.addOrReplaceChild("rotor3",
                CubeListBuilder.create()
                .texOffs(27, 27).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.094395F, 0.0F));
        PartDefinition rotor2 = time_rotor.addOrReplaceChild("rotor2",
                CubeListBuilder.create()
                .texOffs(26, 26).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.047198F, 0.0F));
        PartDefinition rotor = time_rotor.addOrReplaceChild("rotor",
                CubeListBuilder.create()
                .texOffs(26, 27).addBox(-2.5F, 28.0F, 4.0F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26).addBox(-1.5F, 31.0F, 2.6F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(modelData, 40, 40);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        this.timeRotor.resetPose();
        this.timeRotor.y = rotorPivotY(state.getRotorBobOffset());
    }
}
