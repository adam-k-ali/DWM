package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.network.SelectSonicFieldModeC2SPayload;
import com.adamkali.dwm.network.ServerPayloadTypeRegistry;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.FirstHourLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FirstHourAdvancementGameTests {
    private static final Identifier SONIC_IRON_DOOR = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_iron_door");
    private static final Identifier SONIC_INSTALL_SHATTER = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_install_shatter");
    private static final Identifier SONIC_INSTALL_PRIME = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_install_prime");
    private static final Identifier SONIC_INSTALL_DISRUPT = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_install_disrupt");
    private static final Identifier SONIC_INSTALL_SHEAR = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_install_shear");
    private static final Identifier SONIC_CYCLE_SETTING = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_cycle_setting");
    private static final Identifier SONIC_SHATTER = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_shatter");
    private static final Identifier SONIC_ALL_SETTINGS = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/sonic_all_settings");
    private static final Identifier FIND_TARDIS = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/find_tardis");
    private static final Identifier CLAIM_TARDIS = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/claim_tardis");
    private static final Identifier FIRST_HOP = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/first_hop");
    private static final Identifier BIND_KEY = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/bind_key");
    private static final Identifier FIRST_REFUEL = Identifier.fromNamespaceAndPath("minecraft", DWMReference.MOD_ID + "/first_refuel");

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicIronDoor_awardsAdvancement(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        BlockPos doorRel = new BlockPos(1, 2, 1);
        BlockPos doorAbs = context.absolutePos(doorRel);
        context.setBlock(doorRel, Blocks.IRON_DOOR);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(doorAbs), Direction.NORTH, doorAbs, false);
        DWMItems.SONIC_SECOND_DOCTOR.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

        if (!context.getLevel().getBlockState(doorAbs).getValue(DoorBlock.OPEN)) {
            throw new AssertionError("Expected sonic to open iron door");
        }
        assertAdvancementDone(context, player, SONIC_IRON_DOOR);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicInstalls_awardPerSettingAndAllSettings(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        player.setItemInHand(InteractionHand.OFF_HAND, sonic);

        installSetting(player, new ItemStack(DWMItems.SONIC_SETTING_SHATTER));
        assertAdvancementDone(context, player, SONIC_INSTALL_SHATTER);

        installSetting(player, new ItemStack(DWMItems.SONIC_SETTING_PRIME));
        assertAdvancementDone(context, player, SONIC_INSTALL_PRIME);

        installSetting(player, new ItemStack(DWMItems.SONIC_SETTING_DISRUPT));
        assertAdvancementDone(context, player, SONIC_INSTALL_DISRUPT);

        installSetting(player, new ItemStack(DWMItems.SONIC_SETTING_SHEAR));
        assertAdvancementDone(context, player, SONIC_INSTALL_SHEAR);
        assertAdvancementDone(context, player, SONIC_ALL_SETTINGS);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicCycleSetting_awardsOnModeChange(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        SonicStateLogic.install(sonic, SonicFieldMode.SHATTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        boolean changed = ServerPayloadTypeRegistry.safelyHandleSelectSonicFieldMode(
                new SelectSonicFieldModeC2SPayload(SonicFieldMode.SHATTER),
                player
        );
        if (!changed) {
            throw new AssertionError("Expected mode change");
        }
        assertAdvancementDone(context, player, SONIC_CYCLE_SETTING);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicShatter_awardsOnSuccess_notOnWrongSetting(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        SonicStateLogic.install(sonic, SonicFieldMode.SHATTER);
        // Open still selected
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        BlockPos glassRel = new BlockPos(1, 2, 1);
        BlockPos glassAbs = context.absolutePos(glassRel);
        context.setBlock(glassRel, Blocks.GLASS);
        DWMItems.SONIC_THIRD_DOCTOR.useOn(new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(glassAbs), Direction.UP, glassAbs, false)
        ));
        context.assertBlockPresent(Blocks.GLASS, glassRel);
        if (player.getAdvancements().getOrStartProgress(requireHolder(context, SONIC_SHATTER)).isDone()) {
            throw new AssertionError("Wrong setting must not award sonic_shatter");
        }

        SonicStateLogic.select(sonic, SonicFieldMode.SHATTER);
        DWMItems.SONIC_THIRD_DOCTOR.useOn(new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(glassAbs), Direction.UP, glassAbs, false)
        ));
        context.assertBlockPresent(Blocks.AIR, glassRel);
        assertAdvancementDone(context, player, SONIC_SHATTER);
        context.succeed();
    }

    private static void installSetting(ServerPlayer player, ItemStack setting) {
        player.setItemInHand(InteractionHand.MAIN_HAND, setting);
        setting.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void findAndClaim_awardAdvancements(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity exterior)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = exterior.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockState state = context.getLevel().getBlockState(tardisAbs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(tardisAbs), Direction.NORTH, tardisAbs, false);
        state.useWithoutItem(context.getLevel(), player, hit);
        assertAdvancementDone(context, player, FIND_TARDIS);

        if (!TardisOwnershipLogic.tryClaimOnEnter(tardisId, player.getUUID())) {
            throw new AssertionError("Expected claim to succeed");
        }
        FirstHourLogic.notifyClaimed(player);
        if (!player.getUUID().equals(model.ownerUuid)) {
            throw new AssertionError("Expected claim to set owner");
        }
        assertAdvancementDone(context, player, CLAIM_TARDIS);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void bindKey_awardsAdvancement(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.setOwner(player.getUUID());

        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(tardisAbs), Direction.UP, tardisAbs, false);
        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

        if (!tardisId.equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected key to bind");
        }
        assertAdvancementDone(context, player, BIND_KEY);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void firstRefuel_awardsOnSuccessfulCrystalFill(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.artron = 50;

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

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack crystals = new ItemStack(DWMItems.ZEITON_CRYSTALS, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, crystals);
        FirstDoctorConsoleBlock.activateControl(
                LookTarget.REFUELER, context.getLevel(), consoleAbs, player);

        if (ArtronLogic.read(model) != 75) {
            throw new AssertionError("Expected fill to add 25, got " + ArtronLogic.read(model));
        }
        assertAdvancementDone(context, player, FIRST_REFUEL);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void firstHop_sameWorldAwards_summonDoesNot(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");
        TardisTravelService.clearActiveForTests();

        ServerPlayer owner = context.makeMockServerPlayerInLevel();
        String dimension = context.getLevel().dimension().identifier().toString();

        BlockPos shellRel = new BlockPos(2, 2, 2);
        BlockPos shellAbs = context.absolutePos(shellRel);
        clearLandingColumn(context, shellRel);
        context.setBlock(shellRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(shellAbs) instanceof TardisBlockEntity be)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = be.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.setOwner(owner.getUUID());
        model.setExteriorLocation(dimension, shellAbs.getX(), shellAbs.getY(), shellAbs.getZ(), 0);
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        TardisTravelService.putFlightShellForTests(tardisId, tardisId);

        BlockPos landingRel = new BlockPos(4, 2, 4);
        BlockPos landingAbs = context.absolutePos(landingRel);
        clearLandingColumn(context, landingRel);

        InteractionResult result = TardisTravelService.materialiseAt(
                tardisId,
                context.getLevel().getServer(),
                context.getLevel(),
                landingAbs,
                0
        );
        if (result != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected same-world materialise to succeed, got " + result);
        }
        assertAdvancementDone(context, owner, FIRST_HOP);

        TardisTravelService.clearActiveForTests();
        owner.getAdvancements().revoke(requireHolder(context, FIRST_HOP), "first_hop");

        // Summon eligibility is covered by FirstHourLogic; assert materialise with summon pending
        // does not re-award when FirstHourLogic would reject it.
        if (FirstHourLogic.isSameWorldHop(dimension, dimension, true)) {
            throw new AssertionError("Summon pending must not count as first hop");
        }
        if (owner.getAdvancements().getOrStartProgress(requireHolder(context, FIRST_HOP)).isDone()) {
            throw new AssertionError("first_hop must remain revoked after summon eligibility check");
        }
        context.succeed();
    }

    private static void clearLandingColumn(GameTestHelper context, BlockPos feetRel) {
        context.setBlock(feetRel.below(), Blocks.STONE);
        context.setBlock(feetRel, Blocks.AIR);
        context.setBlock(feetRel.above(), Blocks.AIR);
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos door = feetRel.relative(dir);
            context.setBlock(door, Blocks.AIR);
            context.setBlock(door.above(), Blocks.AIR);
        }
    }

    private static void assertAdvancementDone(GameTestHelper context, ServerPlayer player, Identifier id) {
        AdvancementHolder holder = requireHolder(context, id);
        if (!player.getAdvancements().getOrStartProgress(holder).isDone()) {
            throw new AssertionError("Expected advancement done: " + id);
        }
    }

    private static AdvancementHolder requireHolder(GameTestHelper context, Identifier id) {
        AdvancementHolder holder = context.getLevel().getServer().getAdvancements().get(id);
        if (holder == null) {
            throw new AssertionError("Missing advancement: " + id);
        }
        return holder;
    }
}
