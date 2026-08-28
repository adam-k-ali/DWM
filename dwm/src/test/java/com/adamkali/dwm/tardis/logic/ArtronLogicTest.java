package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtronLogicTest {
    private TardisDataModel model;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @BeforeEach
    void setUp() {
        model = new TardisDataModel();
    }

    @Test
    void read_nullAndMissingFieldAreFull() {
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(null));
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(model));
        model.artron = null;
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(model));
    }

    @Test
    void read_clampsToCapacity() {
        model.artron = 999;
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(model));
        model.artron = -4;
        assertEquals(0, ArtronLogic.read(model));
    }

    @Test
    void constructor_startsFull() {
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(new TardisDataModel()));
    }

    @Test
    void foundStart_isThirty() {
        ArtronLogic.applyFoundStart(model);
        assertEquals(ArtronLogic.FOUND_START, ArtronLogic.read(model));
        assertEquals(30, model.artron);
    }

    @Test
    void needle_isFractionOfCapacity() {
        model.artron = 0;
        assertEquals(0.0F, ArtronLogic.needle(model), 1e-4);
        model.artron = ArtronLogic.CAPACITY;
        assertEquals(1.0F, ArtronLogic.needle(model), 1e-4);
        model.artron = 250;
        assertEquals(0.5F, ArtronLogic.needle(model), 1e-4);
        assertEquals(50, ArtronLogic.percent(250));
    }

    @Test
    void cost_sameWorldVsDimensionChange() {
        assertEquals(10, ArtronLogic.cost("minecraft:overworld", "minecraft:overworld"));
        assertEquals(30, ArtronLogic.cost("minecraft:overworld", "minecraft:the_nether"));
        assertEquals(10, ArtronLogic.cost(null, "minecraft:overworld"));
        assertEquals(10, ArtronLogic.cost("minecraft:overworld", null));
        assertEquals(10, ArtronLogic.cost("", "minecraft:overworld"));
    }

    @Test
    void trySpend_deductsWhenAffordable_refusesWhenNot() {
        model.artron = 30;
        assertTrue(ArtronLogic.trySpend(model, 10, false));
        assertEquals(20, ArtronLogic.read(model));
        assertFalse(ArtronLogic.trySpend(model, 30, false));
        assertEquals(20, ArtronLogic.read(model));
        model.artron = 0;
        assertFalse(ArtronLogic.trySpend(model, 10, false));
        assertEquals(0, ArtronLogic.read(model));
    }

    @Test
    void trySpend_creativeSkipsDeduct() {
        model.artron = 0;
        assertTrue(ArtronLogic.trySpend(model, 30, true));
        assertEquals(0, ArtronLogic.read(model));
    }

    @Test
    void tryFill_crystalsAddTwentyFiveAndClamp() {
        model.artron = 50;
        assertEquals(ArtronLogic.FillResult.FILLED, ArtronLogic.tryFill(model, ArtronLogic.HeldFuel.CRYSTALS));
        assertEquals(75, ArtronLogic.read(model));

        model.artron = 490;
        assertEquals(ArtronLogic.FillResult.FILLED, ArtronLogic.tryFill(model, ArtronLogic.HeldFuel.CRYSTALS));
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(model));
    }

    @Test
    void tryFill_alreadyFullDoesNotAdd() {
        model.artron = ArtronLogic.CAPACITY;
        assertEquals(ArtronLogic.FillResult.ALREADY_FULL, ArtronLogic.tryFill(model, ArtronLogic.HeldFuel.CRYSTALS));
        assertEquals(ArtronLogic.CAPACITY, ArtronLogic.read(model));
    }

    @Test
    void tryFill_powderIsHintAndOtherItemsRead() {
        model.artron = 40;
        assertEquals(ArtronLogic.FillResult.POWDER_HINT, ArtronLogic.tryFill(model, ArtronLogic.HeldFuel.POWDER));
        assertEquals(40, ArtronLogic.read(model));
        assertEquals(ArtronLogic.FillResult.READ, ArtronLogic.tryFill(model, ArtronLogic.HeldFuel.NONE));
        assertEquals(40, ArtronLogic.read(model));
    }

    @Test
    void spendRefuseKey_emptyVsInsufficient() {
        assertEquals(ArtronLogic.ARTRON_EMPTY_KEY, ArtronLogic.spendRefuseKey(0));
        assertEquals(ArtronLogic.NOT_ENOUGH_KEY, ArtronLogic.spendRefuseKey(5));
    }
}
