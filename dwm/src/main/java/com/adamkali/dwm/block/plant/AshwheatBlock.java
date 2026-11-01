package com.adamkali.dwm.block.plant;

import com.adamkali.dwm.item.DWMItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Thal field crop. Farmland-only; generated solely by Thal settlement farm pieces, never as wild decoration. */
public class AshwheatBlock extends CropBlock {
    public static final MapCodec<AshwheatBlock> CODEC = simpleCodec(AshwheatBlock::new);

    public AshwheatBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AshwheatBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return DWMItems.ASHWHEAT_SEEDS;
    }
}
