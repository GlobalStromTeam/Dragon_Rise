package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

/** Keeps emplacement tube state, ejection and backblast aligned with SBW's queued projectile. */
public final class MissileLaunchEffects {
    private MissileLaunchEffects() {
    }

    public static void afterLaunch(VehicleEntity vehicle, GunData gun, Runnable effects) {
        if (vehicle.level().isClientSide() || gun == null) return;
        if (!MissileLaunchGate.claimEffects(vehicle)) return;
        int delay = gun.get(GunProp.SHOOT_DELAY_TIME);
        Runnable launch = () -> {
            if (!vehicle.isRemoved() && !vehicle.isWreck()) effects.run();
        };
        if (delay > 0) Mod.queueServerWork(delay, launch);
        else launch.run();
    }
}
