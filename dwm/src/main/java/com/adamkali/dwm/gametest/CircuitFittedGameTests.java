package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.adamkali.dwm.tardis.logic.StabiliserLogic;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class CircuitFittedGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void worldgenFound_claimKeepsBrokenCircuits(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        tardis.setWorldgenFound(true);
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        if (CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR)) {
            throw new AssertionError("Worldgen found ship should have broken planet locator");
        }
        if (StabiliserLogic.isEnabled(model)) {
            throw new AssertionError("Worldgen found ship should start with stabilisers off");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        if (!TardisOwnershipLogic.tryClaimOnEnter(tardisId, player.getUUID())) {
            throw new AssertionError("Expected claim to succeed");
        }
        if (CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR)) {
            throw new AssertionError("Claim must not repair circuits");
        }

        BlockPos consoleRel = new BlockPos(4, 2, 4);
        BlockPos consoleAbs = context.absolutePos(consoleRel);
        context.setBlock(
                consoleRel,
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()
                        .setValue(FirstDoctorConsoleBlock.FACING, Direction.NORTH)
        );
        if (!(context.getLevel().getBlockEntity(consoleAbs) instanceof FirstDoctorConsoleBlockEntity console)) {
            throw new AssertionError("Expected FirstDoctorConsoleBlockEntity");
        }
        console.setTardisId(tardisId);
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                tardisAbs.getX(),
                tardisAbs.getY(),
                tardisAbs.getZ(),
                0
        );
        model.selectedDimension = "minecraft:the_nether";
        String beforeDim = model.selectedDimension;

        context.runAtTickTime(1, () -> {
            List<ConsoleControlInteractionEntity> planet = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(consoleAbs).inflate(2.5),
                    entity -> entity.isBoundTo(consoleAbs)
                            && entity.getLookTarget() == LookTarget.PLANET_LOCATOR
            );
            if (planet.isEmpty()) {
                throw new AssertionError("Expected planet locator control entity");
            }
            InteractionResult planetResult = planet.getFirst().interact(
                    player,
                    InteractionHand.MAIN_HAND,
                    Vec3.ZERO
            );
            if (planetResult != InteractionResult.CONSUME && planetResult != InteractionResult.SUCCESS) {
                throw new AssertionError("Expected broken planet locator to consume, got " + planetResult);
            }
            if (!beforeDim.equals(model.selectedDimension)) {
                throw new AssertionError("Broken planet locator must not change selectedDimension");
            }
            String effective = TardisLogic.effectiveDestinationDimension(model);
            if ("minecraft:the_nether".equals(effective)) {
                throw new AssertionError("Broken planet locator must ignore selectedDimension, got " + effective);
            }

            List<ConsoleControlInteractionEntity> biome = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(consoleAbs).inflate(2.5),
                    entity -> entity.isBoundTo(consoleAbs)
                            && entity.getLookTarget() == LookTarget.BIOME_SELECTOR
            );
            if (biome.isEmpty()) {
                throw new AssertionError("Expected biome selector control entity");
            }
            InteractionResult biomeResult = biome.getFirst().interact(
                    player,
                    InteractionHand.MAIN_HAND,
                    Vec3.ZERO
            );
            if (biomeResult != InteractionResult.SUCCESS && biomeResult != InteractionResult.CONSUME) {
                throw new AssertionError("Biome selector should still respond, got " + biomeResult);
            }

            List<ConsoleControlInteractionEntity> stabilisers = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(consoleAbs).inflate(2.5),
                    entity -> entity.isBoundTo(consoleAbs)
                            && entity.getLookTarget() == LookTarget.STABILISERS
            );
            if (stabilisers.isEmpty()) {
                throw new AssertionError("Expected stabilisers control entity");
            }
            boolean before = StabiliserLogic.isEnabled(model);
            stabilisers.getFirst().interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (StabiliserLogic.isEnabled(model) != before) {
                throw new AssertionError("Broken stabilisers must not toggle");
            }

            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void playerPlaced_isFullyFitted(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        for (TardisCircuit circuit : TardisCircuit.values()) {
            if (!CircuitFittedLogic.isFitted(model, circuit)) {
                throw new AssertionError("Player-placed ship must have fitted " + circuit);
            }
        }
        if (!StabiliserLogic.isEnabled(model)) {
            throw new AssertionError("Player-placed ship must start with stabilisers on");
        }

        BlockPos consoleRel = new BlockPos(4, 2, 4);
        BlockPos consoleAbs = context.absolutePos(consoleRel);
        context.setBlock(
                consoleRel,
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()
                        .setValue(FirstDoctorConsoleBlock.FACING, Direction.NORTH)
        );
        if (!(context.getLevel().getBlockEntity(consoleAbs) instanceof FirstDoctorConsoleBlockEntity console)) {
            throw new AssertionError("Expected FirstDoctorConsoleBlockEntity");
        }
        console.setTardisId(tardisId);
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                tardisAbs.getX(),
                tardisAbs.getY(),
                tardisAbs.getZ(),
                0
        );

        context.runAtTickTime(1, () -> {
            Player player = context.makeMockPlayer(GameType.SURVIVAL);
            List<ConsoleControlInteractionEntity> planet = context.getLevel().getEntitiesOfClass(
                    ConsoleControlInteractionEntity.class,
                    new AABB(consoleAbs).inflate(2.5),
                    entity -> entity.isBoundTo(consoleAbs)
                            && entity.getLookTarget() == LookTarget.PLANET_LOCATOR
            );
            if (planet.isEmpty()) {
                throw new AssertionError("Expected planet locator control entity");
            }
            String before = model.selectedDimension;
            InteractionResult result = planet.getFirst().interact(
                    player,
                    InteractionHand.MAIN_HAND,
                    Vec3.ZERO
            );
            if (result != InteractionResult.SUCCESS && result != InteractionResult.CONSUME) {
                throw new AssertionError("Fitted planet locator should respond, got " + result);
            }
            // Dimension may or may not change depending on available worlds; must not be treated as broken.
            if (CircuitFittedLogic.isBroken(model, TardisCircuit.PLANET_LOCATOR)) {
                throw new AssertionError("Player-placed planet locator must stay fitted");
            }
            if (before != null && before.equals(model.selectedDimension)
                    && TardisLogic.getSelectedDimension(tardisId) == null) {
                // No-op is fine when only one dimension is loaded.
            }
            context.succeed();
        });
    }
}
