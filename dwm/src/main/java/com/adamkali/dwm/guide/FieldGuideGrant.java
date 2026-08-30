package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.item.DWMItems;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Gives one Field Guide book the first time a player joins a world.
 */
public final class FieldGuideGrant {
    public static final AttachmentType<Boolean> RECEIVED = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "received_field_guide"),
            builder -> builder.persistent(Codec.BOOL).copyOnDeath()
    );

    private FieldGuideGrant() {
    }

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> tryGive(handler.player));
    }

    public static boolean tryGive(ServerPlayer player) {
        boolean alreadyReceived = Boolean.TRUE.equals(player.getAttached(RECEIVED));
        if (!FieldGuideGrantLogic.shouldGrant(alreadyReceived)) {
            return false;
        }
        ItemStack stack = new ItemStack(DWMItems.FIELD_GUIDE);
        Inventory inventory = player.getInventory();
        int slot = FieldGuideGrantLogic.slotForGrant(
                inventory.getItem(FieldGuideGrantLogic.PREFERRED_HOTBAR_SLOT).isEmpty());
        if (slot >= 0) {
            inventory.setItem(slot, stack);
        } else if (!inventory.add(stack)) {
            player.drop(stack, false);
        }
        player.setAttached(RECEIVED, true);
        return true;
    }
}
