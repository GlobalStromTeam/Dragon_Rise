package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.J15TEntity;
import com.redabysslucia.dragonrise_reforge.entities.J16Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class J15TModel extends DragonriseVehicleModel<J15TEntity> {

        @Override
        public @Nullable TransformContext<J15TEntity> collectTransform(String boneName) {
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

                        case "FlapL" -> (bone, vehicle, state) ->
                                bone.setRotX(-1 * Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

                        case "FlapR" -> (bone, vehicle, state) ->
                                bone.setRotX(-1 * Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

                        case "weiyiL", "weiyiR" -> (bone, vehicle, state) ->
                                bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

                        case "gear", "gear2", "gear3" ->
                                (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

                        case "qianzhou", "qianzhou2" ->
                                (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

                    case "1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 1));

                    case "2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 2));

                    case "3" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 3));

                    case "4" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 4));

                    case "pl-12-2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile2(vehicle, 1));

                    case "pl-12-1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile2(vehicle, 2));

                    case "yj5" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 1));

                    case "yj4" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 2));

                    case "yj3" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 3));

                    case "yj2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 4));

                    case "yj1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 5));

                        default -> null;

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

        public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
            var gunData = vehicle.getGunData("Missile2");
            if (gunData == null) {
                return false;
            } else {
                return gunData.ammo.get() < ammo;
            }
        }

}