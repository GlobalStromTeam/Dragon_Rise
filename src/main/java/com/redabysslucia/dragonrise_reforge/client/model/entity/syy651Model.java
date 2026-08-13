package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.J11Entity;
import com.redabysslucia.dragonrise_reforge.entities.syy651Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class syy651Model extends DragonriseVehicleModel<syy651Entity> {

        @Override
        public @Nullable TransformContext<syy651Entity> collectTransform(String boneName) {
                return switch (boneName) {
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
                                (bone, vehicle, state) -> bone.setRotX(vehicle.getGearRot() * Mth.DEG_TO_RAD);

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

                        case "glass" -> (bone, vehicle, state) ->
                                bone.setHidden(shouldHideGlass(vehicle));

                        default -> null;
                };
        }

        public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
                var gunData = vehicle.getGunData("Missile");
                if (gunData == null) {
                        return false;
                } else {
                        return gunData.ammo.get() < ammo;
                }
        }

        private boolean shouldHideGlass(syy651Entity vehicle) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return false;
            }

            Player localPlayer = mc.player;
            boolean isInDriverSeat = vehicle.hasPassenger(localPlayer)
                    && vehicle.getSeatIndex(localPlayer) == 0;

            if (!isInDriverSeat) {
                return false;
            }

            CameraType cameraType = mc.options.getCameraType();
            boolean isFirstPerson = cameraType == CameraType.FIRST_PERSON;

            boolean isControllingVehicle = vehicle.isControlledByLocalInstance();

            return isFirstPerson && isControllingVehicle;
        }
}