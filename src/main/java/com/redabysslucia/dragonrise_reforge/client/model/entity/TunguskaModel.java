package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.TunguskaEntity;
import org.jetbrains.annotations.Nullable;

public class TunguskaModel extends DragonriseVehicleModel<TunguskaEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable TransformContext<TunguskaEntity> collectTransform(String boneName) {

        if (boneName.equals("MissilePos2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissilepos2(vehicle));
        }

        if (boneName.equals("MissilePos1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissilepos1(vehicle));
        }

        if (boneName.equals("missileshell")) {
            return (bone, vehicle, state) -> bone.setRotX(getAnimationProcessor().getBone("barrel").getRotX());
        }
        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissilepos2(VehicleEntity vehicle) {
        var gunData = vehicle.getGunData("DriverAAMissile");
        if (gunData == null) {
            return false;
        }
        int count = 0;
        count = 2;
        return gunData.ammo.get() < count;
    }

    public boolean shouldHideMissilepos1(VehicleEntity vehicle) {
        var gunData = vehicle.getGunData("DriverAAMissile");
        if (gunData == null) {
            return false;
        }
        int count = 0;
        count = 1;
        return gunData.ammo.get() < count;
    }

}
