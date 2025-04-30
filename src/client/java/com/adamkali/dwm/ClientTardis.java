package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.network.UpdateTardisChameleonC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClientTardis {
    private final TardisBlockEntity tardisBlockEntity;

    public ClientTardis(TardisBlockEntity tardisBlockEntity) {
        this.tardisBlockEntity = tardisBlockEntity;
    }

    public TardisBlockEntity getTardisBlockEntity() {
        return tardisBlockEntity;
    }

    public void updateChameleonVariant(@NotNull TardisChameleonVariant variant) {
        this.tardisBlockEntity.setVariant(variant);
        ClientPlayNetworking.send(new UpdateTardisChameleonC2SPayload(variant.getId(), new GlobalPos(Objects.requireNonNull(this.tardisBlockEntity.getWorld()).getRegistryKey(), this.tardisBlockEntity.getPos())));
        this.tardisBlockEntity.markDirty();
    }
}
