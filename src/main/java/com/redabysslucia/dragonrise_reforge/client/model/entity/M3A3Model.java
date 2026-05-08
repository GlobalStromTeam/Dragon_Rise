package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.CV90Entity;
import com.redabysslucia.dragonrise_reforge.entities.M3A3Entity;
import org.jetbrains.annotations.Nullable;

public class M3A3Model extends DragonriseVehicleModel<M3A3Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

//    @Override
//    public @Nullable TransformContext<CV90Entity> collectTransform(String boneName) {
//
//        if (boneName.equals("missile2")) {
//            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
//
//
//        if (boneName.equals("missile1")) {
//            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
//        }
//
//        return super.collectTransform(boneName);
//    }
//
//    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
//        var gunData = vehicle.getGunData("Missile");
//        if (gunData == null) {
//            return false;
//        } else {
//            return gunData.ammo.get() < ammo;
//        }
//    }
}