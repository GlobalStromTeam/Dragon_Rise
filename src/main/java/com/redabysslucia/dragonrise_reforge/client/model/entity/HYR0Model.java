package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.HYR0Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTQ15Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.YAW_WHILE_SHOOT;

public class HYR0Model extends VehicleModel<HYR0Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    public @Nullable TransformContext<HYR0Entity> collectTransform(String boneName) {
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
                    bone.setRotX(Mth.clamp(-turretXRot - r * pitch - r2 * roll  -(5 * Math.abs(turretYRot/180)), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
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

    public float getBoneRotX(float t) {
        if (t <= 6) return 0F;
        if (t <= 6.5) return Mth.lerp((t - 6F) / (6.5F - 6F), 0F, 22.5F);
        if (t <= 7.5) return Mth.lerp(t - 6.5F, 22.5F, 37.5F);
        if (t <= 25.25) return 37.5F;
        if (t <= 26) return Mth.lerp((t - 25.25F) / (26F - 25.25F), 37.5F, 52.5F);
        if (t <= 27) return Mth.lerp(t - 26F, 52.5F, 72.5F);
        if (t <= 34) return 72.5F;
        if (t <= 35) return Mth.lerp(t - 34F, 72.5F, 37.5F);
        if (t <= 36.5) return Mth.lerp((t - 35F) / (36.5F - 35F), 37.5F, 5F);
        if (t <= 44.5) return 5F;
        if (t <= 45) return Mth.lerp((t - 44.5F) / (45F - 44.5F), 5F, 37.5F);
        if (t <= 68) return 37.5F;
        if (t <= 68.5) return Mth.lerp((t - 68F) / (68.5F - 68F), 37.5F, 70F);
        if (t <= 76) return 70F;
        if (t <= 76.5) return Mth.lerp((t - 76F) / (76.5F - 76F), 70F, 112.5F);
        if (t <= 78) return 112.5F;
        if (t <= 78.5) return Mth.lerp((t - 78F) / (78.5F - 78F), 112.5F, 150F);
        if (t <= 79.9583) return 150F;

        return Mth.lerp((t - 79.9583F) / (80F - 79.9583F), 150F, 0F);
    }
        public float getBoneMoveY(float t){
            if (t <= 6) return Mth.lerp(t / 6F, 0F, -10.02F);
            if (t <= 7.5) return Mth.lerp((t - 6F) / (7.5F - 6F), -10.02F, -11.05F);
            if (t <= 25.5) return Mth.lerp((t - 7.5F) / (25.5F - 7.5F), -11.05F, -11.08F);
            if (t <= 26.75) return Mth.lerp((t - 25.5F) / (26.75F - 25.5F), -11.08F, -10.03F);
            if (t <= 34) return Mth.lerp((t - 26.75F) / (34F - 26.75F), -10.03F, 0.61F);
            if (t <= 37) return Mth.lerp((t - 34F) / (37F - 34F), 0.61F, -0.58F);
            if (t <= 44.5) return Mth.lerp((t - 37F) / (44.5F - 37F), -0.58F, -11.21F);
            if (t <= 68) return Mth.lerp((t - 44.5F) / (68F - 44.5F), -11.21F, -10.77F);
            if (t <= 76) return Mth.lerp((t - 68F) / (76F - 68F), -10.77F, -0.58F);
            if (t <= 76.5) return Mth.lerp((t - 76F) / (76.5F - 76F), -0.58F, 1.12F);
            if (t <= 78) return Mth.lerp((t - 76.5F) / (78F - 76.5F), 1.12F, 6.21F);
            if (t <= 79.9583) return Mth.lerp((t - 78F) / (79.9583F - 78F), 6.21F, 13F);

            return Mth.lerp((t - 79.9583F) / (80F - 79.9583F), 13F, 0F);
        }
            public float getBoneMoveZ(float t) {
                if (t <= 6) return Mth.lerp(t / 6F, 0F, 12.98F);
                if (t <= 7.5) return Mth.lerp((t - 6F) / (7.5F - 6F), 12.98F, 16.22F);
                if (t <= 25.5) return Mth.lerp((t - 7.5F) / (25.5F - 7.5F), 16.22F, 55.14F);
                if (t <= 26.75) return Mth.lerp((t - 25.5F) / (26.75F - 25.5F), 55.14F, 58.14F);
                if (t <= 34) return Mth.lerp((t - 26.75F) / (34F - 26.75F), 58.14F, 73.52F);
                if (t <= 37) return Mth.lerp((t - 34F) / (37F - 34F), 73.52F, 80.01F);
                if (t <= 44.5) return Mth.lerp((t - 37F) / (44.5F - 37F), 80.01F, 96.23F);
                if (t <= 68) return Mth.lerp((t - 44.5F) / (68F - 44.5F), 96.23F, 158.06F);
                if (t <= 76) return Mth.lerp((t - 68F) / (76F - 68F), 158.06F, 174.35F);
                if (t <= 76.5) return Mth.lerp((t - 76F) / (76.5F - 76F), 174.35F, 174.88F);
                if (t <= 78) return Mth.lerp((t - 76.5F) / (78F - 76.5F), 174.88F, 175.88F);
                if (t <= 79.9583) return Mth.lerp((t - 78F) / (79.9583F - 78F), 175.88F, 173F);

                return Mth.lerp((t - 79.9583F) / (80F - 79.9583F), 173F, 0F);


            }
                    }
