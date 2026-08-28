package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * First-hour teaching tree under a single Doctor Who tab.
 */
public class DWMAdvancementProvider extends FabricAdvancementProvider {
    private static final Identifier GALLIFREY_STONE_BACKGROUND =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "block/gallifrey_stone");

    public DWMAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        DWMBlocks.GALLIFREY_GRASS_BLOCK,
                        Component.translatable("advancements.dwm.root"),
                        Component.translatable("advancements.dwm.root.description"),
                        GALLIFREY_STONE_BACKGROUND,
                        AdvancementType.TASK,
                        false,
                        false,
                        false
                )
                .addCriterion("tick", emptyPlayerCriterion(CriteriaTriggers.TICK))
                .save(consumer, DWMReference.MOD_ID + "/root");

        AdvancementHolder obtainSonic = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        DWMItems.SONIC_THIRD_DOCTOR,
                        Component.translatable("advancements.dwm.sonic_screwdriver"),
                        Component.translatable("advancements.dwm.sonic_screwdriver.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion(
                        "sonic_screwdriver",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, DWMItemTags.SONIC_SCREWDRIVERS)
                        )
                )
                .save(consumer, DWMReference.MOD_ID + "/sonic_screwdriver");

        Advancement.Builder.advancement()
                .parent(obtainSonic)
                .display(
                        Items.IRON_DOOR,
                        Component.translatable("advancements.dwm.sonic_iron_door"),
                        Component.translatable("advancements.dwm.sonic_iron_door.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("sonic_iron_door", emptyPlayerCriterion(DWMCriteria.SONIC_IRON_DOOR))
                .save(consumer, DWMReference.MOD_ID + "/sonic_iron_door");

        Advancement.Builder.advancement()
                .parent(obtainSonic)
                .display(
                        DWMItems.SONIC_THIRD_DOCTOR,
                        Component.translatable("advancements.dwm.sonic_cycle_setting"),
                        Component.translatable("advancements.dwm.sonic_cycle_setting.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("sonic_cycle_setting", emptyPlayerCriterion(DWMCriteria.SONIC_CYCLE_SETTING))
                .save(consumer, DWMReference.MOD_ID + "/sonic_cycle_setting");

        AdvancementHolder installShatter = installSettingAdvancement(
                consumer, obtainSonic, DWMItems.SONIC_SETTING_SHATTER,
                "sonic_install_shatter", DWMCriteria.SONIC_INSTALL_SHATTER
        );
        AdvancementHolder installPrime = installSettingAdvancement(
                consumer, obtainSonic, DWMItems.SONIC_SETTING_PRIME,
                "sonic_install_prime", DWMCriteria.SONIC_INSTALL_PRIME
        );
        AdvancementHolder installDisrupt = installSettingAdvancement(
                consumer, obtainSonic, DWMItems.SONIC_SETTING_DISRUPT,
                "sonic_install_disrupt", DWMCriteria.SONIC_INSTALL_DISRUPT
        );
        AdvancementHolder installShear = installSettingAdvancement(
                consumer, obtainSonic, DWMItems.SONIC_SETTING_SHEAR,
                "sonic_install_shear", DWMCriteria.SONIC_INSTALL_SHEAR
        );

        useSettingAdvancement(consumer, installShatter, Items.GLASS_PANE, "sonic_shatter", DWMCriteria.SONIC_SHATTER);
        useSettingAdvancement(consumer, installPrime, Items.TNT, "sonic_prime", DWMCriteria.SONIC_PRIME);
        useSettingAdvancement(consumer, installDisrupt, Items.SLIME_BALL, "sonic_disrupt", DWMCriteria.SONIC_DISRUPT);
        useSettingAdvancement(consumer, installShear, Items.WOOL.white(), "sonic_shear", DWMCriteria.SONIC_SHEAR);

        Advancement.Builder.advancement()
                .parent(obtainSonic)
                .display(
                        DWMItems.SONIC_FIFTH_DOCTOR,
                        Component.translatable("advancements.dwm.sonic_all_settings"),
                        Component.translatable("advancements.dwm.sonic_all_settings.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("sonic_install_shatter", emptyPlayerCriterion(DWMCriteria.SONIC_INSTALL_SHATTER))
                .addCriterion("sonic_install_prime", emptyPlayerCriterion(DWMCriteria.SONIC_INSTALL_PRIME))
                .addCriterion("sonic_install_disrupt", emptyPlayerCriterion(DWMCriteria.SONIC_INSTALL_DISRUPT))
                .addCriterion("sonic_install_shear", emptyPlayerCriterion(DWMCriteria.SONIC_INSTALL_SHEAR))
                .save(consumer, DWMReference.MOD_ID + "/sonic_all_settings");

        AdvancementHolder findTardis = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        DWMBlocks.TARDIS_BLOCK,
                        Component.translatable("advancements.dwm.find_tardis"),
                        Component.translatable("advancements.dwm.find_tardis.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("find_tardis", emptyPlayerCriterion(DWMCriteria.FIND_TARDIS))
                .save(consumer, DWMReference.MOD_ID + "/find_tardis");

        AdvancementHolder claimTardis = Advancement.Builder.advancement()
                .parent(findTardis)
                .display(
                        DWMBlocks.FIRST_DOCTOR_CONSOLE,
                        Component.translatable("advancements.dwm.claim_tardis"),
                        Component.translatable("advancements.dwm.claim_tardis.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("claim_tardis", emptyPlayerCriterion(DWMCriteria.CLAIM_TARDIS))
                .save(consumer, DWMReference.MOD_ID + "/claim_tardis");

        Advancement.Builder.advancement()
                .parent(claimTardis)
                .display(
                        DWMBlocks.TARDIS_BLOCK,
                        Component.translatable("advancements.dwm.first_hop"),
                        Component.translatable("advancements.dwm.first_hop.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("first_hop", emptyPlayerCriterion(DWMCriteria.FIRST_HOP))
                .save(consumer, DWMReference.MOD_ID + "/first_hop");

        Advancement.Builder.advancement()
                .parent(claimTardis)
                .display(
                        DWMItems.TARDIS_KEY,
                        Component.translatable("advancements.dwm.bind_key"),
                        Component.translatable("advancements.dwm.bind_key.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("bind_key", emptyPlayerCriterion(DWMCriteria.BIND_KEY))
                .save(consumer, DWMReference.MOD_ID + "/bind_key");

        Advancement.Builder.advancement()
                .parent(claimTardis)
                .display(
                        DWMItems.ZEITON_CRYSTALS,
                        Component.translatable("advancements.dwm.first_refuel"),
                        Component.translatable("advancements.dwm.first_refuel.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("first_refuel", emptyPlayerCriterion(DWMCriteria.TARDIS_REFUEL))
                .save(consumer, DWMReference.MOD_ID + "/first_refuel");
    }

    private static AdvancementHolder installSettingAdvancement(
            Consumer<AdvancementHolder> consumer,
            AdvancementHolder parent,
            Item icon,
            String id,
            PlayerTrigger trigger
    ) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                        icon,
                        Component.translatable("advancements.dwm." + id),
                        Component.translatable("advancements.dwm." + id + ".description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion(id, emptyPlayerCriterion(trigger))
                .save(consumer, DWMReference.MOD_ID + "/" + id);
    }

    private static void useSettingAdvancement(
            Consumer<AdvancementHolder> consumer,
            AdvancementHolder parent,
            Item icon,
            String id,
            PlayerTrigger trigger
    ) {
        Advancement.Builder.advancement()
                .parent(parent)
                .display(
                        icon,
                        Component.translatable("advancements.dwm." + id),
                        Component.translatable("advancements.dwm." + id + ".description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion(id, emptyPlayerCriterion(trigger))
                .save(consumer, DWMReference.MOD_ID + "/" + id);
    }

    private static Criterion<PlayerTrigger.TriggerInstance> emptyPlayerCriterion(PlayerTrigger trigger) {
        return trigger.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }
}
