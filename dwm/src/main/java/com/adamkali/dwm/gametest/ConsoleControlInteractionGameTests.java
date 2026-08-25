package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConsoleControlInteractionGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void consoleSpawnsEightControlEntities(GameTestHelper context) {
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = context.absolutePos(rel);
        context.setBlock(
                rel,
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState().setValue(FirstDoctorConsoleBlock.FACING, Direction.NORTH)
        );
        context.assertBlockPresent(DWMBlocks.FIRST_DOCTOR_CONSOLE, rel);

        if (!(context.getLevel().getBlockEntity(abs) instanceof FirstDoctorConsoleBlockEntity)) {
            throw new AssertionError("Expected FirstDoctorConsoleBlockEntity");
        }

        // Server ticker spawns interaction entities.
        context.runAtTickTime(1, () -> {
            List<ConsoleControlInteractionEntity> controls = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(abs).inflate(2.5),
                    entity -> entity.isBoundTo(abs)
            );
            if (controls.size() != LookTarget.interactiveValues().length) {
                throw new AssertionError(
                        "Expected " + LookTarget.interactiveValues().length
                                + " console control entities, got " + controls.size()
                );
            }
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void removingConsoleDiscardsControlEntities(GameTestHelper context) {
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = context.absolutePos(rel);
        context.setBlock(
                rel,
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState().setValue(FirstDoctorConsoleBlock.FACING, Direction.SOUTH)
        );

        context.runAtTickTime(1, () -> {
            List<ConsoleControlInteractionEntity> before = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(abs).inflate(2.5),
                    entity -> entity.isBoundTo(abs)
            );
            if (before.isEmpty()) {
                throw new AssertionError("Expected control entities before removal");
            }

            context.setBlock(rel, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

            context.runAtTickTime(2, () -> {
                List<ConsoleControlInteractionEntity> after = context.getLevel().getEntitiesOfClass(
                        ConsoleControlInteractionEntity.class,
                        new AABB(abs).inflate(2.5),
                        entity -> entity.isBoundTo(abs) && !entity.isRemoved()
                );
                if (!after.isEmpty()) {
                    throw new AssertionError("Expected control entities discarded after console removal, got " + after.size());
                }
                context.succeed();
            });
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void interactingStabilisersWithoutTardisIdConsumes(GameTestHelper context) {
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = context.absolutePos(rel);
        BlockState consoleState = DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()
                .setValue(FirstDoctorConsoleBlock.FACING, Direction.EAST);
        context.setBlock(rel, consoleState);

        context.runAtTickTime(1, () -> {
            List<ConsoleControlInteractionEntity> controls = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(abs).inflate(2.5),
                    entity -> entity.isBoundTo(abs)
                            && entity.getLookTarget() == LookTarget.STABILISERS
            );
            if (controls.isEmpty()) {
                throw new AssertionError("Expected stabilisers control entity");
            }

            Player player = context.makeMockPlayer(GameType.SURVIVAL);
            InteractionResult result = controls.getFirst().interact(
                    player,
                    InteractionHand.MAIN_HAND,
                    Vec3.ZERO
            );
            if (result != InteractionResult.CONSUME && result != InteractionResult.SUCCESS) {
                throw new AssertionError("Expected CONSUME/SUCCESS without tardisId, got " + result);
            }

            ConsoleControlInteractionEntity lever = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(abs).inflate(2.5),
                    entity -> entity.isBoundTo(abs)
                            && entity.getLookTarget() == LookTarget.MATERIALISATION_LEVER
            ).stream().findFirst().orElse(null);
            if (lever == null) {
                throw new AssertionError("Expected materialisation lever control entity");
            }
            InteractionResult leverResult = lever.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (leverResult != InteractionResult.CONSUME && leverResult != InteractionResult.SUCCESS) {
                throw new AssertionError("Expected CONSUME/SUCCESS for lever without tardisId, got " + leverResult);
            }

            context.succeed();
        });
    }
}
