package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.ChameleonCircuitModel;
import com.adamkali.dwm.model.tileentity.CloakLeverModel;
import com.adamkali.dwm.model.tileentity.FastReturnModel;
import com.adamkali.dwm.model.tileentity.FifthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FourthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.MaterialisationLeverModel;
import com.adamkali.dwm.model.tileentity.PlanetLocatorModel;
import com.adamkali.dwm.model.tileentity.PlayerLocatorModel;
import com.adamkali.dwm.model.tileentity.RadiationReaderModel;
import com.adamkali.dwm.model.tileentity.ReaderModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SeventhDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SixthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.StabilisersModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.WaypointSelectorModel;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.render.state.FirstDoctorConsoleBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FirstDoctorConsoleBlockEntityRenderer
        implements BlockEntityRenderer<FirstDoctorConsoleBlockEntity, FirstDoctorConsoleBlockEntityRenderState> {
    private static final float PX = 1.0f / 16.0f;
    /** Uniform scale for the translucent shell hologram above the chameleon dial. */
    private static final float HOLOGRAM_SCALE = 0.125F;
    /** Deck-local Y lift (model pixels) so the hologram sits above the dial. */
    private static final float HOLOGRAM_Y_OFFSET_PX = 5.0F;
    private static final int HOLOGRAM_COLOR = ARGB.color(0x88, 0xAA, 0xEE, 0xFF);
    /** ~8s per full revolution (360° / 160 ticks). */
    static final float HOLOGRAM_DEGREES_PER_TICK = 360.0F / 160.0F;
    /** Bob angular speed (~1.25s period at 20 TPS). */
    static final float HOLOGRAM_BOB_SPEED = 0.08F;
    /** Peak vertical bob displacement in blocks (deck-local Y). */
    static final float HOLOGRAM_BOB_AMPLITUDE = 0.03F;

    private final FirstDoctorConsoleModel model;
    private final BiomeSelectorModel biomeSelectorModel;
    private final WaypointSelectorModel waypointSelectorModel;
    private final PlayerLocatorModel playerLocatorModel;
    private final PlanetLocatorModel planetLocatorModel;
    private final ChameleonCircuitModel chameleonCircuitModel;
    private final MaterialisationLeverModel materialisationLeverModel;
    private final FastReturnModel fastReturnModel;
    private final StabilisersModel stabilisersModel;
    private final ReaderModel readerModel;
    private final RadiationReaderModel radiationReaderModel;
    private final CloakLeverModel cloakLeverModel;
    private final HashMap<TardisChameleonVariant, TardisModel> shellModelCache = new HashMap<>();
    private final HashMap<TardisChameleonVariant, Identifier> shellTextureCache = new HashMap<>();

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.bakeLayer(FirstDoctorConsoleModel.LAYER_LOCATION));
        this.biomeSelectorModel = new BiomeSelectorModel(
                context.bakeLayer(BiomeSelectorModel.LAYER_LOCATION));
        this.waypointSelectorModel = new WaypointSelectorModel(
                context.bakeLayer(WaypointSelectorModel.LAYER_LOCATION));
        this.playerLocatorModel = new PlayerLocatorModel(
                context.bakeLayer(PlayerLocatorModel.LAYER_LOCATION));
        this.planetLocatorModel = new PlanetLocatorModel(
                context.bakeLayer(PlanetLocatorModel.LAYER_LOCATION));
        this.chameleonCircuitModel = new ChameleonCircuitModel(
                context.bakeLayer(ChameleonCircuitModel.LAYER_LOCATION));
        this.materialisationLeverModel = new MaterialisationLeverModel(
                context.bakeLayer(MaterialisationLeverModel.LAYER_LOCATION));
        this.fastReturnModel = new FastReturnModel(
                context.bakeLayer(FastReturnModel.LAYER_LOCATION));
        this.stabilisersModel = new StabilisersModel(
                context.bakeLayer(StabilisersModel.LAYER_LOCATION));
        this.readerModel = new ReaderModel(context.bakeLayer(ReaderModel.LAYER_LOCATION));
        this.radiationReaderModel = new RadiationReaderModel(
                context.bakeLayer(RadiationReaderModel.LAYER_LOCATION));
        this.cloakLeverModel = new CloakLeverModel(context.bakeLayer(CloakLeverModel.LAYER_LOCATION));

        cacheShell(TardisChameleonVariant.TT_CAPSULE,
                new TTCapsuleModel(context.bakeLayer(TTCapsuleModel.LAYER_LOCATION)),
                TTCapsuleModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.FIRST_DOCTOR_BOX,
                new FirstDoctorTardisModel(context.bakeLayer(FirstDoctorTardisModel.LAYER_LOCATION)),
                FirstDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.SECOND_DOCTOR_BOX,
                new SecondDoctorTardisModel(context.bakeLayer(SecondDoctorTardisModel.LAYER_LOCATION)),
                SecondDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.THIRD_DOCTOR_BOX,
                new ThirdDoctorTardisModel(context.bakeLayer(ThirdDoctorTardisModel.LAYER_LOCATION)),
                ThirdDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.FOURTH_DOCTOR_BOX,
                new FourthDoctorTardisModel(context.bakeLayer(FourthDoctorTardisModel.LAYER_LOCATION)),
                FourthDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.FIFTH_DOCTOR_BOX,
                new FifthDoctorTardisModel(context.bakeLayer(FifthDoctorTardisModel.LAYER_LOCATION)),
                FifthDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.SIXTH_DOCTOR_BOX,
                new SixthDoctorTardisModel(context.bakeLayer(SixthDoctorTardisModel.LAYER_LOCATION)),
                SixthDoctorTardisModel.TEXTURE_LOCATION);
        cacheShell(TardisChameleonVariant.SEVENTH_DOCTOR_BOX,
                new SeventhDoctorTardisModel(context.bakeLayer(SeventhDoctorTardisModel.LAYER_LOCATION)),
                SeventhDoctorTardisModel.TEXTURE_LOCATION);
    }

    private void cacheShell(TardisChameleonVariant variant, TardisModel shellModel, Identifier texture) {
        shellModelCache.put(variant, shellModel);
        shellTextureCache.put(variant, texture);
    }

    @Override
    public FirstDoctorConsoleBlockEntityRenderState createRenderState() {
        return new FirstDoctorConsoleBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            FirstDoctorConsoleBlockEntity entity,
            FirstDoctorConsoleBlockEntityRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = entity.getBlockState();
        state.facing = blockState.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);

        TardisTravelPhase phase = TardisLogic.getTravelPhase(entity.getTardisId());
        Level world = entity.getLevel();
        float timeTicks = world == null ? partialTicks : world.getGameTime() + partialTicks;
        boolean stabilisersEnabled = entity.isSyncedStabilisersEnabled();
        state.stabilisersEnabled = stabilisersEnabled;
        state.traveling = phase.isTraveling();
        state.rotorBobOffset = FirstDoctorConsoleModel.rotorBobOffset(
                timeTicks, phase.isTraveling(), stabilisersEnabled);
        state.variant = entity.getSyncedVariant();
        state.hologramYawDegrees = hologramYawDegrees(timeTicks);
        state.hologramBobOffset = hologramBobOffset(timeTicks);
        state.cloaked = entity.isSyncedCloaked();
        ExteriorEnvironmentReadout.Reading reading = entity.syncedReading();
        state.readerNoSignal = reading.noSignal();
        state.oxygen = reading.needle(reading.oxygen());
        state.pressure = reading.needle(reading.pressure());
        state.temperature = reading.needle(reading.temperature());
        state.radiation = reading.needle(reading.radiation());

        if (world != null && world.isClientSide() && state.traveling && !stabilisersEnabled) {
            spawnUnstabilisedRotorSmoke(world, entity.getBlockPos());
        }
    }

    /** Light smoke around the time rotor while traveling with stabilisers off. */
    private static void spawnUnstabilisedRotorSmoke(Level world, net.minecraft.core.BlockPos pos) {
        var random = world.getRandom();
        if (random.nextFloat() > 0.35F) {
            return;
        }
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.35;
        double y = pos.getY() + 1.35 + random.nextDouble() * 0.45;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.35;
        world.addParticle(
                random.nextBoolean() ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE,
                x,
                y,
                z,
                0.0,
                0.02,
                0.0
        );
    }

    /** Continuous turntable yaw in degrees from game time. */
    static float hologramYawDegrees(float timeTicks) {
        return timeTicks * HOLOGRAM_DEGREES_PER_TICK;
    }

    /** Slight vertical hover offset from game time. */
    static float hologramBobOffset(float timeTicks) {
        return (float) Math.sin(timeTicks * HOLOGRAM_BOB_SPEED) * HOLOGRAM_BOB_AMPLITUDE;
    }

    @Override
    public void submit(
            FirstDoctorConsoleBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        TardisRenderState animState = new TardisRenderState();
        animState.setRotorBobOffset(state.rotorBobOffset);
        animState.setStabilisersEnabled(state.stabilisersEnabled);
        animState.setCloaked(state.cloaked);

        poseStack.pushPose();
        applyTransforms(poseStack, state.facing);

        submitNodeCollector.submitModel(
                model,
                animState,
                poseStack,
                RenderTypes.entityTranslucent(FirstDoctorConsoleModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);

        submitPanel3Dial(poseStack, submitNodeCollector, state, animState,
                biomeSelectorModel, BiomeSelectorModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.BIOME_SELECTOR_MOUNT_X_PX);
        submitPanel3Dial(poseStack, submitNodeCollector, state, animState,
                waypointSelectorModel, WaypointSelectorModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.WAYPOINT_SELECTOR_MOUNT_X_PX);
        submitPanel3Dial(poseStack, submitNodeCollector, state, animState,
                playerLocatorModel, PlayerLocatorModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.PLAYER_LOCATOR_MOUNT_X_PX);
        submitPanel3Dial(poseStack, submitNodeCollector, state, animState,
                planetLocatorModel, PlanetLocatorModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.PLANET_LOCATOR_MOUNT_X_PX);

        poseStack.pushPose();
        applyPanel6ChameleonTransforms(poseStack);
        submitNodeCollector.submitModel(
                chameleonCircuitModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(ChameleonCircuitModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        submitChameleonHologram(poseStack, submitNodeCollector, state);

        poseStack.pushPose();
        applyPanel6LeverTransforms(poseStack);
        submitNodeCollector.submitModel(
                materialisationLeverModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(MaterialisationLeverModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        poseStack.pushPose();
        applyPanel6FastReturnTransforms(poseStack);
        submitNodeCollector.submitModel(
                fastReturnModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(FastReturnModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        poseStack.pushPose();
        applyPanel6StabilisersTransforms(poseStack);
        submitNodeCollector.submitModel(
                stabilisersModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(StabilisersModel.TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();

        submitReader(poseStack, submitNodeCollector, state, animState,
                ReaderModel.OXYGEN_TEXTURE, state.oxygen,
                FirstDoctorConsoleControls.PANEL1_YAW_RAD,
                FirstDoctorConsoleControls.READER_SCALE,
                FirstDoctorConsoleControls.OXYGEN_READER_MOUNT_X_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);
        submitReader(poseStack, submitNodeCollector, state, animState,
                ReaderModel.PRESSURE_TEXTURE, state.pressure,
                FirstDoctorConsoleControls.PANEL1_YAW_RAD,
                FirstDoctorConsoleControls.READER_SCALE,
                FirstDoctorConsoleControls.PRESSURE_READER_MOUNT_X_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);
        submitReader(poseStack, submitNodeCollector, state, animState,
                ReaderModel.TEMPERATURE_TEXTURE, state.temperature,
                FirstDoctorConsoleControls.PANEL1_YAW_RAD,
                FirstDoctorConsoleControls.READER_SCALE,
                FirstDoctorConsoleControls.TEMPERATURE_READER_MOUNT_X_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);
        animState.setNeedle(state.radiation);
        submitMounted(poseStack, submitNodeCollector, state, animState,
                radiationReaderModel, RadiationReaderModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.PANEL1_YAW_RAD,
                FirstDoctorConsoleControls.RADIATION_SCALE,
                0.0F,
                FirstDoctorConsoleControls.BOTTOM_MOUNT_Y_PX,
                FirstDoctorConsoleControls.BOTTOM_MOUNT_Z_PX);
        submitReader(poseStack, submitNodeCollector, state, animState,
                ReaderModel.REFUELER_TEXTURE, ReaderModel.REFUELER_NEEDLE,
                FirstDoctorConsoleControls.PANEL5_YAW_RAD,
                FirstDoctorConsoleControls.READER_SCALE,
                0.0F,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);

        poseStack.popPose();
        submitMounted(poseStack, submitNodeCollector, state, animState,
                cloakLeverModel, CloakLeverModel.TEXTURE_LOCATION,
                FirstDoctorConsoleControls.PANEL4_YAW_RAD,
                FirstDoctorConsoleControls.CLOAK_SCALE,
                0.0F,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX);

        poseStack.popPose();
    }

    private void submitReader(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            FirstDoctorConsoleBlockEntityRenderState state,
            TardisRenderState animState,
            Identifier texture,
            float needle,
            float panelYawRad,
            float scale,
            float mountXPx,
            float mountYPx,
            float mountZPx
    ) {
        animState.setNeedle(needle);
        submitMounted(
                poseStack,
                submitNodeCollector,
                state,
                animState,
                readerModel,
                texture,
                panelYawRad,
                scale,
                mountXPx,
                mountYPx,
                mountZPx
        );
    }

    private void submitMounted(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            FirstDoctorConsoleBlockEntityRenderState state,
            TardisRenderState animState,
            net.minecraft.client.model.Model<? super TardisRenderState> controlModel,
            Identifier texture,
            float panelYawRad,
            float scale,
            float mountXPx,
            float mountYPx,
            float mountZPx
    ) {
        poseStack.pushPose();
        applyPanelControlTransforms(poseStack, panelYawRad, scale, mountXPx, mountYPx, mountZPx);
        submitNodeCollector.submitModel(
                controlModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(texture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();
    }

    private void submitPanel3Dial(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            FirstDoctorConsoleBlockEntityRenderState state,
            TardisRenderState animState,
            net.minecraft.client.model.Model<? super TardisRenderState> dialModel,
            Identifier texture,
            float mountXPx
    ) {
        poseStack.pushPose();
        applyPanelControlTransforms(
                poseStack,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                mountXPx
        );
        submitNodeCollector.submitModel(
                dialModel,
                animState,
                poseStack,
                RenderTypes.entityCutout(texture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        poseStack.popPose();
    }

    private void submitChameleonHologram(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            FirstDoctorConsoleBlockEntityRenderState state
    ) {
        TardisChameleonVariant variant =
                state.variant != null ? state.variant : TardisChameleonVariant.TT_CAPSULE;
        TardisModel shellModel = shellModelCache.get(variant);
        Identifier texture = shellTextureCache.get(variant);
        if (shellModel == null || texture == null) {
            return;
        }

        TardisRenderState hologramAnim = new TardisRenderState();
        hologramAnim.setDoorSwingProgress(0.0F);

        poseStack.pushPose();
        applyPanel6ChameleonHologramTransforms(poseStack, state.hologramYawDegrees, state.hologramBobOffset);
        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(texture),
                (pose, consumer) -> {
                    PoseStack local = new PoseStack();
                    local.last().set(pose);
                    shellModel.setupAnim(hologramAnim);
                    shellModel.renderToBuffer(local, consumer, light, OverlayTexture.NO_OVERLAY, HOLOGRAM_COLOR);
                });
        poseStack.popPose();
    }

    /**
     * Console model keeps Blockbench Y-up cuboids (unlike Java-entity door exports),
     * so no X-180 flip — only center on the block, scale, and yaw for facing.
     */
    static void applyTransforms(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.scale(1.0f, 0.8f, 1.0f);
        matrices.mulPose(Axis.YP.rotationDegrees(-Direction.getYRot(facing)));
    }

    /**
     * Panel3 → deck → biome mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3BiomeSelectorTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                FirstDoctorConsoleControls.BIOME_SELECTOR_MOUNT_X_PX
        );
    }

    /**
     * Panel3 → deck → planet locator mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel3PlanetLocatorTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL3_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                FirstDoctorConsoleControls.PLANET_LOCATOR_MOUNT_X_PX
        );
    }

    static void applyPanel6ChameleonTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.SELECTOR_SCALE,
                FirstDoctorConsoleControls.CHAMELEON_CIRCUIT_MOUNT_X_PX
        );
    }

    /**
     * Mount at the chameleon control, lift above the dial (with bob), undo deck
     * pitch so the shell stands upright for the player, then scale, Java-entity
     * orientation, and turntable yaw.
     */
    static void applyPanel6ChameleonHologramTransforms(
            PoseStack matrices,
            float yawDegrees,
            float bobOffset
    ) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                1.0F,
                FirstDoctorConsoleControls.CHAMELEON_CIRCUIT_MOUNT_X_PX
        );
        matrices.translate(0.0, HOLOGRAM_Y_OFFSET_PX * PX + bobOffset, 0.0);
        matrices.mulPose(Axis.XP.rotation(-FirstDoctorConsoleControls.DECK_PITCH_RAD));
        matrices.scale(HOLOGRAM_SCALE, HOLOGRAM_SCALE, HOLOGRAM_SCALE);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.mulPose(Axis.YP.rotationDegrees(yawDegrees));
    }

    /**
     * Panel6 → deck → mount + scale, matching {@link FirstDoctorConsoleControls}.
     */
    static void applyPanel6LeverTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.LEVER_SCALE,
                FirstDoctorConsoleControls.LEVER_MOUNT_X_PX
        );
    }

    static void applyPanel6FastReturnTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.FAST_RETURN_SCALE,
                FirstDoctorConsoleControls.FAST_RETURN_MOUNT_X_PX
        );
    }

    static void applyPanel6StabilisersTransforms(PoseStack matrices) {
        applyPanelControlTransforms(
                matrices,
                FirstDoctorConsoleControls.PANEL6_YAW_RAD,
                FirstDoctorConsoleControls.STABILISERS_SCALE,
                FirstDoctorConsoleControls.STABILISERS_MOUNT_X_PX,
                FirstDoctorConsoleControls.STABILISERS_MOUNT_Y_PX,
                FirstDoctorConsoleControls.STABILISERS_MOUNT_Z_PX
        );
    }

    private static void applyPanelControlTransforms(
            PoseStack matrices,
            float panelYawRad,
            float scale,
            float mountXPx
    ) {
        applyPanelControlTransforms(
                matrices,
                panelYawRad,
                scale,
                mountXPx,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Y_PX,
                FirstDoctorConsoleControls.CONTROL_MOUNT_Z_PX
        );
    }

    private static void applyPanelControlTransforms(
            PoseStack matrices,
            float panelYawRad,
            float scale,
            float mountXPx,
            float mountYPx,
            float mountZPx
    ) {
        matrices.translate(0.0, FirstDoctorConsoleControls.PANEL_PIVOT_Y_PX * PX, 0.0);
        matrices.mulPose(Axis.YP.rotation(panelYawRad));

        matrices.translate(
                0.0,
                FirstDoctorConsoleControls.DECK_PIVOT_Y_PX * PX,
                FirstDoctorConsoleControls.DECK_PIVOT_Z_PX * PX
        );
        matrices.mulPose(Axis.XP.rotation(FirstDoctorConsoleControls.DECK_PITCH_RAD));

        matrices.translate(
                mountXPx * PX,
                mountYPx * PX,
                mountZPx * PX
        );
        matrices.scale(scale, scale, scale);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
