package com.redabysslucia.dragonrise_reforge.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleLeashData;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class DisconnectLeashMessage {

    public static void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            var vehicle = player.getVehicle();
            if (!(vehicle instanceof VehicleEntity self)) return;

            UUID selfId = self.getUUID();

            // 断开自己栓住的实体
            self.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
            self.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);

            // 断开所有栓住自己的实体
            self.level().getEntitiesOfClass(VehicleEntity.class,
                    self.getBoundingBox().inflate(50)).forEach(other -> {
                    Optional<UUID> holder = other.getEntityData().get(VehicleLeashData.LEASH_HOLDER);
                    if (holder.isPresent() && holder.get().equals(selfId)) {
                        other.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
                        other.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);
                    }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
