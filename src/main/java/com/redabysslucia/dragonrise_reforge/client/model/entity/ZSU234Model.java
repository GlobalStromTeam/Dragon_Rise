package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.AKMEEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZSU234EEntity;
import org.jetbrains.annotations.Nullable;

public class ZSU234Model extends DragonriseVehicleModel<ZSU234EEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

    @Override
    public @Nullable TransformContext<ZSU234EEntity> collectTransform(String boneName) {

        if (boneName.equals("missile1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("missile2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("missile3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("missile4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("missileshell")) {
            return(bone, vehicle, state) -> bone.setRotX(getAnimationProcessor().getBone("barrel").getRotX());
        }
        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("DriverAAMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}