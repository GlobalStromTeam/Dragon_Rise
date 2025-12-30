package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.*;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.YAW_WHILE_SHOOT;

public class ZTZ99AModel extends VehicleModel<ZTZ99AEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    public @Nullable TransformContext<ZTZ99AEntity> collectTransform(String boneName) {
        // 瞄准时隐藏车体
        if (boneName.equals("root") && hideForTurretControllerWhileZooming()) {
            return (bone, vehicle, state) -> bone.setHidden(hideForTurretControllerWhileZooming);
        }

        // 瞄准时隐藏乘客武器站
        if (boneName.equals("passengerWeaponStation") && hideForTurretControllerWhileZooming()) {
            return (bone, vehicle, state) -> bone.setHidden(hideForPassengerWeaponStationControllerWhileZooming);
        }

        //射击时带来的车体摇晃视觉效果
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

                    bone.setPosX(r2 * recoilShake * 0.5f);
                    bone.setPosZ(r * recoilShake * 1f);
                    bone.setRotX((r * recoilShake - 2.5f ) * Mth.DEG_TO_RAD);
                    bone.setRotZ(r2 * recoilShake * Mth.DEG_TO_RAD);
                };
            }

            // turret
            case "turret" -> {
                return (bone, vehicle, state) -> {
                    bone.setRotY(turretYRot * Mth.DEG_TO_RAD);

                    //bone.setRotX((float) (-2 * Math.cos(turretYRot * Mth.DEG_TO_RAD) + 2)* Mth.DEG_TO_RAD);

                    //bone.setRotZ((float) ( 12 * Math.sin(turretYRot * Mth.DEG_TO_RAD) )* Mth.DEG_TO_RAD);
                };
            }

            // barrel
            case "barrel" -> {
                return (bone, vehicle, state) -> {
                    float a = turretYaw;
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

                    //两倍于车体倾斜
                    bone.setRotX(Mth.clamp(-turretXRot - r * pitch - r2 * roll+5 -(5 * Math.abs(turretYRot/180)), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
                    //bone.setRotX((float) (Mth.clamp(-turretXRot - r * pitch - r2 * roll -(5 * Math.abs(turretYRot/180)), -180,180) * Mth.DEG_TO_RAD));
                };
            }

            // turret上的成员武器站Yaw
            case "passengerWeaponStationYaw" -> {
                return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.gunYRotO, vehicle.getGunYRot()) * Mth.DEG_TO_RAD - turretYRot * Mth.DEG_TO_RAD);
            }

            // turret上的成员武器站Pitch
            case "passengerWeaponStationPitch" -> {
                return (bone, vehicle, state) -> {
                    float a = vehicle.getTurretYaw(state.getPartialTick());
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
                            -Mth.lerp(state.getPartialTick(), vehicle.gunXRotO, vehicle.getGunXRot()) * Mth.DEG_TO_RAD
                                    - r * pitch * Mth.DEG_TO_RAD
                                    - r2 * roll * Mth.DEG_TO_RAD,
                            -10 * Mth.DEG_TO_RAD, 60 * Mth.DEG_TO_RAD)
                    );
                };
            }
        }

        // track(Mov|Rot)[RL]\d+
        var trackMatcher = TRACK_PATTERN.matcher(boneName);
        if (trackMatcher.matches()) {
            var isRot = trackMatcher.group("type").equals("Rot");
            var isL = trackMatcher.group("direction").equals("L");
            var index = Integer.parseInt(trackMatcher.group("id"));

            if (isRot) {
                if (isL) {
                    return (bone, vehicle, state) -> {
                        float t = wrap(leftTrack + 2 * index, vehicle);
                        bone.setRotX(-getBoneRotX(t) * Mth.DEG_TO_RAD);
                    };
                } else {
                    return (bone, vehicle, state) -> {
                        float t2 = wrap(rightTrack + 2 * index, vehicle);
                        bone.setRotX(-getBoneRotX(t2) * Mth.DEG_TO_RAD);
                    };
                }
            } else {
                if (isL) {
                    return (bone, vehicle, state) -> {
                        float t = wrap(leftTrack + 2 * index, vehicle);
                        bone.setPosY(getBoneMoveY(t));
                        bone.setPosZ(getBoneMoveZ(t));
                    };
                } else {
                    return (bone, vehicle, state) -> {
                        float t2 = wrap(rightTrack + 2 * index, vehicle);
                        bone.setPosY(getBoneMoveY(t2));
                        bone.setPosZ(getBoneMoveZ(t2));
                    };
                }
            }
        }

        var wheelMatcher = WHEEL_PATTERN.matcher(boneName);
        if (wheelMatcher.matches()) {
            var isL = wheelMatcher.group("direction").equals("L");

            if (boneName.endsWith("Turn")) {
                return (bone, vehicle, state) -> {
                    bone.setRotX(1.5f * (isL ? leftWheelRot : rightWheelRot));
                    bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.rudderRotO, vehicle.getRudderRot()));
                };
            } else {
                return (bone, vehicle, state) -> bone.setRotX(1.5f * (isL ? leftWheelRot : rightWheelRot));
            }
        }

        return null;
    }
}
