package com.adamkali.dwm;

import com.adamkali.dwm.item.DWMItems;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DWMItemsTests {
    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        DWMItems.initialize();
    }

    @Test
    public void testSonicScrewdriversRegistered() {
        assertEquals(Registries.ITEM.get(Identifier.of("dwm", "sonic_second_doctor")), DWMItems.SONIC_SECOND_DOCTOR);
        assertEquals(Registries.ITEM.get(Identifier.of("dwm", "sonic_third_doctor")), DWMItems.SONIC_THIRD_DOCTOR);
        assertEquals(Registries.ITEM.get(Identifier.of("dwm", "sonic_fourth_doctor")), DWMItems.SONIC_FOURTH_DOCTOR);
        assertEquals(Registries.ITEM.get(Identifier.of("dwm", "sonic_fifth_doctor")), DWMItems.SONIC_FIFTH_DOCTOR);
    }
}
