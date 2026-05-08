package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.F16CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class F16CModel extends DragonriseVehicleModel<F16CEntity> {

        @Override
        public @Nullable TransformContext<F16CEntity> collectTransform(String boneName) {
                return switch (boneName) {
                        case "root" -> (bone, vehicle, state) ->
                                bone.setHidden(getHideForTurretControllerWhileZooming() && vehicle.getWeaponIndex(0) == 2);

                        case "wingLR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1LRotO(), vehicle.getFlap1LRot()) * Mth.DEG_TO_RAD);

                        case "wingRR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1RRotO(), vehicle.getFlap1RRot()) * Mth.DEG_TO_RAD);

                        case "wingLR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1L2RotO(), vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

                        case "wingRR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1R2RotO(), vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

                        case "wingLB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

                        case "wingRB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

                        case "weiyiR" -> (bone, vehicle, state) ->
                                bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

                        case "gear", "gear2", "gear3" ->
                                (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

                        case "qianzhou", "qianzhou2" ->
                                (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

                        case "bomb1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideBomb(vehicle, 1));

                        case "bomb2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideBomb(vehicle, 2));

                        case "aim120-1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 1));

                        case "aim120-2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 2));

                        case "agm65-1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 1));

                        case "agm65-2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 2));

                        case "agm65-3" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 3));

                        case "agm65-4" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 4));

                        case "agm65-5" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 5));

                        case "agm65-6" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideSeekMissile(vehicle, 6));

                        case "missile1" -> (bone, vehicle, state) ->
                                    bone.setHidden(shouldHideMissile(vehicle, 1));

                        case "missile2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 2));

//                        case "missile3" -> (bone, vehicle, state) ->
//                                bone.setHidden(shouldHideMissile(vehicle, 2));
//
//                        case "missile4" -> (bone, vehicle, state) ->
//                                bone.setHidden(shouldHideMissile(vehicle, 1));

                        default -> null;
                };
        }
        public boolean shouldHideBomb(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Bomb");
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

        public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Missile2");
                if (gunData == null) {
                       return false;
                } else {
                       return gunData.ammo.get() < ammo;
                }
        }

        public boolean shouldHideSeekMissile(VehicleEntity vehicle, int ammo) {
            var gunData = vehicle.getGunData("SeekMissile");
            if (gunData == null) {
                return false;
            } else {
                return gunData.ammo.get() < ammo;
            }
        }
}