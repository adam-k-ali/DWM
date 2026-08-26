package com.adamkali.dwm.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Persistent sonic screwdriver state: which field modes are unlocked, which is selected,
 * and whether the TARDIS handshake (DWM-061) has paired this sonic.
 */
public record SonicState(
        Set<SonicFieldMode> unlocked,
        SonicFieldMode selected,
        boolean tardisPaired
) {
    public static final Codec<SonicState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SonicFieldMode.CODEC.listOf().fieldOf("unlocked").forGetter(state -> List.copyOf(state.unlocked())),
            SonicFieldMode.CODEC.fieldOf("selected").forGetter(SonicState::selected),
            Codec.BOOL.optionalFieldOf("tardisPaired", false).forGetter(SonicState::tardisPaired)
    ).apply(instance, SonicState::fromCodec));

    public static final StreamCodec<RegistryFriendlyByteBuf, SonicState> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC).cast();

    public SonicState {
        unlocked = unlocked.isEmpty()
                ? EnumSet.noneOf(SonicFieldMode.class)
                : EnumSet.copyOf(unlocked);
        if (selected == null) {
            selected = SonicFieldMode.OPEN;
        }
    }

    private static SonicState fromCodec(List<SonicFieldMode> unlocked, SonicFieldMode selected, boolean tardisPaired) {
        return new SonicState(EnumSet.copyOf(unlocked), selected, tardisPaired);
    }

    public static SonicState craftedOpenOnly() {
        return new SonicState(EnumSet.of(SonicFieldMode.OPEN), SonicFieldMode.OPEN, false);
    }

    public static SonicState fullyUnlocked() {
        return new SonicState(EnumSet.allOf(SonicFieldMode.class), SonicFieldMode.OPEN, false);
    }

    public boolean isUnlocked(SonicFieldMode mode) {
        return unlocked.contains(mode);
    }

    public int unlockedCount() {
        return unlocked.size();
    }

    public SonicState withUnlocked(SonicFieldMode mode) {
        EnumSet<SonicFieldMode> next = EnumSet.copyOf(unlocked);
        next.add(mode);
        return new SonicState(next, selected, tardisPaired);
    }

    public SonicState withSelected(SonicFieldMode mode) {
        return new SonicState(unlocked, mode, tardisPaired);
    }

    public SonicState withTardisPaired(boolean paired) {
        return new SonicState(unlocked, selected, paired);
    }
}
