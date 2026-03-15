package com.redabysslucia.dragonrise_reforge.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.*;

public final class AirshipEngineUtils {
    public static void airshipEngine(VehicleEntity vehicle, AirshipInfo engineInfo) {
        // 获取引擎参数
        double buoyancy = engineInfo.buoyancy;               // 恒定浮力（向上加速度）
        float liftSpeedRate = engineInfo.liftSpeedRate;      // 上升加速度（按下up时）
        float sinkSpeedRate = engineInfo.sinkSpeedRate;      // 下降加速度（按下down时）
        //float thrustCoeff = engineInfo.thrustCoefficient;   // 水平推力系数
        float steeringSpeed = engineInfo.steeringSpeed;     // 转向速度
        float maxForwardSpeed = engineInfo.maxForwardSpeedRate; // 最大前进速度
        float maxBackwardSpeed = engineInfo.maxBackwardSpeedRate; // 最大后退速度
        float powerAdd = engineInfo.increment;              // 动力增加速率
        float powerReduce = engineInfo.decrement;           // 动力减少速率
        float dragHorizontal = engineInfo.dragHorizontal;   // 水平阻尼
        float dragVertical = engineInfo.dragVertical;       // 垂直阻尼
        int energyCost = (int) (engineInfo.energyCostRate * Mth.abs(vehicle.getEntityData().get(POWER)));

        // 能量不足时重置所有输入并衰减动力
        if (vehicle.getEnergy() < energyCost || (vehicle.getMaxEnergy() > 0 && vehicle.getEnergy() <= 0)) {
            vehicle.setForwardInputDown(false);
            vehicle.setBackInputDown(false);
            vehicle.setLeftInputDown(false);
            vehicle.setRightInputDown(false);
            vehicle.setUpInputDown(false);
            vehicle.setDownInputDown(false);
            vehicle.getEntityData().set(POWER, vehicle.getEntityData().get(POWER) * 0.95f);
            vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) * 0.5f);
        }

        // 无乘客时重置所有输入，动力归零
        if (vehicle.getFirstPassenger() == null) {
            vehicle.setLeftInputDown(false);
            vehicle.setRightInputDown(false);
            vehicle.setForwardInputDown(false);
            vehicle.setBackInputDown(false);
            vehicle.setUpInputDown(false);
            vehicle.setDownInputDown(false);
            vehicle.getEntityData().set(POWER, 0f);
        }

        // 水平动力输入（前进/后退）
        if (vehicle.forwardInputDown()) {
            vehicle.getEntityData().set(POWER,
                    Math.min(vehicle.getEntityData().get(POWER) + (vehicle.getEntityData().get(POWER) < 0 ? powerAdd * 2f : powerAdd), 1));
        }
        if (vehicle.backInputDown()) {
            vehicle.getEntityData().set(POWER, org.joml.Math.max(vehicle.getEntityData().get(POWER) - (vehicle.getEntityData().get(POWER) > 0 ? powerReduce * 2f : powerReduce), -1));
            if (vehicle.rightInputDown()) {
                vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) + steeringSpeed);
            } else if (vehicle.leftInputDown()) {
                vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) - steeringSpeed);
            }
        } else {
            if (vehicle.rightInputDown()) {
                vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) - steeringSpeed);
            } else if (vehicle.leftInputDown()) {
                vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) + steeringSpeed);
            }
        }

        // 无水平输入时动力自然衰减
        if (!vehicle.forwardInputDown() && !vehicle.backInputDown()) {
            vehicle.getEntityData().set(POWER, vehicle.getEntityData().get(POWER) * 0.96f);
        }

        // 获取当前速度向量
        Vec3 delta = vehicle.getDeltaMovement();

        // 垂直输入（上升/下降）
        if (vehicle.upInputDown()) {
             delta = delta.add(0, liftSpeedRate, 0);
        }
        if (vehicle.downInputDown()) {
            delta = delta.add(0, -sinkSpeedRate, 0);
        }

        // 应用恒定浮力
        delta = delta.add(0, buoyancy, 0);

        // 水平推力：沿视线方向施加推力，大小与当前动力成正比
        Vec3 viewVec = vehicle.getViewVector(1);
        delta = delta.add(viewVec.scale(vehicle.getEntityData().get(POWER)));

        // 应用空气阻力（水平和垂直阻尼不同）
        delta = delta.multiply(1-dragHorizontal, 1-dragVertical, 1-dragHorizontal);

        // 限制水平速度不超过最大前进/后退速度（可选，防止无限加速）
        double horizSpeed = delta.horizontalDistance();
        float currentPower = vehicle.getEntityData().get(POWER);
        if (currentPower > 0 && horizSpeed > maxForwardSpeed) {
            delta = new Vec3(delta.x / horizSpeed * maxForwardSpeed, delta.y, delta.z / horizSpeed * maxForwardSpeed);
        } else if (currentPower < 0 && horizSpeed > maxBackwardSpeed) {
            // 后退时速度方向可能与视线相反，简单限制标量大小
            delta = new Vec3(delta.x / horizSpeed * maxBackwardSpeed, delta.y, delta.z / horizSpeed * maxBackwardSpeed);
        }

        vehicle.setDeltaMovement(delta);

        // 根据转向增量更新偏航角（类似履带式）
        float yawDelta = (float) (vehicle.getEntityData().get(DELTA_ROT) * 6.0); // 6为经验转向系数
        vehicle.setYRot(vehicle.getYRot() - yawDelta);

        // 转向增量自然衰减
        vehicle.getEntityData().set(DELTA_ROT, vehicle.getEntityData().get(DELTA_ROT) * 0.85f);

        // 仅在服务器端消耗能量
        if (vehicle.level() instanceof ServerLevel) {
            vehicle.consumeEnergy(energyCost);
        }

        // 引擎损坏影响
        if (vehicle.getEntityData().get(MAIN_ENGINE_DAMAGED)) {
            vehicle.getEntityData().set(POWER, vehicle.getEntityData().get(POWER) * 0.96f);
        }

        // 可选：螺旋桨动画
        // vehicle.setPropellerRot(vehicle.getPropellerRot() + 30 * vehicle.getEntityData().get(POWER));
    }
}
