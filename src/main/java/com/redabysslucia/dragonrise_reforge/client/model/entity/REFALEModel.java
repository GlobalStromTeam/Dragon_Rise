package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import com.redabysslucia.dragonrise_reforge.entities.REFALEEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class REFALEModel extends VehicleModel<REFALEEntity> {

        @Override
        public @Nullable TransformContext<REFALEEntity> collectTransform(String boneName) {
                return switch (boneName) {
                        case "root" -> (bone, vehicle, state) ->
                                bone.setHidden(hideForTurretControllerWhileZooming && vehicle.getWeaponIndex(0) == 2);

                        case "wingLR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1LRotO, vehicle.getFlap1LRot()) * Mth.DEG_TO_RAD);

                        case "wingRR" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1RRotO, vehicle.getFlap1RRot()) * Mth.DEG_TO_RAD);

                        case "wingLR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1L2RotO, vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

                        case "wingRR2" -> (bone, vehicle, state) ->
                                bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1R2RotO, vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

                        case "wingLB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.flap2LRotO, vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

                        case "wingRB" -> (bone, vehicle, state) ->
                                bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.flap2RRotO, vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

                        case "weiyiL", "weiyiR" -> (bone, vehicle, state) ->
                                bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.flap3RotO, vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

                        case "gear", "gear2", "gear3" ->
                                (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

                        case "qianzhou", "qianzhou2" ->
                                (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));

                        case "missle7" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideBomb(vehicle, 1));

                        case "missile1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 1));

                        case "missile2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 2));

                        case "1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 1));

                        case "2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 2));

                        case "3" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 3));

                        case "4" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 4));

                        case "5" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 5));

                        case "6" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissileAT(vehicle, 6));
//

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

        public boolean shouldHideMissileAT(VehicleEntity vehicle, int ammo) {
               var gunData = vehicle.getGunData("SeekMissile");
               if (gunData == null) {
                       return false;
               } else {
                       return gunData.ammo.get() < ammo;
               }
        }

}