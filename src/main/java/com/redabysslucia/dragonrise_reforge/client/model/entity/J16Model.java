package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.F14Entity;
import com.redabysslucia.dragonrise_reforge.entities.J16Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class J16Model extends DragonriseVehicleModel<J16Entity> {

        @Override
        public @Nullable TransformContext<J16Entity> collectTransform(String boneName) {
                return switch (boneName) {
                        case "root" -> (bone, vehicle, state) ->
                                bone.setHidden(getHideForTurretControllerWhileZooming() && vehicle.getWeaponIndex(0) == 2);

                        case "wingLR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1L2RotO(), vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

                        case "wingRR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1R2RotO(), vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

                        case "wingLR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1LRotO(), vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

                        case "wingRR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1LRotO(), vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

                        case "wingLB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

                        case "wingRB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

                        case "weiyiL", "weiyiR" -> (bone, vehicle, state) ->
                                bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

                        case "gear", "gear2", "gear3" ->
                                (bone, vehicle, state) -> bone.setRotX(vehicle.getGearRot() * Mth.DEG_TO_RAD);

                        case "qianzhou", "qianzhou2" ->
                                (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

                        case "missile1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 1));

                        case "missile2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 2));

                    case "yj1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 1));

                    case "yj2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 2));

                    case "yj3" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 3));

                    case "yj4" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 4));

                    case "yj5" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 5));

                    case "yj6" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 6));
                        default -> null;

                    case "bomb1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideBomb(vehicle, 1));

                    case "bomb2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideBomb(vehicle, 2));
                };
        }
        public boolean shouldHideCow(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("SeekMissile");
                if (gunData == null) {
                        return false;
                } else {
                        return gunData.ammo.get() < ammo;
                }
        }

        public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Missile");
                if (gunData == null) {
                        return false;
                } else {
                        return gunData.ammo.get() < ammo;
                }
        }

        public boolean shouldHideBomb(VehicleEntity vehicle, int ammo) {
            var gunData = vehicle.getGunData("Bomb");
            if (gunData == null) {
                    return false;
            } else {
                    return gunData.ammo.get() < ammo;
        }
    }

}