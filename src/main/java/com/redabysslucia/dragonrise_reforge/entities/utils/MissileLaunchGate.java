package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.LivingEntity;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.MissileLaunchStateMessage;
import net.minecraftforge.network.PacketDistributor;

/** Server-side reservation covers all missile weapons and seats on the same platform. */
public final class MissileLaunchGate {
    private static final LaunchReservations<VehicleEntity> PENDING = new LaunchReservations<>();

    private MissileLaunchGate() {}

    public static boolean tryBegin(VehicleEntity vehicle, GunData gun) {
        if (vehicle.level().isClientSide() || !GroundMissileGuidance.delayedMissile(vehicle, gun)) return true;
        long token = PENDING.reserve(vehicle);
        if (token == 0) {
            sync(vehicle, PENDING.currentToken(vehicle), true);
            return false;
        }
        sync(vehicle, token, true);
        // Successful spawns release immediately. Release failed/cancelled shots one tick later as a fallback.
        Mod.queueServerWork(gun.get(GunProp.SHOOT_DELAY_TIME) + 1, () -> {
            if (PENDING.currentToken(vehicle) == token) {
                PENDING.expire(vehicle, token);
                sync(vehicle, token, false);
            }
        });
        return true;
    }

    public static void launched(VehicleEntity vehicle) {
        long token = PENDING.currentToken(vehicle);
        PENDING.complete(vehicle);
        if (token != 0) sync(vehicle, token, false);
    }

    private static void sync(VehicleEntity vehicle, long token, boolean pending) {
        ModNetwork.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> vehicle),
                new MissileLaunchStateMessage(vehicle.getUUID(), token, pending));
    }

    public static boolean tryNamed(VehicleEntity vehicle, String weapon) {
        return tryBegin(vehicle, vehicle.getGunData(weapon));
    }

    public static boolean trySelected(VehicleEntity vehicle, LivingEntity shooter) {
        return tryBegin(vehicle, vehicle.getGunData(vehicle.getSeatIndex(shooter)));
    }

    public static boolean claimEffects(VehicleEntity vehicle) { return PENDING.claimEffects(vehicle); }
}
