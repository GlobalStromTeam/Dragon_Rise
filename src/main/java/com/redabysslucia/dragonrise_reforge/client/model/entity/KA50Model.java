package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.Mi28Entity;
import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.A10Entity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.KA50Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;


public class KA50Model extends DragonriseVehicleModel<KA50Entity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<KA50Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller0" -> (bone, vehicle, state) ->
                    bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

            case "propeller1" -> (bone, vehicle, state) ->
                    bone.setRotY(-1 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

            case "tailPropeller" -> (bone, vehicle, state) ->
                    bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

            case "missile1" -> (bone, vehicle, state) ->
                    bone.setHidden(shouldHideMissile(vehicle, 2));

            case "missile2" -> (bone, vehicle, state) ->
                    bone.setHidden(shouldHideMissile(vehicle, 1));

            default -> null;
        };
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("SeekMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}
