package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.EC665Entity;
import com.redabysslucia.dragonrise_reforge.entities.Z10MEEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class EC665Model extends DragonriseVehicleModel<EC665Entity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<EC665Entity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("agL1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("agL2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("agL3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("agL4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("agR1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 5));
        }

        if (boneName.equals("agR2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 6));
        }

        if (boneName.equals("agR3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
        }

        if (boneName.equals("agR4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
        }



        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("PassengerMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

}