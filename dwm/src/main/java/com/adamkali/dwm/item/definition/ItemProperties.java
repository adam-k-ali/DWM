package com.adamkali.dwm.item.definition;

import com.adamkali.dwm.data.definition.NamedValues;
import com.adamkali.dwm.item.DWMArmorMaterials;
import com.adamkali.dwm.item.DWMToolMaterials;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.Optional;

/** JSON shape of {@link Item.Properties}; absent fields keep vanilla defaults. */
public record ItemProperties(
        Optional<Integer> stacksTo,
        Optional<Boolean> useItemDescriptionPrefix,
        Optional<Tool> tool,
        Optional<Armor> armor
) {
    public static final ItemProperties EMPTY = new ItemProperties(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<ToolMaterial> TOOL_MATERIAL =
            NamedValues.codec("tool_material", NamedValues.constants(DWMToolMaterials.class, ToolMaterial.class));
    public static final Codec<ArmorMaterial> ARMOR_MATERIAL =
            NamedValues.codec("armor_material", NamedValues.constants(DWMArmorMaterials.class, ArmorMaterial.class));

    public static final Codec<ItemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 99).optionalFieldOf("stacks_to").forGetter(ItemProperties::stacksTo),
            Codec.BOOL.optionalFieldOf("use_item_description_prefix").forGetter(ItemProperties::useItemDescriptionPrefix),
            Tool.CODEC.optionalFieldOf("tool").forGetter(ItemProperties::tool),
            Armor.CODEC.optionalFieldOf("armor").forGetter(ItemProperties::armor)
    ).apply(instance, ItemProperties::new));

    /** Tier + vanilla attack numbers; {@link ItemType} decides which tool class uses them. */
    public record Tool(ToolMaterial material, float attackDamage, float attackSpeed) {
        public static final Codec<Tool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TOOL_MATERIAL.fieldOf("material").forGetter(Tool::material),
                Codec.FLOAT.fieldOf("attack_damage").forGetter(Tool::attackDamage),
                Codec.FLOAT.fieldOf("attack_speed").forGetter(Tool::attackSpeed)
        ).apply(instance, Tool::new));
    }

    public record Armor(ArmorMaterial material, ArmorType slot) {
        public static final Codec<Armor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ARMOR_MATERIAL.fieldOf("material").forGetter(Armor::material),
                NamedValues.enumCodec("armor_slot", ArmorType.class).fieldOf("slot").forGetter(Armor::slot)
        ).apply(instance, Armor::new));
    }

    public Item.Properties apply(Item.Properties target, ItemType type) {
        stacksTo.ifPresent(target::stacksTo);
        if (useItemDescriptionPrefix.orElse(false)) {
            target.useItemDescriptionPrefix();
        }
        switch (type) {
            case SWORD -> {
                Tool spec = tool.orElseThrow();
                target.sword(spec.material(), spec.attackDamage(), spec.attackSpeed());
            }
            case PICKAXE -> {
                Tool spec = tool.orElseThrow();
                target.pickaxe(spec.material(), spec.attackDamage(), spec.attackSpeed());
            }
            case ARMOR -> target.humanoidArmor(armor.orElseThrow().material(), armor.orElseThrow().slot());
            default -> { }
        }
        return target;
    }
}
