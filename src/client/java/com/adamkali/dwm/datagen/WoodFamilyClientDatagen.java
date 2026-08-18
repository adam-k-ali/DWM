package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyBlocks;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;

public final class WoodFamilyClientDatagen {
    private static final Set<Identifier> EMITTED_DOOR_ITEM_TEMPLATES = new HashSet<>();

    private WoodFamilyClientDatagen() {
    }

    public static void generateBlockModels(BlockModelGenerators generator, RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        generator.woodProvider(blocks.log()).log(blocks.log()).wood(blocks.wood());
        generator.woodProvider(blocks.strippedLog()).log(blocks.strippedLog()).wood(blocks.strippedWood());
        generator.createTrivialBlock(blocks.leaves(), TexturedModel.LEAVES);
        generator.registerSimpleItemModel(blocks.leaves(), ModelLocationUtils.getModelLocation(blocks.leaves()));
        generator.createPlantWithDefaultItem(
                blocks.sapling(),
                blocks.pottedSapling(),
                BlockModelGenerators.PlantType.NOT_TINTED
        );
        generator.family(blocks.planks()).generateFor(family.vanillaModelFamily());
        // Hanging signs are entity-rendered; blockstates stay particle-only (stripped log texture).
        generator.createParticleOnlyBlock(blocks.hangingSign(), blocks.strippedLog());
        generator.createParticleOnlyBlock(blocks.wallHangingSign(), blocks.strippedLog());
        generator.registerSimpleFlatItemModel(blocks.hangingSign().asItem());
        if (family.has(WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL) && blocks.trapdoor() != null) {
            Identifier bottomModel = Identifier.fromNamespaceAndPath(
                    DWMReference.MOD_ID,
                    "block/" + family.definition().id() + "_trapdoor_bottom"
            );
            generator.registerSimpleItemModel(blocks.trapdoor(), bottomModel);
        }
        if (family.has(WoodFamilyFeature.CUSTOM_DOOR_MODEL) && blocks.door() != null) {
            emitCustomDoorItemModel(generator, family);
        }
    }

    public static void generateItemModels(ItemModelGenerators generator, RegisteredWoodFamily family) {
        generator.generateFlatItem(family.boatItem(), ModelTemplates.FLAT_ITEM);
    }

    private static void emitCustomDoorItemModel(BlockModelGenerators generator, RegisteredWoodFamily family) {
        try {
            DoorItemModelAssembler.AssembledDoorItem assembled =
                    DoorItemModelAssembler.assemble(
                            DoorItemModelAssembler.resolveBlockModelsDir(),
                            family.definition().id()
                    );
            Identifier templateId = Identifier.fromNamespaceAndPath(
                    DWMReference.MOD_ID,
                    assembled.templateModelPath()
            );
            Identifier itemId = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, assembled.itemModelPath());
            if (EMITTED_DOOR_ITEM_TEMPLATES.add(templateId)) {
                generator.modelOutput.accept(templateId, () -> JsonParser.parseString(assembled.template().toString()));
            }
            generator.modelOutput.accept(itemId, () -> JsonParser.parseString(assembled.wrapper().toString()));
            generator.registerSimpleItemModel(family.blocks().door(), itemId);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to assemble custom door item model for " + family.definition().id(),
                    e
            );
        }
    }
}
