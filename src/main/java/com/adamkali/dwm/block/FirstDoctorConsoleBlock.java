package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * First Doctor TARDIS console. Mesh is drawn by {@code FirstDoctorConsoleBlockEntityRenderer}.
 */
public class FirstDoctorConsoleBlock extends BlockWithEntity {
    private static final MapCodec<FirstDoctorConsoleBlock> CODEC = createCodec(FirstDoctorConsoleBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    /** Approximate hexagonal pedestal: ~1.6×1.6 footprint, ~1.25 blocks tall. */
    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.cuboid(-0.3, 0.0, -0.3, 1.3, 1.25, 1.3);

    /** Outline includes Panel3 biome selector so raycast can target it. */
    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.cuboid(-0.5, 0.0, -0.5, 1.5, 1.6, 1.5);

    public FirstDoctorConsoleBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<FirstDoctorConsoleBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FirstDoctorConsoleBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit
    ) {
        if (player.isSneaking()) {
            return ActionResult.PASS;
        }

        Direction facing = state.get(FACING);
        boolean leverHit = FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, player);
        boolean biomeHit = FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, player);
        if (!leverHit && !biomeHit) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof FirstDoctorConsoleBlockEntity console)
                || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.CONSUME;
        }

        UUID tardisId = console.getTardisId();
        if (tardisId == null) {
            player.sendMessage(Text.translatable(
                    leverHit ? "dwm.console.travel_unavailable" : "dwm.console.biome_unavailable"), true);
            return ActionResult.CONSUME;
        }

        if (leverHit) {
            return handleMaterialisationLever(world, pos, player, serverWorld, tardisId);
        }
        return handleBiomeSelector(world, pos, player, serverWorld, tardisId);
    }

    private static ActionResult handleMaterialisationLever(
            World world,
            BlockPos pos,
            PlayerEntity player,
            ServerWorld serverWorld,
            UUID tardisId
    ) {
        TardisTravelPhase phase = TardisLogic.getTravelPhase(tardisId);
        if (phase == TardisTravelPhase.DEMATERIALISING) {
            player.sendMessage(Text.translatable("dwm.console.travel_dematerialising"), true);
            return ActionResult.CONSUME;
        }
        if (phase == TardisTravelPhase.MATERIALISING) {
            player.sendMessage(Text.translatable("dwm.console.travel_materialising"), true);
            return ActionResult.CONSUME;
        }

        ActionResult result;
        String successKey;
        if (phase.awaitsMaterialise()) {
            result = TardisTravelService.requestMaterialise(tardisId, serverWorld.getServer());
            successKey = "dwm.console.travel_materialising";
        } else {
            result = TardisTravelService.startTravel(tardisId, serverWorld.getServer());
            successKey = "dwm.console.travel_dematerialising";
        }

        if (result == ActionResult.SUCCESS) {
            player.sendMessage(Text.translatable(successKey), true);
            world.playSound(
                    null,
                    pos,
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundCategory.BLOCKS,
                    0.4F,
                    1.0F
            );
            return ActionResult.SUCCESS;
        }
        if (result == ActionResult.PASS) {
            player.sendMessage(Text.translatable("dwm.console.travel_in_progress"), true);
            return ActionResult.CONSUME;
        }
        player.sendMessage(Text.translatable("dwm.console.travel_unavailable"), true);
        return ActionResult.CONSUME;
    }

    private static ActionResult handleBiomeSelector(
            World world,
            BlockPos pos,
            PlayerEntity player,
            ServerWorld serverWorld,
            UUID tardisId
    ) {
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendMessage(Text.translatable("dwm.console.travel_in_flight"), true);
            return ActionResult.CONSUME;
        }

        Optional<Identifier> selected = TardisLogic.cycleSelectedBiome(tardisId, serverWorld.getServer());
        if (selected.isEmpty()) {
            player.sendMessage(Text.translatable("dwm.console.biome_unavailable"), true);
            return ActionResult.CONSUME;
        }

        Text biomeName = Text.translatable(selected.get().toTranslationKey("biome"));
        player.sendMessage(Text.translatable("dwm.console.biome_selected", biomeName), true);
        world.playSound(
                null,
                pos,
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundCategory.BLOCKS,
                0.4F,
                1.0F
        );
        return ActionResult.SUCCESS;
    }
}
