package com.adamkali.dwm.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;

import java.util.List;
import java.util.Optional;

public class InventoryContainsCriterion extends AbstractCriterion<InventoryContainsCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return null;
    }

    public static record Conditions(Optional<LootContextPredicate> player,
                                    InventoryChangedCriterion.Conditions.Slots slots,
                                    List<ItemPredicate> items) implements AbstractCriterion.Conditions {
        public static final Codec<InventoryContainsCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(InventoryContainsCriterion.Conditions::player),
                                InventoryChangedCriterion.Conditions.Slots.CODEC
                                        .optionalFieldOf("slots", InventoryChangedCriterion.Conditions.Slots.ANY)
                                        .forGetter(InventoryContainsCriterion.Conditions::slots),
                                ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(InventoryContainsCriterion.Conditions::items)
                        )
                        .apply(instance, InventoryContainsCriterion.Conditions::new)
        );
    }
}
