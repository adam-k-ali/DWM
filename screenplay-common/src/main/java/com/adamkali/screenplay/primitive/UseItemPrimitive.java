package com.adamkali.screenplay.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Map;

public final class UseItemPrimitive extends NoArgPrimitive {
    @Override
    public String name() {
        return "useItem";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Minecraft client = context.client();
        if (client.player == null || client.gameMode == null || context.screen() != null) {
            return false;
        }
        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        context.logger().info("Using item on {} face {}", blockHit.getBlockPos(), blockHit.getDirection());
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, blockHit);
        return true;
    }
}
