package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.PlotSlot;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.TardisPlotSlots;
import com.adamkali.dwm.tardis.interior.ars.ArsService;
import com.adamkali.dwm.tardis.interior.migration.InteriorBlockOps;
import com.adamkali.dwm.tardis.interior.migration.InteriorSnapshot;
import com.adamkali.dwm.tardis.interior.migration.LegacyPositionRewriter;
import com.adamkali.dwm.tardis.interior.migration.MigrationWorld;
import com.adamkali.dwm.tardis.interior.migration.ServerMigrationWorld;
import com.adamkali.dwm.tardis.interior.migration.TardisPlotMigration;
import com.adamkali.dwm.tardis.interior.migration.TardisPlotMigrationHooks;
import com.adamkali.dwm.tardis.interior.migration.TardisPlotPreparer;
import com.adamkali.dwm.tardis.interior.TardisTeleport;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Server GameTests for the one-off migration of legacy 64-block interiors to the 10000-block plots
 * ({@link TardisPlotMigration}) and for clearing leftovers out of a fresh plot ({@link TardisPlotPreparer}).
 *
 * <p>Each test works in the GameTest level (the GameTest server has no {@code dwm:tardis}). It builds a legacy interior at its own TARDIS's legacy
 * anchor (the console room, ARS pieces and a fixture of rich block entities, block states and entities), records
 * every block, block-entity NBT and entity NBT in the legacy window, migrates, and compares the new plot with the
 * recording moved by the migration delta. Legacy anchors are chosen far from every plot anchor so other tests'
 * interiors are never touched, and every new plot is pre-assigned a slot no other test is given.
 */
public class TardisPlotMigrationGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STRUCTURE = "fabric-gametest-api-v1:empty";
    private static final Random RANDOM = new Random();

    // ---- Migration -----------------------------------------------------------------------------------------

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_CopiesEveryBlockBlockEntityAndEntityExactly(GameTestHelper context) {
        ServerLevel level = interior(context);
        Legacy tardis = legacyTardis(level, 40, 41, new PlotSlot(11, -13));
        tardis.buildInterior(level, true);
        BlockPos controlBlock = tardis.legacyAnchor.offset(-TardisPlotAllocator.LEGACY_WINDOW_OFFSET - 1, 1, 5);
        level.setBlock(controlBlock, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        Entity controlEntity = summon(level, "armor_stand", Vec3.atBottomCenterOf(controlBlock.above()), "{NoGravity:1b}");
        ConsoleControlInteractionEntity control = tardis.spawnConsoleControl(level);
        Recording before = Recording.of(level, tardis.legacyBox());
        List<ArsPlacedPiece> piecesBefore = List.copyOf(tardis.model().getArsPieces());
        check(piecesBefore.size() >= 2, "fixture grew " + piecesBefore.size() + " ARS pieces");
        check(before.blockEntities.size() >= 12, "fixture block entities: " + before.blockEntities.keySet());
        check(before.entities.size() >= 14, "fixture entities: " + before.entityTypes());

        TardisPlotMigration.Report report = migrate(level);

        check(report.migrated() >= 1, "nothing migrated: " + report);
        Vec3i delta = TardisPlotAllocator.migrationDelta(tardis.id);
        check(TardisPlotAllocator.plotOrigin(tardis.id).equals(TardisPlotAllocator.plotOrigin(tardis.slot)),
                "pre-assigned slot was not kept");
        before.assertMovedTo(level, tardis.legacyBox(), delta);
        assertEmpty(level, tardis.legacyBox(), "legacy window");
        check(level.getBlockState(controlBlock).is(Blocks.GOLD_BLOCK), "neighbouring legacy window block was touched");
        check(!controlEntity.isRemoved() && controlEntity.blockPosition().equals(controlBlock.above()),
                "neighbouring legacy window entity was touched");
        // Console control hitboxes are never saved (the console re-spawns them, bound to itself, as it ticks), so
        // they are not copied; the stale one bound to the legacy console is gone with the legacy window.
        BlockPos legacyConsole = tardis.legacyAnchor.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        check(control.isRemoved(), "stale console control left at the legacy console");
        check(level.getEntitiesOfClass(ConsoleControlInteractionEntity.class, tardis.legacyBox().aabb(level),
                entity -> entity.isBoundTo(legacyConsole)).isEmpty(), "console controls still bound to the legacy console");
        // The model is migrated; its ARS layout keeps its meaning, so growing at the new anchor works.
        TardisDataModel model = tardis.model();
        check(!model.isLegacyPlot() && model.isPlotPrepared(), "model not marked migrated");
        check(piecesBefore.equals(model.getArsPieces()), "ARS layout changed: " + model.getArsPieces());
        BlockPos newAnchor = TardisPlotAllocator.plotOrigin(tardis.id);
        check(ArsService.isInteriorGenerated(level, newAnchor), "ARS does not see the console room at the new anchor");
        ArsService.Result grow = ArsService.grow(level, newAnchor, tardis.id, piece("corridor_straight"), 2, 4, 1, true);
        check(grow.success(), "growing at the new anchor refused: " + grow.translationKey());

        // A second run (the next world load) changes nothing.
        Recording after = Recording.of(level, tardis.newBox());
        check(TardisPlotMigration.Report.NOTHING == migrate(level), "second run migrated again");
        after.assertMovedTo(level, tardis.newBox(), Vec3i.ZERO);
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_ExteriorEntranceAndEntryUseTheNewPlot(GameTestHelper context) {
        ServerLevel level = interior(context);
        Legacy tardis = legacyTardis(level, 42, 43, new PlotSlot(-14, 12));
        tardis.buildInterior(level, false);
        BlockPos legacyEntrance = tardis.legacyAnchor.offset(FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
        BlockPos marker = tardis.legacyAnchor.offset(2, 1, 2);
        level.setBlock(marker, Blocks.EMERALD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);

        migrate(level);

        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(context.absolutePos(new BlockPos(1, 2, 1))) instanceof TardisBlockEntity exterior)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        exterior.restoreTravelIdentity(tardis.id, legacyEntrance, true);
        BlockPos entrance = TardisInteriorService.refreshEntrance(exterior);
        BlockPos expected = TardisPlotAllocator.plotOrigin(tardis.id).offset(FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
        check(expected.equals(entrance), "entrance " + entrance + ", expected " + expected);
        check(expected.equals(exterior.getInteriorEntrance()), "exterior kept the legacy entrance");
        // Entering uses the migrated room as it is: its floor is there, so it is not regenerated over.
        check(!level.getBlockState(entrance.below()).isAir(), "no floor at the new entrance");
        check(level.getBlockState(marker.offset(TardisPlotAllocator.migrationDelta(tardis.id))).is(Blocks.EMERALD_BLOCK),
                "migrated interior missing");
        check(TardisDataLoader.get(tardis.id).isPlotPrepared(), "migrated plot would be cleared on first entry");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_ResumesAfterACrashInEveryPhase(GameTestHelper context) {
        ServerLevel level = interior(context);
        // Crash before the first clear (snapshots on disk), before the first new-window prepare (legacy windows
        // cleared), and before the second paste (one interior already pasted).
        String[] crashes = {"clear", "prepare", "paste#2"};
        for (int i = 0; i < crashes.length; i++) {
            Legacy first = legacyTardis(level, 44 + 4 * i, 45, new PlotSlot(15 + i, 16));
            Legacy second = legacyTardis(level, 46 + 4 * i, 45, new PlotSlot(15 + i, -16));
            first.buildInterior(level, false);
            second.buildInterior(level, false);
            Recording firstBefore = Recording.of(level, first.legacyBox());
            Recording secondBefore = Recording.of(level, second.legacyBox());
            Path directory = Objects.requireNonNull(TardisPlotMigration.directory());
            MigrationWorld crashing = new CrashingWorld(new ServerMigrationWorld(level.getServer(), level), crashes[i]);
            try {
                TardisPlotMigration.run(crashing, new ServerMigrationWorld.FileSnapshotStore(directory, level));
                throw new AssertionError("expected a crash at " + crashes[i]);
            } catch (CrashingWorld.Crash expected) {
                LOGGER.info("Simulated crash at {} in phase {}", crashes[i], TardisPlotMigration.phase());
            }
            // Restart: forget cached state and resume from what is on disk.
            TardisPlotMigration.reset();
            TardisPlotMigration.Report resumed = migrate(level);
            check(resumed.resumedFrom() != TardisPlotMigration.Phase.NONE || crashes[i].equals("clear"),
                    "crash at " + crashes[i] + " resumed from " + resumed.resumedFrom());
            check(TardisPlotMigration.phase() == TardisPlotMigration.Phase.DONE, "not done after crash at " + crashes[i]);
            firstBefore.assertMovedTo(level, first.legacyBox(), TardisPlotAllocator.migrationDelta(first.id));
            secondBefore.assertMovedTo(level, second.legacyBox(), TardisPlotAllocator.migrationDelta(second.id));
            assertEmpty(level, first.legacyBox(), "first legacy window after crash at " + crashes[i]);
            assertEmpty(level, second.legacyBox(), "second legacy window after crash at " + crashes[i]);
            check(!Files.exists(directory.resolve(first.id + ".nbt")), "snapshot left behind after " + crashes[i]);
        }
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_LegacyWindowInsideAnotherNewWindowSurvives(GameTestHelper context) {
        ServerLevel level = interior(context);
        // A's new plot is slot (7, 9): anchor (70000, 64, 90000), window x 65005..75004, z 85005..95004.
        Legacy a = legacyTardis(level, 56, 57, new PlotSlot(7, 9));
        // B's legacy anchor (1134 * 64, 1446 * 64) = (72576, 92544) lies inside A's new window.
        Legacy b = legacyTardis(level, 1134, 1446, new PlotSlot(-7, 9));
        check(TardisPlotAllocator.isInsideWindow(b.legacyAnchor, TardisPlotAllocator.plotOrigin(a.slot)),
                "fixture: B's legacy plot must be inside A's new window");
        a.buildInterior(level, false);
        b.buildInterior(level, true);
        Recording aBefore = Recording.of(level, a.legacyBox());
        Recording bBefore = Recording.of(level, b.legacyBox());

        migrate(level);

        aBefore.assertMovedTo(level, a.legacyBox(), TardisPlotAllocator.migrationDelta(a.id));
        bBefore.assertMovedTo(level, b.legacyBox(), TardisPlotAllocator.migrationDelta(b.id));
        assertEmpty(level, b.legacyBox(), "B's legacy window inside A's new plot");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_MovesPendingPlayersAndRespawnPointsOnJoin(GameTestHelper context) {
        ServerLevel level = interior(context);
        Legacy tardis = legacyTardis(level, 60, 61, new PlotSlot(-15, -15));
        tardis.buildInterior(level, false);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        try {
            BlockPos legacyFeet = tardis.legacyAnchor.offset(FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
            // Saved before the migration: the player's file is in the world's player data.
            MinecraftServer server = level.getServer();
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID() + ".dat");
            Files.createDirectories(playerFile.getParent());
            if (!Files.exists(playerFile)) {
                Files.write(playerFile, new byte[0]);
            }

            migrate(level);
            check(TardisPlotMigration.isPlayerPending(player.getUUID()), "player saved before the migration not pending");

            TardisTeleport.teleport(player, level, legacyFeet, player.getYRot());
            player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(
                    GlobalPos.of(level.dimension(), legacyFeet), 0.0F, 0.0F), true), false);
            TardisPlotMigrationHooks.onJoin(player, level.dimension());

            BlockPos expected = legacyFeet.offset(TardisPlotAllocator.migrationDelta(tardis.id));
            check(player.blockPosition().equals(expected), "player at " + player.blockPosition() + ", expected " + expected);
            check(expected.equals(player.getRespawnConfig().respawnData().globalPos().pos()), "respawn point not moved");
            check(!TardisPlotMigration.isPlayerPending(player.getUUID()), "player still pending");

            // Later joins never move the player again, even back inside the old window.
            TardisTeleport.teleport(player, level, legacyFeet, player.getYRot());
            TardisPlotMigrationHooks.onJoin(player, level.dimension());
            check(player.blockPosition().equals(legacyFeet), "player moved twice");
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        context.succeed();
    }

    // ---- Leftover clearing ---------------------------------------------------------------------------------

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void preparer_ClearsLeftoversBeforeAFreshInterior(GameTestHelper context) {
        ServerLevel level = interior(context);
        UUID id = TardisDataLoader.create().uuid;
        BlockPos anchor = TardisPlotAllocator.plotOrigin(id);
        InteriorBlockOps.Box window = TardisPlotPreparer.windowBox(anchor);
        // Junk across several chunks and regions of the window: an orphaned console room, a chest full of items, a
        // mob, an item frame and blocks on the window's corners and at the lowest and highest heights.
        List<BlockPos> junk = new ArrayList<>(List.of(
                anchor.offset(-30, 1, -30), anchor.offset(140, 70, -800),
                new BlockPos(window.minX(), 64, window.minZ()), new BlockPos(window.maxX(), 64, window.maxZ()),
                new BlockPos(window.minX(), level.getMinY() + 8, window.maxZ()),
                new BlockPos(window.maxX(), level.getMaxY(), window.minZ())));
        for (BlockPos pos : junk) {
            level.getChunk(pos);
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockPos orphan = anchor.offset(-1500, 0, 2200);
        level.getChunk(orphan);
        FirstDoctorConsoleRoomPlacer.place(level, orphan, UUID.randomUUID());
        junk.add(orphan.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE));
        BlockPos chest = anchor.offset(400, 2, 400);
        level.getChunk(chest);
        command(level, "setblock " + xyz(chest) + " minecraft:chest{Items:[{Slot:0b,id:\"minecraft:diamond\",count:64}]}");
        check(level.getBlockState(chest).is(Blocks.CHEST), "fixture chest");
        junk.add(chest);
        Entity zombie = spawnDirect(level, net.minecraft.world.entity.EntityTypes.SHEEP, anchor.offset(-3000, 70, 10));
        level.setBlock(chest.above().north(), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
        junk.add(chest.above().north());
        Entity frame = summon(level, "item_frame", Vec3.atCenterOf(chest.above()), "{Facing:3b,Item:{id:\"minecraft:clock\",count:1}}");
        // Just outside the window, in the neighbouring slot: must be untouched.
        BlockPos outside = new BlockPos(window.maxX() + 1, 64, window.minZ() + 20);
        level.getChunk(outside);
        level.setBlock(outside, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        Entity outsider = spawnDirect(level, net.minecraft.world.entity.EntityTypes.ARMOR_STAND, outside.above());

        // First placement: what TardisInteriorService.placeInterior does before stamping the console room.
        TardisInteriorService.preparePlotOnce(level, id, anchor);
        FirstDoctorConsoleRoomPlacer.place(level, anchor, id);

        for (BlockPos pos : junk) {
            check(level.getBlockState(pos).isAir(), "leftover at " + pos + ": " + level.getBlockState(pos));
        }
        check(zombie.isRemoved() && frame.isRemoved(), "leftover entities remain");
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, window.aabb(level), item -> true);
        check(drops.isEmpty(), "clearing dropped items: " + drops);
        check(level.getBlockState(outside).is(Blocks.GOLD_BLOCK) && !outsider.isRemoved(), "the neighbouring slot was touched");
        check(level.getBlockState(anchor.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE)).is(DWMBlocks.FIRST_DOCTOR_CONSOLE),
                "console room not placed after clearing");
        check(TardisDataLoader.get(id).isPlotPrepared(), "plot not marked prepared");
        // Later placements never scan or clear again: a block placed in the window now stays.
        BlockPos later = anchor.offset(300, 1, 300);
        level.setBlock(later, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_CLIENTS);
        TardisInteriorService.preparePlotOnce(level, id, anchor);
        check(level.getBlockState(later).is(Blocks.OBSIDIAN), "the plot was cleared a second time");
        context.succeed();
    }

    @GameTest(structure = STRUCTURE, maxTicks = 400)
    public void migration_ClearsLeftoversInTheNewWindowBeforePasting(GameTestHelper context) {
        ServerLevel level = interior(context);
        Legacy tardis = legacyTardis(level, 62, 63, new PlotSlot(-16, 17));
        tardis.buildInterior(level, false);
        BlockPos anchor = TardisPlotAllocator.plotOrigin(tardis.slot);
        InteriorBlockOps.Box window = TardisPlotPreparer.windowBox(anchor);
        BlockPos junkAtRoom = anchor.offset(3, 1, 3);
        BlockPos junkFar = new BlockPos(window.maxX(), 100, window.maxZ());
        for (BlockPos pos : List.of(junkAtRoom, junkFar)) {
            level.getChunk(pos);
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        Entity zombie = spawnDirect(level, net.minecraft.world.entity.EntityTypes.SHEEP, anchor.offset(-900, 70, 4000));
        Recording before = Recording.of(level, tardis.legacyBox());

        migrate(level);

        before.assertMovedTo(level, tardis.legacyBox(), TardisPlotAllocator.migrationDelta(tardis.id));
        check(level.getBlockState(junkFar).isAir(), "leftover in the far corner of the new window");
        check(zombie.isRemoved(), "leftover mob in the new window");
        context.succeed();
    }

    // ---- Fixture -------------------------------------------------------------------------------------------

    /** A TARDIS saved before 10000-block plots, with its legacy anchor at {@code (gridX, gridZ) * 64}. */
    private record Legacy(UUID id, BlockPos legacyAnchor, PlotSlot slot) {
        TardisDataModel model() {
            return Objects.requireNonNull(TardisDataLoader.get(id));
        }

        InteriorBlockOps.Box legacyBox() {
            return InteriorBlockOps.legacyWindowBox(legacyAnchor);
        }

        InteriorBlockOps.Box newBox() {
            return legacyBox().moved(TardisPlotAllocator.migrationDelta(id));
        }

        /** The console room and two ARS pieces at the legacy anchor; {@code rich} adds the full fixture. */
        void buildInterior(ServerLevel level, boolean rich) {
            InteriorBlockOps.loadChunks(level, legacyBox());
            FirstDoctorConsoleRoomPlacer.place(level, legacyAnchor, id);
            grow(level, "corridor_straight", 5, 4, 1);
            grow(level, "corridor_straight", 6, 4, 1);
            if (rich) {
                buildFixture(level, legacyAnchor);
            }
        }

        private void grow(ServerLevel level, String piece, int x, int z, int rotation) {
            ArsService.Result result = ArsService.grow(level, legacyAnchor, id, piece(piece), x, z, rotation, true);
            check(result.success(), "fixture grow " + piece + " refused: " + result.translationKey());
        }

        ConsoleControlInteractionEntity spawnConsoleControl(ServerLevel level) {
            BlockPos console = legacyAnchor.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
            ConsoleControlInteractionEntity control = new ConsoleControlInteractionEntity(DWMEntityTypes.CONSOLE_CONTROL, level);
            control.setConsolePos(console);
            control.setLookTarget(FirstDoctorConsoleControls.LookTarget.interactiveValues()[0]);
            control.setPos(console.getX() + 0.5, console.getY() + 1.2, console.getZ() + 0.5);
            check(level.addFreshEntity(control), "fixture console control");
            return control;
        }
    }

    /**
     * Rich content in the free north-west corner of the legacy window (anchor-local x -25..-6, z -25..-6), plus
     * blocks on the window's edges and at the lowest and highest heights.
     */
    private static void buildFixture(ServerLevel level, BlockPos anchor) {
        BlockPos base = anchor.offset(-25, 0, -25);
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        // Block entities with rich data.
        command(level, "setblock " + xyz(base.offset(1, 1, 1)) + " minecraft:chest[facing=east]{CustomName:\"Spares\","
                + "Items:[{Slot:0b,id:\"minecraft:diamond\",count:5},{Slot:13b,id:\"minecraft:written_book\",count:1,"
                + "components:{\"minecraft:written_book_content\":{title:\"Log\",author:\"Doctor\",pages:[\"Day one\"]}}}]}");
        command(level, "setblock " + xyz(base.offset(4, 1, 1)) + " minecraft:barrel[facing=up]{Items:[{Slot:3b,id:\"minecraft:apple\",count:12}]}");
        command(level, "setblock " + xyz(base.offset(7, 1, 1)) + " minecraft:blue_shulker_box{Items:[{Slot:26b,id:\"minecraft:ender_pearl\",count:16}]}");
        command(level, "setblock " + xyz(base.offset(10, 1, 1)) + " minecraft:furnace[facing=south,lit=true]{Items:[{Slot:0b,"
                + "id:\"minecraft:iron_ore\",count:7},{Slot:1b,id:\"minecraft:coal\",count:3},{Slot:2b,id:\"minecraft:iron_ingot\","
                + "count:2}],lit_time_remaining:120s,cooking_time_spent:40s,cooking_total_time:200s,lit_total_time:1600s}");
        command(level, "setblock " + xyz(base.offset(13, 1, 1)) + " minecraft:oak_sign[rotation=4]{front_text:{messages:"
                + "[\"Bigger\",\"on the\",\"inside\",\"\"]},back_text:{messages:[\"Back\",\"\",\"\",\"\"],has_glowing_text:1b},is_waxed:1b}");
        command(level, "setblock " + xyz(base.offset(16, 1, 1)) + " minecraft:white_banner[rotation=8]{patterns:"
                + "[{color:\"blue\",pattern:\"minecraft:stripe_top\"},{color:\"red\",pattern:\"minecraft:cross\"}]}");
        command(level, "setblock " + xyz(base.offset(1, 1, 4)) + " minecraft:lectern[facing=north,has_book=true]{Book:"
                + "{id:\"minecraft:writable_book\",count:1,components:{\"minecraft:writable_book_content\":{pages:[\"Notes\"]}}},Page:0}");
        command(level, "setblock " + xyz(base.offset(4, 1, 4)) + " minecraft:decorated_pot{sherds:[\"minecraft:brick\","
                + "\"minecraft:archer_pottery_sherd\",\"minecraft:brick\",\"minecraft:skull_pottery_sherd\"],"
                + "item:{id:\"minecraft:emerald\",count:2}}");
        level.setBlock(base.offset(7, 1, 4), Blocks.POTTED_RED_TULIP.defaultBlockState(), Block.UPDATE_CLIENTS);
        command(level, "setblock " + xyz(base.offset(10, 1, 4)) + " minecraft:beehive[facing=west]{flower_pos:"
                + "[I;" + xyz3(base.offset(10, 1, 6)) + "]}");
        level.setBlock(base.offset(10, 1, 6), Blocks.DANDELION.defaultBlockState(), Block.UPDATE_CLIENTS);
        command(level, "setblock " + xyz(base.offset(13, 1, 4)) + " minecraft:player_head{profile:{name:\"Steve\"}}");
        command(level, "setblock " + xyz(base.offset(16, 1, 4)) + " minecraft:jukebox{RecordItem:{id:\"minecraft:music_disc_cat\",count:1}}");
        // DWM block entities: interior doors, monitor and decor are part of the console room the placer stamped.

        // Block states placed without neighbour updates, so they are kept exactly.
        int quiet = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(base.offset(1, 1, 8), Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.EAST)
                .setValue(net.minecraft.world.level.block.StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP)
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true), quiet);
        level.setBlock(base.offset(3, 1, 8), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, true), quiet);
        level.setBlock(base.offset(3, 2, 8), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, true), quiet);
        command(level, "setblock " + xyz(base.offset(5, 1, 8)) + " minecraft:red_bed[part=foot,facing=south]");
        command(level, "setblock " + xyz(base.offset(5, 1, 9)) + " minecraft:red_bed[part=head,facing=south]");
        level.setBlock(base.offset(7, 1, 8), Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER, 7), quiet);
        level.setBlock(base.offset(9, 1, 8), Blocks.REPEATER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.RepeaterBlock.DELAY, 3)
                .setValue(net.minecraft.world.level.block.RepeaterBlock.FACING, net.minecraft.core.Direction.WEST), quiet);
        level.setBlock(base.offset(11, 1, 8), Blocks.WATER.defaultBlockState(), quiet);
        level.setBlock(base.offset(13, 1, 8), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);

        // Window edges, and the lowest and highest buildable heights.
        BlockPos window = TardisPlotAllocator.legacyWindowOrigin(anchor);
        int last = TardisPlotAllocator.LEGACY_PLOT_SPACING - 1;
        level.setBlock(window.offset(0, 0, 0), Blocks.GLASS.defaultBlockState(), quiet);
        level.setBlock(window.offset(last, 0, 0), Blocks.GLASS.defaultBlockState(), quiet);
        level.setBlock(window.offset(0, 0, last), Blocks.GLASS.defaultBlockState(), quiet);
        level.setBlock(window.offset(last, 0, last), Blocks.GLASS.defaultBlockState(), quiet);
        level.setBlock(new BlockPos(window.getX() + 3, level.getMinY(), window.getZ() + 3), Blocks.BEDROCK.defaultBlockState(), quiet);
        level.setBlock(new BlockPos(window.getX() + 3, level.getMaxY(), window.getZ() + 3), Blocks.SEA_LANTERN.defaultBlockState(), quiet);

        // Entities.
        Vec3 row = Vec3.atBottomCenterOf(base.offset(1, 1, 12));
        summon(level, "armor_stand", row, "{NoGravity:1b,ShowArms:1b,CustomName:\"Butler\",equipment:{head:"
                + "{id:\"minecraft:diamond_helmet\",count:1},mainhand:{id:\"minecraft:clock\",count:1}},Pose:{Head:[10f,20f,0f]}}");
        BlockPos frameWall = base.offset(3, 1, 13);
        level.setBlock(frameWall, Blocks.OAK_PLANKS.defaultBlockState(), quiet);
        summon(level, "item_frame", Vec3.atCenterOf(frameWall.north()), "{Facing:2b,ItemRotation:3b,Item:{id:\"minecraft:compass\",count:1}}");
        summon(level, "glow_item_frame", Vec3.atCenterOf(frameWall.above().north()), "{Facing:2b,Item:{id:\"minecraft:map\",count:1}}");
        BlockPos paintingWall = base.offset(6, 1, 13);
        level.setBlock(paintingWall, Blocks.OAK_PLANKS.defaultBlockState(), quiet);
        level.setBlock(paintingWall.above(), Blocks.OAK_PLANKS.defaultBlockState(), quiet);
        summon(level, "painting", Vec3.atCenterOf(paintingWall.north()), "{facing:2b,variant:\"minecraft:kebab\"}");
        BlockPos fence = base.offset(13, 1, 8);
        summon(level, "leash_knot", Vec3.atBottomCenterOf(fence), "{}");
        summon(level, "pig", Vec3.atBottomCenterOf(base.offset(13, 1, 10)), "{NoAI:1b,leash:[I;" + xyz3(fence) + "]}");
        summon(level, "minecart", Vec3.atBottomCenterOf(base.offset(9, 1, 12)), "{Passengers:[{id:\"minecraft:pig\",NoAI:1b,CustomName:\"Rider\"}]}");
        summon(level, "chest_minecart", Vec3.atBottomCenterOf(base.offset(11, 1, 12)), "{Items:[{Slot:5b,id:\"minecraft:gold_ingot\",count:9}]}");
        summon(level, "item", Vec3.atBottomCenterOf(base.offset(13, 1, 12)), "{Item:{id:\"minecraft:apple\",count:3},PickupDelay:32767s,Age:-32768s}");
        summon(level, "villager", Vec3.atBottomCenterOf(base.offset(15, 1, 12)), "{NoAI:1b,CustomName:\"Bob\",VillagerData:"
                + "{profession:\"minecraft:librarian\",level:2,type:\"minecraft:plains\"},Brain:{memories:{\"minecraft:home\":"
                + "{value:{dimension:\"dwm:tardis\",pos:[I;" + xyz3(base.offset(5, 1, 8)) + "]}}}}}");
        summon(level, "wolf", Vec3.atBottomCenterOf(base.offset(17, 1, 12)), "{NoAI:1b,Owner:[I;1,2,3,4],Sitting:1b,CollarColor:5b}");
        // Hostile mobs cannot be summoned on the GameTest server's peaceful difficulty; create them directly.
        spawnDirect(level, net.minecraft.world.entity.EntityTypes.ZOMBIE, base.offset(1, 1, 16));
        spawnDirect(level, DWMEntityTypes.DALEK, base.offset(4, 1, 16));
        summon(level, "fox", Vec3.atBottomCenterOf(base.offset(7, 1, 16)), "{NoAI:1b,PersistenceRequired:1b,Type:\"snow\","
                + "equipment:{mainhand:{id:\"minecraft:iron_sword\",count:1}}}");
    }

    // ---- Recording -----------------------------------------------------------------------------------------

    /** Every block, block entity and non-player entity in a box, recorded so a migrated copy can be compared. */
    private static final class Recording {
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        final Map<BlockPos, CompoundTag> blockEntities = new LinkedHashMap<>();
        final Map<UUID, CompoundTag> entities = new LinkedHashMap<>();
        final Map<UUID, EntityType<?>> types = new HashMap<>();
        final Map<UUID, Vec3> positions = new HashMap<>();
        final Map<UUID, List<UUID>> passengers = new HashMap<>();

        static Recording of(ServerLevel level, InteriorBlockOps.Box box) {
            InteriorBlockOps.loadChunks(level, box);
            Recording recording = new Recording();
            InteriorBlockOps.forEachBlock(level, box, (pos, state) -> {
                BlockPos immutable = pos.immutable();
                recording.blocks.put(immutable, state);
                BlockEntity blockEntity = level.getBlockEntity(immutable);
                if (blockEntity != null) {
                    recording.blockEntities.put(immutable, blockEntity.saveWithoutMetadata(level.registryAccess()));
                }
            });
            // Entities vanilla never saves (leash knots are rebuilt from the leashed mob's leash position) are not
            // part of the world's saved state, so they are not expected to be copied either.
            for (Entity entity : level.getEntitiesOfClass(Entity.class, box.aabb(level),
                    entity -> !(entity instanceof Player) && entity.getType().canSerialize()
                            && box.contains(entity.blockPosition()))) {
                recording.entities.put(entity.getUUID(), save(level, entity));
                recording.types.put(entity.getUUID(), entity.getType());
                recording.positions.put(entity.getUUID(), entity.position());
                recording.passengers.put(entity.getUUID(), entity.getPassengers().stream().map(Entity::getUUID).toList());
            }
            return recording;
        }

        List<String> entityTypes() {
            return types.values().stream().map(type -> EntityType.getKey(type).toString()).sorted().toList();
        }

        /** Asserts the content of {@code source} is now at {@code source + delta}, exactly, and nothing else is. */
        void assertMovedTo(ServerLevel level, InteriorBlockOps.Box source, Vec3i delta) {
            LegacyPositionRewriter rewriter = new LegacyPositionRewriter(source::contains, delta);
            InteriorBlockOps.Box target = source.moved(delta);
            Recording now = of(level, target);
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos moved = entry.getKey().offset(delta);
                BlockState actual = level.getBlockState(moved);
                check(entry.getValue().equals(actual), "block at " + moved + " is " + actual + ", expected " + entry.getValue());
            }
            check(now.blocks.size() == blocks.size(), "target holds " + now.blocks.size() + " blocks, expected " + blocks.size());
            for (Map.Entry<BlockPos, CompoundTag> entry : blockEntities.entrySet()) {
                BlockPos moved = entry.getKey().offset(delta);
                CompoundTag expected = rewriter.rewrite(entry.getValue());
                CompoundTag actual = now.blockEntities.get(moved);
                check(expected.equals(actual), "block entity at " + moved + ":\n  expected " + expected + "\n  actual   " + actual);
            }
            check(now.blockEntities.size() == blockEntities.size(), "target block entities " + now.blockEntities.keySet());
            for (Map.Entry<UUID, CompoundTag> entry : entities.entrySet()) {
                UUID id = entry.getKey();
                Entity entity = level.getEntity(id);
                check(entity != null && !entity.isRemoved(), "entity " + id + " (" + types.get(id) + ") was not moved");
                check(entity.getType() == types.get(id), "entity " + id + " changed type to " + entity.getType());
                Vec3 expectedPos = positions.get(id).add(delta.getX(), delta.getY(), delta.getZ());
                check(entity.position().distanceTo(expectedPos) < 1.0E-6,
                        types.get(id) + " " + id + " at " + entity.position() + ", expected " + expectedPos);
                check(passengers.get(id).equals(entity.getPassengers().stream().map(Entity::getUUID).toList()),
                        types.get(id) + " lost its passengers");
                CompoundTag expected = normalised(level, rewriter.rewrite(entry.getValue()), types.get(id));
                CompoundTag actual = save(level, entity);
                check(expected.equals(actual), EntityType.getKey(types.get(id)) + " NBT changed:\n  expected " + expected
                        + "\n  actual   " + actual);
            }
            check(now.entities.size() == entities.size(), "target holds entities " + now.entityTypes() + ", expected " + entityTypes());
        }

        /**
         * {@code tag} after one vanilla load and save (without adding the entity to the world). Some entities save
         * differently once loaded (a baby zombie's speed attribute, for one); that asymmetry is vanilla's, so the
         * expected NBT goes through it too and the comparison only sees what the migration changed.
         */
        private static CompoundTag normalised(ServerLevel level, CompoundTag tag, EntityType<?> type) {
            CompoundTag withId = tag.copy();
            withId.putString("id", EntityType.getKey(type).toString());
            Entity loaded = EntityType.loadEntityRecursive(withId, level,
                    new net.minecraft.world.entity.EntitySpawnRequest(net.minecraft.world.entity.EntitySpawnReason.LOAD, true),
                    entity -> entity);
            check(loaded != null, "could not reload " + type);
            return save(level, loaded);
        }

        private static CompoundTag save(ServerLevel level, Entity entity) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
                entity.saveWithoutId(output);
                return output.buildResult();
            }
        }
    }

    private static void assertEmpty(ServerLevel level, InteriorBlockOps.Box box, String what) {
        int[] count = {0};
        InteriorBlockOps.forEachBlock(level, box, (pos, state) -> count[0]++);
        check(count[0] == 0, what + " still holds " + count[0] + " blocks");
        List<Entity> left = level.getEntitiesOfClass(Entity.class, box.aabb(level),
                entity -> !(entity instanceof Player) && box.contains(entity.blockPosition()));
        check(left.isEmpty(), what + " still holds entities " + left);
    }

    // ---- Helpers -------------------------------------------------------------------------------------------

    /**
     * The level the interiors are built in. The GameTest server does not load {@code dwm:tardis}, so (like the other
     * interior GameTests) these use the GameTest level, which the migration code treats the same way.
     */
    private static ServerLevel interior(GameTestHelper context) {
        redirectSaves(context);
        return context.getLevel();
    }

    private static void redirectSaves(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
    }

    private static TardisPlotMigration.Report migrate(ServerLevel level) {
        Path directory = Objects.requireNonNull(TardisPlotMigration.directory());
        return TardisPlotMigration.run(new ServerMigrationWorld(level.getServer(), level),
                new ServerMigrationWorld.FileSnapshotStore(directory, level));
    }

    /**
     * A legacy save for a fresh id whose legacy anchor is {@code (gridX, gridZ) * 64} and whose new plot is
     * {@code slot} (pre-assigned, so tests never share plots). The model is written without {@code plotVersion}, as
     * an old save would be.
     */
    private static Legacy legacyTardis(ServerLevel level, int gridX, int gridZ, PlotSlot slot) {
        long high = RANDOM.nextLong() & ~0xFFFFL;
        UUID id = new UUID(high | gridX, (RANDOM.nextLong() & ~0xFFFFL) | gridZ);
        TardisDataModel model = new TardisDataModel();
        model.uuid = id;
        model.plotSlotX = slot.x();
        model.plotSlotZ = slot.z();
        Path file = TardisDataLoader.tardisSaveDirectory.resolve(id + ".json");
        try {
            Files.createDirectories(file.getParent());
            try (FileWriter writer = new FileWriter(file.toFile())) {
                new Gson().toJson(model, writer);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        TardisDataModel loaded = TardisDataLoader.get(id);
        check(loaded != null && loaded.isLegacyPlot(), "legacy model did not load as legacy");
        check(slot.equals(TardisPlotSlots.storedSlot(loaded)), "pre-assigned slot");
        BlockPos legacyAnchor = TardisPlotAllocator.legacyPlotOrigin(id);
        check(legacyAnchor.equals(new BlockPos(gridX * 64, 64, gridZ * 64)), "legacy anchor " + legacyAnchor);
        return new Legacy(id, legacyAnchor, slot);
    }

    private static Identifier piece(String name) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, name);
    }

    private static void command(ServerLevel level, String command) {
        CommandSourceStack source = level.getServer().createCommandSourceStack().withLevel(level).withSuppressedOutput();
        level.getServer().getCommands().performPrefixedCommand(source, command);
    }

    private static Entity summon(ServerLevel level, String type, Vec3 pos, String nbt) {
        level.getChunk(BlockPos.containing(pos));
        Set<UUID> before = new java.util.HashSet<>();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new net.minecraft.world.phys.AABB(BlockPos.containing(pos)).inflate(2))) {
            before.add(entity.getUUID());
        }
        command(level, "summon " + type + " " + pos.x + " " + pos.y + " " + pos.z + " " + nbt);
        Identifier typeId = type.contains(":") ? Identifier.parse(type) : Identifier.withDefaultNamespace(type);
        Optional<Entity> summoned = level.getEntitiesOfClass(Entity.class, new net.minecraft.world.phys.AABB(BlockPos.containing(pos)).inflate(2),
                        entity -> !before.contains(entity.getUUID()) && EntityType.getKey(entity.getType()).equals(typeId))
                .stream().findFirst();
        if (summoned.isEmpty()) {
            throw new AssertionError("fixture: could not summon " + type + " " + nbt);
        }
        return summoned.get();
    }

    private static Entity spawnDirect(ServerLevel level, EntityType<?> type, BlockPos pos) {
        level.getChunk(pos);
        Entity entity = type.create(level, new net.minecraft.world.entity.EntitySpawnRequest(
                net.minecraft.world.entity.EntitySpawnReason.COMMAND, true));
        check(entity != null, "fixture: could not create " + type);
        entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 90.0F, 0.0F);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }
        check(level.addFreshEntity(entity), "fixture: could not add " + type);
        return entity;
    }

    private static String xyz(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String xyz3(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** Delegates to a real world but throws before the {@code n}-th call of one operation ({@code "op"} or {@code "op#n"}). */
    private static final class CrashingWorld implements MigrationWorld {
        static final class Crash extends RuntimeException {
            Crash(String message) {
                super(message);
            }
        }

        private final MigrationWorld delegate;
        private final String op;
        private final int nth;
        private final Map<String, Integer> counts = new HashMap<>();

        CrashingWorld(MigrationWorld delegate, String crashAt) {
            this.delegate = delegate;
            String[] parts = crashAt.split("#");
            this.op = parts[0];
            this.nth = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        }

        private void step(String name) {
            int count = counts.merge(name, 1, Integer::sum);
            if (name.equals(op) && count == nth) {
                throw new Crash("simulated crash at " + name + " #" + count);
            }
        }

        @Override
        public InteriorSnapshot capture(UUID tardisId) {
            step("capture");
            return delegate.capture(tardisId);
        }

        @Override
        public void clearLegacy(UUID tardisId) {
            step("clear");
            delegate.clearLegacy(tardisId);
        }

        @Override
        public void prepareNew(UUID tardisId) {
            step("prepare");
            delegate.prepareNew(tardisId);
        }

        @Override
        public void paste(UUID tardisId, InteriorSnapshot snapshot) {
            step("paste");
            delegate.paste(tardisId, snapshot);
        }

        @Override
        public void flush() {
            step("flush");
            delegate.flush();
        }

        @Override
        public Set<UUID> knownPlayers() {
            return delegate.knownPlayers();
        }
    }
}
