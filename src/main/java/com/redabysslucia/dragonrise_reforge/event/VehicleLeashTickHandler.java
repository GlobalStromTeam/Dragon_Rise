package com.redabysslucia.dragonrise_reforge.event;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleLeashData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

/**
 * 全局 tick：每 tick 对所有载具执行栓绳/铁链拉力。
 * 每秒一次检查断开（仅栓绳）。
 */
@Mod.EventBusSubscriber
public class VehicleLeashTickHandler {

    private static int tickCounter;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        tickCounter++;
        boolean doDisconnectCheck = tickCounter >= 20;

        var server = event.getServer();
        for (var level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (!(entity instanceof VehicleEntity vehicle)) continue;

                Optional<UUID> holderUUID = vehicle.getEntityData().get(VehicleLeashData.LEASH_HOLDER);
                if (holderUUID.isEmpty()) continue;

                Entity holder = level.getEntity(holderUUID.get());
                if (holder == null || !holder.isAlive() || holder.isRemoved()) {
                    vehicle.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
                    vehicle.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);
                    continue;
                }

                // 每 tick 拉力
                VehicleLeashData.tickLeash(vehicle);

                // 每秒一次：栓绳超 30 格断开，铁链永不断开
                if (doDisconnectCheck && !vehicle.getEntityData().get(VehicleLeashData.CHAIN_LEASHED)
                        && vehicle.distanceToSqr(holder) > 900) {
                    vehicle.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
                    vehicle.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);
                }
            }
        }

        if (doDisconnectCheck) tickCounter = 0;
    }
}
