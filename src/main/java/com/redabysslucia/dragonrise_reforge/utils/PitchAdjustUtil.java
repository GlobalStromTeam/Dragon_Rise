package com.redabysslucia.dragonrise_reforge.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class PitchAdjustUtil {
    /**
     * Adjustment格式：{{yawStart,yawEnd,move,minAdjust,maxAdjust},...}
     * 需要调整pitch的yaw区间（前两个），yaw过渡值，pitch在区间内的调整值（下限和上限，加算）
     * 以{120f, 180f, 5f, 2f, -10f}举例
     * 当yaw为120~180时，最大仰角增加2，最大俯角增加-10，即减少10
     * 当yaw为115~120时，最大仰角随yaw接近120而线性增加2，最大俯角随yaw接近120而线性减少10
     */
    public static void adjustedPassengerPitchOnTurret(Entity entity, float turretMinPitch, float turretMaxPitch, VehicleEntity vehicle, Float[][] pitchAdjustments) {
        float yaw = vehicle.getTurretYaw(1);
        float r = (Mth.abs(yaw) - 90f) / 90f;

        float r2;

        if (Mth.abs(yaw) <= 90f) {
            r2 = yaw / 90f;
        } else {
            if (yaw < 0) {
                r2 = -(180f + yaw) / 90f;
            } else {
                r2 = (180f - yaw) / 90f;
            }
        }

        float min = -turretMaxPitch - r * vehicle.getXRot() - r2 * vehicle.getRoll();
        float max = -turretMinPitch - r * vehicle.getXRot() - r2 * vehicle.getRoll();

        //调整pitch上下限
        for (Float[] adjust : pitchAdjustments) {
            float yawStart = adjust[0];
            float yawEnd = adjust[1];
            float move = adjust[2];
            float minAdjust = adjust[3];
            float maxAdjust = adjust[4];
            if (yaw >= 0) {
                if (yaw >= yawStart && yaw <= yawEnd) {
                    min += minAdjust;
                    max += maxAdjust;
                } else if (yaw >= yawStart - move && yaw < yawStart) {
                    float factor = Mth.lerp((yawStart-yaw)/move,1,0 );
                    min += minAdjust * factor;
                    max += maxAdjust * factor;
                }
            }else {
                if (yaw <= yawStart && yaw >= yawEnd) {
                    min += minAdjust;
                    max += maxAdjust;
                } else if (yaw <= yawStart + move && yaw > yawStart) {
                    float factor = Mth.lerp((yawStart - yaw) / (-move), 1, 0);
                    min += minAdjust * factor;
                    max += maxAdjust * factor;
                }
            }
        }

        float f = Mth.wrapDegrees(entity.getXRot());
        float f1 = Mth.clamp(f, min, max);
        entity.xRotO += f1 - f;
        entity.setXRot(entity.getXRot() + f1 - f);
    }
}
