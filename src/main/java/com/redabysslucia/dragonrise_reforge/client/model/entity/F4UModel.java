package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.CamelEntity;
import com.redabysslucia.dragonrise_reforge.entities.F4UEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class F4UModel extends DragonriseVehicleModel<F4UEntity> {

        @Override
        public @Nullable TransformContext<F4UEntity> collectTransform(String boneName) {
                return switch (boneName) {

                    case "propeller" -> (bone, vehicle, state) ->
                            bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
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
                    case "weiyiL", "weiyiR" -> (bone, vehicle, state) ->
                            bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);
                    case "gear", "gear2", "gear3" ->
                            (bone, vehicle, state) -> bone.setRotX(vehicle.getGearRot() * Mth.DEG_TO_RAD);
                    case "qianzhou", "qianzhou2" ->
                            (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
                    case "bomb1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 1));
                    case "bomb2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 2));

                    case "bomb3" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideMissile(vehicle, 3));

                    case "rocket1" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 1));

                    case "rocket2" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 2));

                    case "rocket3" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 3));

                    case "rocket4" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 4));

                    case "rocket5" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 5));

                    case "rocket6" -> (bone, vehicle, state) ->
                            bone.setHidden(shouldHideCow(vehicle, 6));
                        default -> null;
                };
        }
        public boolean shouldHideCow(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Rocket");
                if (gunData == null) {
                        return false;
                } else {
                        return gunData.ammo.get() < ammo;
                }
        }

        public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Bomb");
                if (gunData == null) {
                        return false;
                } else {
                        return gunData.ammo.get() < ammo;
                }
        }

}