package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.TardisBlockEntity;
import org.jetbrains.annotations.NotNull;

public class ClientTardis {
    private final TardisBlockEntity tardisBlockEntity;

    public ClientTardis(TardisBlockEntity tardisBlockEntity) {
        this.tardisBlockEntity = tardisBlockEntity;
    }

    public void updateChameleonVariant(@NotNull TardisChameleonVariant variant) {
        System.out.println("ClientTardis: updateChameleonVariant: " + variant.getId());
        System.out.println("ClientTardis: Tardis Pos: " + this.tardisBlockEntity.getPos());
//        ClientPlayNetworking.send(new UpdateTardisChameleonC2SPayload(variant.getId(), new GlobalPos(Objects.requireNonNull(this.tardisBlockEntity.getWorld()).getRegistryKey(), this.tardisBlockEntity.getPos())));
    }
}
