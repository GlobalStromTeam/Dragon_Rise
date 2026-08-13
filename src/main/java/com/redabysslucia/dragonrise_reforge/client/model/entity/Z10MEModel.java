package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.Z10MEEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Z10MEModel extends DragonriseVehicleModel<Z10MEEntity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Z10MEEntity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("LJ11-1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("LJ11-2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("LJ11-3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("LJ11-4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("AKD10-1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 1));
        }

        if (boneName.equals("AKD10-2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 2));
        }

        if (boneName.equals("AKD10-3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 3));
        }

        if (boneName.equals("AKD10-4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 4));
        }

        if (boneName.equals("TY90-1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 1));
        }

        if (boneName.equals("TY90-2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 2));
        }

        if (boneName.equals("TY90-3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 3));
        }

        if (boneName.equals("TY90-4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 4));
        }

        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("SeekMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("PassengerMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissileAA(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("DriverAAMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}