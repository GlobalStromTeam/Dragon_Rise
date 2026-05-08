package com.redabysslucia.dragonrise_reforge.client.model.entity.special;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.DragonriseVehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.*;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.YAW_WHILE_SHOOT;

public class CyborgTankModel extends DragonriseVehicleModel<CyborgTankEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<CyborgTankEntity> collectTransform(String boneName) {
        //
        if (boneName.equals("root") && hideForTurretControllerWhileZooming()) {
            return (bone, vehicle, state) -> bone.setHidden(getHideForTurretControllerWhileZooming());
        }

        //
        if (boneName.equals("passengerWeaponStation") && hideForTurretControllerWhileZooming()) {
            return (bone, vehicle, state) -> bone.setHidden(getHideForPassengerWeaponStationControllerWhileZooming());
        }

        if (boneName.equals("laser")) {
            return (bone, vehicle, state) -> {
                bone.setScaleZ(10 * vehicle.getEntityData().get(LASER_LENGTH));
                float scale = Math.min(Mth.lerp(state.getPartialTick(), vehicle.getEntityData().get(LASER_SCALE_O), vehicle.getEntityData().get(LASER_SCALE)), 1.2f);

                bone.setScaleX(scale);
                bone.setScaleY(scale);
            };
        }

        //
        switch (boneName) {
            case "base" -> {
                return (bone, vehicle, state) -> {
                    float a = vehicle.getEntityData().get(YAW_WHILE_SHOOT);
                    float r = (Mth.abs(a) - 90f) / 90f;

                    float r2;

                    if (Mth.abs(a) <= 90f) {
                        r2 = a / 90f;
                    } else {
                        if (a < 0) {
                            r2 = -(180f + a) / 90f;
                        } else {
                            r2 = (180f - a) / 90f;
                        }
                    }

                    bone.setPosX(r2 * getRecoilShake() * 0.5f);
                    bone.setPosZ(r * getRecoilShake() * 1f);
                    bone.setRotX(r * getRecoilShake() * Mth.DEG_TO_RAD);
                    bone.setRotZ(r2 * getRecoilShake() * Mth.DEG_TO_RAD);
                };
            }

            //
            case "turret" -> {
                return (bone, vehicle, state) -> {
                    bone.setRotY(getTurretYRot() * Mth.DEG_TO_RAD);


                };
            }

            //
            case "barrel" -> {
                return (bone, vehicle, state) -> {
                    float a = getTurretYaw();
                    float r = (Mth.abs(a) - 90f) / 90f;

                    float r2;

                    if (Mth.abs(a) <= 90f) {
                        r2 = a / 90f;
                    } else {
                        if (a < 0) {
                            r2 = -(180f + a) / 90f;
                        } else {
                            r2 = (180f - a) / 90f;
                        }
                    }

                    bone.setRotX(Mth.clamp(-getTurretXRot() - r * getPitch() - r2 * getRoll(), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);

                    var barrelLaser = getAnimationProcessor().getBone("barrelLaser");
                    if (barrelLaser != null) {
                        barrelLaser.setRotX(bone.getRotX());
                    }
                };
            }

            //
            case "passengerWeaponStationYaw" -> {
                return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getGunYRotO(), vehicle.getGunYRot()) * Mth.DEG_TO_RAD);
            }

            //
            case "passengerWeaponStationPitch" -> {
                return (bone, vehicle, state) -> {
                    float a = vehicle.getGunYRot(state.getPartialTick());
                    float r = (Mth.abs(a) - 90f) / 90f;

                    float r2;

                    if (Mth.abs(a) <= 90f) {
                        r2 = a / 90f;
                    } else {
                        if (a < 0) {
                            r2 = -(180f + a) / 90f;
                        } else {
                            r2 = (180f - a) / 90f;
                        }
                    }

                    bone.setRotX(Mth.clamp(
                            -Mth.lerp(state.getPartialTick(), vehicle.getGunXRotO(), vehicle.getGunXRot()) * Mth.DEG_TO_RAD
                                    - r * getPitch() * Mth.DEG_TO_RAD
                                    - r2 * getRoll() * Mth.DEG_TO_RAD,
                            -10 * Mth.DEG_TO_RAD, 60 * Mth.DEG_TO_RAD)
                    );
                };
            }
        }

        var wheelMatcher = VehicleModel.Companion.getWHEEL_PATTERN().matcher(boneName);
        if (wheelMatcher.matches()) {
            var isL = wheelMatcher.group("direction").equals("L");

            if (boneName.endsWith("Turn")) {
                return (bone, vehicle, state) -> {
                    bone.setRotX(1.5f * (isL ? getLeftWheelRot() : getRightWheelRot()));
                    bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getRudderRotO(), vehicle.getRudderRot()));
                };
            } else {
                return (bone, vehicle, state) -> bone.setRotX(1.5f * (isL ? getLeftWheelRot() : getRightWheelRot()));
            }
        }

        return null;
    }
}
