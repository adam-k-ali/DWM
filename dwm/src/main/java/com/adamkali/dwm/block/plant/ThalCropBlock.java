package com.adamkali.dwm.block.plant;

import com.adamkali.dwm.item.DWMItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Thal field crop. Farmland-only; generated solely by Thal settlement farm pieces, never as wild decoration. */
public class ThalCropBlock extends CropBlock {
    public static final MapCodec<ThalCropBlock> CODEC = simpleCodec(ThalCropBlock::new);

    public ThalCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ThalCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return DWMItems.THAL_SEEDS;
    }
}
