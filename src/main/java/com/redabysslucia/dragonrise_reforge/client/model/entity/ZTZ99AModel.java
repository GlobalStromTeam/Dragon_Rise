package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.*;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.YAW_WHILE_SHOOT;

public class ZTZ99AModel extends DragonriseVehicleModel<ZTZ99AEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    private static final ResourceLocation[] TEXTURE_RESOURCES = {
            new ResourceLocation("dragonrise_reforge:textures/entity/ztz99a.png"),
            new ResourceLocation("dragonrise_reforge:textures/entity/ztz99asand.png"),
    };

    @Override
    public ResourceLocation getTextureResource(ZTZ99AEntity entity) {
        if (entity != null) {
            int camoType = entity.getCamoType();
            if (camoType >= 0 && camoType < TEXTURE_RESOURCES.length) {
                return TEXTURE_RESOURCES[camoType];
            }
        }

        return TEXTURE_RESOURCES[0];
    }

//    public @Nullable TransformContext<ZTZ99AEntity> collectTransform(String boneName) {
//        // target hide root
//        if (boneName.equals("root") && hideForTurretControllerWhileZooming()) {
//            return (bone, vehicle, state) -> bone.setHidden(getHideForTurretControllerWhileZooming());
//        }
//
//        // 瞄准时隐藏乘客武器站
//        if (boneName.equals("passengerWeaponStation") && hideForTurretControllerWhileZooming()) {
//            return (bone, vehicle, state) -> bone.setHidden(getHideForPassengerWeaponStationControllerWhileZooming());
//        }
//
//        //射击时带来的车体摇晃视觉效果
//        switch (boneName) {
//            case "base" -> {
//                return (bone, vehicle, state) -> {
//                    float a = vehicle.getEntityData().get(YAW_WHILE_SHOOT);
//                    float r = (Mth.abs(a) - 90f) / 90f;
//
//                    float r2;
//
//                    if (Mth.abs(a) <= 90f) {
//                        r2 = a / 90f;
//                    } else {
//                        if (a < 0) {
//                            r2 = -(180f + a) / 90f;
//                        } else {
//                            r2 = (180f - a) / 90f;
//                        }
//                    }
//
//                    bone.setPosX(r2 * getRecoilShake() * 0.5f);
//                    bone.setPosZ(r * getRecoilShake() * 1f);
//                    bone.setRotX((r * getRecoilShake() - 2.5f ) * Mth.DEG_TO_RAD);
//                    bone.setRotZ(r2 * getRecoilShake() * Mth.DEG_TO_RAD);
//                };
//            }
//
//            // turret
//            case "turret" -> {
//                return (bone, vehicle, state) -> {
//                    bone.setRotY(getTurretYRot() * Mth.DEG_TO_RAD);
//
//                    //bone.setRotX((float) (-2 * Math.cos(getTurretYRot() * Mth.DEG_TO_RAD) + 2)* Mth.DEG_TO_RAD);
//
//                    //bone.setRotZ((float) ( 12 * Math.sin(getTurretYRot() * Mth.DEG_TO_RAD) )* Mth.DEG_TO_RAD);
//                };
//            }
//
//            // barrel
//            case "barrel" -> {
//                return (bone, vehicle, state) -> {
//                    float a = getTurretYaw();
//                    float r = (Mth.abs(a) - 90f) / 90f;
//
//                    float r2;
//
//                    if (Mth.abs(a) <= 90f) {
//                        r2 = a / 90f;
//                    } else {
//                        if (a < 0) {
//                            r2 = -(180f + a) / 90f;
//                        } else {
//                            r2 = (180f - a) / 90f;
//                        }
//                    }
//
//                    //两倍于车体倾斜
//                    bone.setRotX(Mth.clamp(-getTurretXRot() - r * getPitch() - r2 * getRoll()+5 -(5 * Math.abs(getTurretYRot()/180)), vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
//                    //bone.setRotX((float) (Mth.clamp(-getTurretXRot() - r * getPitch() - r2 * getRoll() -(5 * Math.abs(getTurretYRot()/180)), -180,180) * Mth.DEG_TO_RAD));
//                };
//            }
//
//            // turret上的成员武器站Yaw
//            case "passengerWeaponStationYaw" -> {
//                return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getGunYRotO(), vehicle.getGunYRot()) * Mth.DEG_TO_RAD - getTurretYRot() * Mth.DEG_TO_RAD);
//            }
//
//            // turret上的成员武器站Pitch
//            case "passengerWeaponStationPitch" -> {
//                return (bone, vehicle, state) -> {
//                    float a = vehicle.getTurretYaw(state.getPartialTick());
//                    float r = (Mth.abs(a) - 90f) / 90f;
//
//                    float r2;
//
//                    if (Mth.abs(a) <= 90f) {
//                        r2 = a / 90f;
//                    } else {
//                        if (a < 0) {
//                            r2 = -(180f + a) / 90f;
//                        } else {
//                            r2 = (180f - a) / 90f;
//                        }
//                    }
//
//                    bone.setRotX(Mth.clamp(
//                            -Mth.lerp(state.getPartialTick(), vehicle.getGunXRotO(), vehicle.getGunXRot()) * Mth.DEG_TO_RAD
//                                    - r * getPitch() * Mth.DEG_TO_RAD
//                                    - r2 * getRoll() * Mth.DEG_TO_RAD,
//                            -10 * Mth.DEG_TO_RAD, 60 * Mth.DEG_TO_RAD)
//                    );
//                };
//            }
//        }
//
//        // track(Mov|Rot)[RL]\d+
//        var trackMatcher = VehicleModel.Companion.getTRACK_PATTERN().matcher(boneName);
//        if (trackMatcher.matches()) {
//            var isRot = trackMatcher.group("type").equals("Rot");
//            var isL = trackMatcher.group("direction").equals("L");
//            var index = Integer.parseInt(trackMatcher.group("id"));
//
//            if (isRot) {
//                if (isL) {
//                    return (bone, vehicle, state) -> {
//                        float t = wrap(getLeftTrack() + 2 * index, vehicle);
//                        bone.setRotX(-getBoneRotX(t) * Mth.DEG_TO_RAD);
//                    };
//                } else {
//                    return (bone, vehicle, state) -> {
//                        float t2 = wrap(getRightTrack() + 2 * index, vehicle);
//                        bone.setRotX(-getBoneRotX(t2) * Mth.DEG_TO_RAD);
//                    };
//                }
//            } else {
//                if (isL) {
//                    return (bone, vehicle, state) -> {
//                        float t = wrap(getLeftTrack() + 2 * index, vehicle);
//                        bone.setPosY(getBoneMoveY(t));
//                        bone.setPosZ(getBoneMoveZ(t));
//                    };
//                } else {
//                    return (bone, vehicle, state) -> {
//                        float t2 = wrap(getRightTrack() + 2 * index, vehicle);
//                        bone.setPosY(getBoneMoveY(t2));
//                        bone.setPosZ(getBoneMoveZ(t2));
//                    };
//                }
//            }
//        }
//
//        var wheelMatcher = VehicleModel.Companion.getWHEEL_PATTERN().matcher(boneName);
//        if (wheelMatcher.matches()) {
//            var isL = wheelMatcher.group("direction").equals("L");
//
//            if (boneName.endsWith("Turn")) {
//                return (bone, vehicle, state) -> {
//                    bone.setRotX(1.5f * (isL ? getLeftWheelRot() : getRightWheelRot()));
//                    bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getRudderRotO(), vehicle.getRudderRot()));
//                };
//            } else {
//                return (bone, vehicle, state) -> bone.setRotX(1.5f * (isL ? getLeftWheelRot() : getRightWheelRot()));
//            }
//        }
//
//        return null;
//    }
//    public float getBoneRotX(float t) {
//        if (t <= 34.25) return -4F;
//        if (t <= 34.5833) return Mth.lerp((t - 34.25F) / (34.5833F - 34.25F), -4F, -49F);
//        if (t <= 36.0833) return -49F;
//        if (t <= 36.3333) return Mth.lerp((t - 36.0833F) / (36.3333F - 36.0833F), -49F, -89F);
//        if (t <= 37.5) return -89F;
//        if (t <= 37.6667) return Mth.lerp((t - 37.5F) / (37.6667F - 37.5F), -89F, -134F);
//        if (t <= 38.5833) return -134F;
//        if (t <= 38.6667) return Mth.lerp((t - 38.5833F) / (38.6667F - 38.5833F), -134F, -154F);
//        if (t <= 43.6667) return -154F;
//        if (t <= 43.75) return Mth.lerp((t - 43.6667F) / (43.75F - 43.6667F), -154F, -180F);
//        if (t <= 70.1667) return -180F;
//        if (t <= 70.3333) return Mth.lerp((t - 70.1667F) / (70.3333F - 70.1667F), -180F, -215F);
//        if (t <= 75.5833) return -215F;
//        if (t <= 75.75) return Mth.lerp((t - 75.5833F) / (75.75F - 75.5833F), -215F, -235F);
//        if (t <= 76.9167) return Mth.lerp((t - 75.75F) / (76.9167F - 75.75F), -235F, -260F);
//        if (t <= 77.25) return Mth.lerp((t - 76.9167F) / (77.25F - 76.9167F), -260F, -305F);
//        if (t <= 77.8333) return -305F;
//        if (t <= 77.9167) return -305F;
//        if (t <= 78.9167) return Mth.lerp(t - 77.9167F, -305F, -365F);
//
//        return -365F;
//    }
//        public float getBoneMoveY(float t) {
//            if (t <= 34.25) return Mth.lerp(t / (34.25F - 0F), 0F, -6F);
//            if (t <= 36.0833) return Mth.lerp((t - 34.25F) / (36.0833F - 34.25F), -6F, -11.25F);
//            if (t <= 37.5) return Mth.lerp((t - 36.0833F) / (37.5F - 36.0833F), -11.25F, -16.96F);
//            if (t <= 38.5833) return Mth.lerp((t - 37.5F) / (38.5833F - 37.5F), -16.96F, -19.25F);
//            if (t <= 43.6667) return Mth.lerp((t - 38.5833F) / (43.6667F - 38.5833F), -19.25F, -24F);
//            if (t <= 70.1667) return -24F;
//            if (t <= 75.5833) return Mth.lerp((t - 70.1667F) / (75.5833F - 70.1667F), -24F, -14F);
//            if (t <= 76.25) return Mth.lerp((t - 75.5833F) / (76.25F - 75.5833F), -14F, -9.24F);
//            if (t <= 77) return Mth.lerp((t - 76.25F) / (77F - 76.25F), -9.24F, -5F);
//            if (t <= 78.3333) return Mth.lerp((t - 77F) / (78.3333F - 77F), -5F, -1F);
//
//            return Mth.lerp((t - 78.3333F) / (80.1667F - 78.3333F), -1F, 0F);
//        }
//            public float getBoneMoveZ(float t) {
//                if (t <= 34.25) return Mth.lerp(t / (34.25F - 0F), 0F, 123F);
//                if (t <= 36.0833) return Mth.lerp((t - 34.25F) / (36.0833F - 34.25F), 123F, 128F);
//                if (t <= 37.5) return 128F;
//                if (t <= 38.5833) return Mth.lerp((t - 37.5F) / (38.5833F - 37.5F), 128F, 125F);
//                if (t <= 43.6667) return Mth.lerp((t - 38.5833F) / (43.6667F - 38.5833F), 125F, 110.75F);
//                if (t <= 70.1667) return Mth.lerp((t - 43.6667F) / (70.1667F - 43.6667F), 110.75F, 13.75F);
//                if (t <= 75.5833) return Mth.lerp((t - 70.1667F) / (75.5833F - 70.1667F), 13.75F, -1F);
//                if (t <= 76.25) return Mth.lerp((t - 75.5833F) / (76.25F - 75.5833F), -1F, -4.65F);
//                if (t <= 77) return Mth.lerp((t - 76.25F) / (77F - 76.25F), -4.65F, -5F);
//                if (t <= 78.3333) return Mth.lerp((t - 77F) / (78.3333F - 77F), -5F, -2.5F);
//
//                return Mth.lerp((t - 78.3333F) / (80.1667F - 78.3333F), -2.5F, 0F);
//
//
//            }
}
