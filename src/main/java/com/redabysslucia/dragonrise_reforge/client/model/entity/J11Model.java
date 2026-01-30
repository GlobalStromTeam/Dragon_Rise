package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J11Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class J11Model extends VehicleModel<J11Entity> {

        @Override
        public @Nullable TransformContext<J11Entity> collectTransform(String boneName) {
                return switch (boneName) {
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

                        case "weiyiR" -> (bone, vehicle, state) ->
                                bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.flap3RotO, vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

                        case "gear", "gear2", "gear3" ->
                                (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

                        case "qianzhou", "qianzhou2" ->
                                (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));

                        case "1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 1));

                        case "2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 2));

                        case "3" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 3));

                        case "4" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile(vehicle, 4));

                        case "pl-12-1" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 1));

                        case "pl-12-2" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 2));

                        case "pl-12-3" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 3));

                        case "pl-12-4" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 4));

                        case "pl-12-5" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 5));

                        case "pl-12-6" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideMissile2(vehicle, 6));

                           default -> null;
                };
        }
        public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Missile2");
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

}