package com.adamkali.dwm.render;

import com.adamkali.dwm.block.ConsoleControlSpec;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.model.tileentity.BiomeSelectorModel;
import com.adamkali.dwm.model.tileentity.ChameleonCircuitModel;
import com.adamkali.dwm.model.tileentity.CloakLeverModel;
import com.adamkali.dwm.model.tileentity.CoordinateLockModel;
import com.adamkali.dwm.model.tileentity.DoorLockModel;
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
import com.adamkali.dwm.model.tileentity.TelepathicCircuitModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.WaypointSelectorModel;
import com.adamkali.dwm.render.state.FirstDoctorConsoleBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.ConsoleDisplayState;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.List;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.model.Model;
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
import org.jetbrains.annotations.Nullable;

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
    private final List<MountedWidget> widgets;
    private final HashMap<TardisChameleonVariant, TardisModel> shellModelCache = new HashMap<>();
    private final HashMap<TardisChameleonVariant, Identifier> shellTextureCache = new HashMap<>();

    public FirstDoctorConsoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new FirstDoctorConsoleModel(
                context.bakeLayer(FirstDoctorConsoleModel.LAYER_LOCATION));
        ReaderModel readerModel = new ReaderModel(context.bakeLayer(ReaderModel.LAYER_LOCATION));

        this.widgets = List.of(
                widget(LookTarget.BIOME_SELECTOR,
                        new BiomeSelectorModel(context.bakeLayer(BiomeSelectorModel.LAYER_LOCATION)),
                        BiomeSelectorModel.TEXTURE_LOCATION, null),
                widget(LookTarget.WAYPOINT_SELECTOR,
                        new WaypointSelectorModel(context.bakeLayer(WaypointSelectorModel.LAYER_LOCATION)),
                        WaypointSelectorModel.TEXTURE_LOCATION, null),
                widget(LookTarget.PLAYER_LOCATOR,
                        new PlayerLocatorModel(context.bakeLayer(PlayerLocatorModel.LAYER_LOCATION)),
                        PlayerLocatorModel.TEXTURE_LOCATION, null),
                widget(LookTarget.PLANET_LOCATOR,
                        new PlanetLocatorModel(context.bakeLayer(PlanetLocatorModel.LAYER_LOCATION)),
                        PlanetLocatorModel.TEXTURE_LOCATION, null),
                widget(LookTarget.CHAMELEON_CIRCUIT,
                        new ChameleonCircuitModel(context.bakeLayer(ChameleonCircuitModel.LAYER_LOCATION)),
                        ChameleonCircuitModel.TEXTURE_LOCATION, null),
                widget(LookTarget.MATERIALISATION_LEVER,
                        new MaterialisationLeverModel(context.bakeLayer(MaterialisationLeverModel.LAYER_LOCATION)),
                        MaterialisationLeverModel.TEXTURE_LOCATION, null),
                widget(LookTarget.FAST_RETURN,
                        new FastReturnModel(context.bakeLayer(FastReturnModel.LAYER_LOCATION)),
                        FastReturnModel.TEXTURE_LOCATION, null),
                widget(LookTarget.STABILISERS,
                        new StabilisersModel(context.bakeLayer(StabilisersModel.LAYER_LOCATION)),
                        StabilisersModel.TEXTURE_LOCATION, null),
                widget(LookTarget.OXYGEN_READER, readerModel, ReaderModel.OXYGEN_TEXTURE,
                        display -> needle(display.reading(), ExteriorEnvironmentReadout.Reading::oxygen)),
                widget(LookTarget.PRESSURE_READER, readerModel, ReaderModel.PRESSURE_TEXTURE,
                        display -> needle(display.reading(), ExteriorEnvironmentReadout.Reading::pressure)),
                widget(LookTarget.TEMPERATURE_READER, readerModel, ReaderModel.TEMPERATURE_TEXTURE,
                        display -> needle(display.reading(), ExteriorEnvironmentReadout.Reading::temperature)),
                widget(LookTarget.RADIATION_READER,
                        new RadiationReaderModel(context.bakeLayer(RadiationReaderModel.LAYER_LOCATION)),
                        RadiationReaderModel.TEXTURE_LOCATION,
                        display -> needle(display.reading(), ExteriorEnvironmentReadout.Reading::radiation)),
                widget(LookTarget.REFUELER, readerModel, ReaderModel.REFUELER_TEXTURE,
                        display -> ArtronLogic.needleFrom(display.artron())),
                widget(LookTarget.TELEPATHIC_CIRCUIT,
                        new TelepathicCircuitModel(context.bakeLayer(TelepathicCircuitModel.LAYER_LOCATION)),
                        TelepathicCircuitModel.TEXTURE_LOCATION, null),
                widget(LookTarget.CLOAK,
                        new CloakLeverModel(context.bakeLayer(CloakLeverModel.LAYER_LOCATION)),
                        CloakLeverModel.TEXTURE_LOCATION, null),
                widget(LookTarget.DOOR_LOCK,
                        new DoorLockModel(context.bakeLayer(DoorLockModel.LAYER_LOCATION)),
                        DoorLockModel.TEXTURE_LOCATION, null),
                // One mesh for all three coordinate-lock pads; Y/Z are hitboxes only.
                widget(LookTarget.COORDINATE_LOCK_X,
                        new CoordinateLockModel(context.bakeLayer(CoordinateLockModel.LAYER_LOCATION)),
                        CoordinateLockModel.TEXTURE_LOCATION, null)
        );

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

    private static MountedWidget widget(
            LookTarget target,
            Model<? super TardisRenderState> controlModel,
            Identifier texture,
            @Nullable ToDoubleFunction<ConsoleDisplayState> needle
    ) {
        return new MountedWidget(target, controlModel, texture, needle);
    }

    private static float needle(
            ExteriorEnvironmentReadout.Reading reading,
            ToDoubleFunction<ExteriorEnvironmentReadout.Reading> value
    ) {
        return reading.needle((float) value.applyAsDouble(reading));
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
        ConsoleDisplayState display = entity.syncedDisplay();
        state.display = display;
        state.traveling = phase.isTraveling();
        state.rotorBobOffset = FirstDoctorConsoleModel.rotorBobOffset(
                timeTicks, phase.isTraveling(), display.stabilisersEnabled());
        state.rotorSpinRadians = FirstDoctorConsoleModel.rotorSpinRadians(
                timeTicks, phase.isTraveling(), display.stabilisersEnabled());
        state.hologramYawDegrees = hologramYawDegrees(timeTicks);
        state.hologramBobOffset = hologramBobOffset(timeTicks);

        if (world != null && world.isClientSide() && state.traveling && !display.stabilisersEnabled()) {
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
        animState.setRotorSpinRadians(state.rotorSpinRadians);
        applyDisplay(animState, state.display);

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

        for (MountedWidget widget : widgets) {
            submitMounted(poseStack, submitNodeCollector, state, animState, widget);
            if (widget.target() == LookTarget.CHAMELEON_CIRCUIT) {
                submitChameleonHologram(poseStack, submitNodeCollector, state);
            }
        }

        poseStack.popPose();
    }

    private static void applyDisplay(TardisRenderState animState, ConsoleDisplayState display) {
        ConsoleDisplayState safe = display == null ? ConsoleDisplayState.defaults() : display;
        animState.setStabilisersEnabled(safe.stabilisersEnabled());
        animState.setCloaked(safe.cloaked());
        animState.setDoorsLocked(safe.doorsLocked());
        animState.setLockX(safe.lockX());
        animState.setLockY(safe.lockY());
        animState.setLockZ(safe.lockZ());
    }

    private void submitMounted(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            FirstDoctorConsoleBlockEntityRenderState state,
            TardisRenderState animState,
            MountedWidget widget
    ) {
        ConsoleControlSpec layout = FirstDoctorConsoleControls.spec(widget.target());
        if (layout == null) {
            return;
        }
        if (widget.needle() != null) {
            animState.setNeedle((float) widget.needle().applyAsDouble(state.display));
        }
        poseStack.pushPose();
        applyPanelControlTransforms(poseStack, layout);
        submitNodeCollector.submitModel(
                widget.model(),
                animState,
                poseStack,
                RenderTypes.entityCutout(widget.texture()),
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
        TardisChameleonVariant variant = state.display == null
                ? TardisChameleonVariant.TT_CAPSULE
                : state.display.variant();
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
     * Mount at the chameleon control, lift above the dial (with bob), undo deck
     * pitch so the shell stands upright for the player, then scale, Java-entity
     * orientation, and turntable yaw.
     */
    static void applyPanel6ChameleonHologramTransforms(
            PoseStack matrices,
            float yawDegrees,
            float bobOffset
    ) {
        ConsoleControlSpec chameleon = FirstDoctorConsoleControls.spec(LookTarget.CHAMELEON_CIRCUIT);
        if (chameleon == null) {
            return;
        }
        applyPanelControlTransforms(
                matrices,
                chameleon.panelYaw(),
                1.0F,
                chameleon.mountX(),
                chameleon.mountY(),
                chameleon.mountZ()
        );
        matrices.translate(0.0, HOLOGRAM_Y_OFFSET_PX * PX + bobOffset, 0.0);
        matrices.mulPose(Axis.XP.rotation(-FirstDoctorConsoleControls.DECK_PITCH_RAD));
        matrices.scale(HOLOGRAM_SCALE, HOLOGRAM_SCALE, HOLOGRAM_SCALE);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.mulPose(Axis.YP.rotationDegrees(yawDegrees));
    }

    private static void applyPanelControlTransforms(PoseStack matrices, ConsoleControlSpec layout) {
        applyPanelControlTransforms(
                matrices,
                layout.panelYaw(),
                layout.scale(),
                layout.mountX(),
                layout.mountY(),
                layout.mountZ()
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

    private record MountedWidget(
            LookTarget target,
            Model<? super TardisRenderState> model,
            Identifier texture,
            @Nullable ToDoubleFunction<ConsoleDisplayState> needle
    ) {
    }
}
