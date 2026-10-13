package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.IkLocustEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * 引擎矢量推力动画（参照 A10 襟翼的渲染方式：渲染器里对骨骼 rotation 叠加 rotateX）。
 * move_enginLeft / move_enginRight 默认朝向后面：
 *
 * 1. 基础下倾：速度 0 时引擎旋转 +90° 朝向地面；随着飞船加速角度逐渐归 0，
 *    达到 120 km/h（≈1.6667 格/tick）时完全归 0，引擎恢复水平朝后。
 * 2. 偏航差动（0 ~ 80 km/h 有效，随速度线性衰减，80 km/h 完全消失）：
 *    幅度 = 10° × 速度因子 × 偏航速率因子（偏航越快倾角越大）。
 *    向左偏航：左发 +、右发 -；向右偏航：左发 -、右发 +。
 * 3. 滚转差动（速度 &gt; 80 km/h 有效）：
 *    幅度 = 10° × 滚转角度因子（30° 滚转达满幅度）。
 *    向左滚转：左发 -、右发 +；向右滚转：左发 +、右发 -。
 */
public class IkLocustRenderer extends GeoVehicleRenderer<IkLocustEntity> {

    /** 120 km/h 换算：120/3.6 m/s ÷ 20 tick/s ≈ 1.66667 格/tick（引擎完全收回） */
    private static final double FULL_SPEED = 120.0 / 3.6 / 20.0;

    /** 80 km/h ≈ 1.11111 格/tick（偏航差动消失 / 滚转差动生效分界） */
    private static final double DIFF_SPEED_LIMIT = 80.0 / 3.6 / 20.0;

    /** 速度 0 时引擎下倾角（度） */
    private static final float MAX_TILT = 90f;

    /** 偏航 / 滚转差动最大幅度（度） */
    private static final float DIFF_MAX = 10f;

    /** 偏航速率满幅度阈值（度/tick，0.5 ≈ 10°/s） */
    private static final float YAW_RATE_FULL = 0.5f;

    /** 滚转满幅度所需倾角（度） */
    private static final float ROLL_ANGLE_FULL = 30f;

    /** 上一 tick 的左右引擎角度（用于 partialTicks 插值平滑） */
    private float prevLeft;
    private float prevRight;
    private int prevTick = -1;

    public IkLocustRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(IkLocustEntity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        if (entity.isWreck()) return;

        double speed = entity.getDeltaMovement().length();

        // 1. 基础下倾：速度 0 → 90°，120 km/h → 0°
        float base = MAX_TILT * (1f - Mth.clamp((float) (speed / FULL_SPEED), 0f, 1f));

        // 2. 偏航差动：方向取实际偏航速率（yRot 每 tick 变化），幅度随速率
        float yawRate = entity.getYRot() - entity.yRotO;
        float yawSpeedFactor = 1f - Mth.clamp((float) (speed / DIFF_SPEED_LIMIT), 0f, 1f);
        float yawRateFactor = Mth.clamp(Math.abs(yawRate) / YAW_RATE_FULL, 0f, 1f);
        float yawDiff = DIFF_MAX * yawSpeedFactor * yawRateFactor;
        float yawLeft = yawRate < 0 ? yawDiff : -yawDiff;   // 向左偏航：左发 +
        float yawRight = yawRate < 0 ? -yawDiff : yawDiff;

        // 3. 滚转差动：速度 > 80 km/h 有效，幅度随滚转角度
        float rollAngle = entity.getRoll();
        float rollDiff = 0f;
        if (speed > DIFF_SPEED_LIMIT) {
            rollDiff = DIFF_MAX * Mth.clamp(Math.abs(rollAngle) / ROLL_ANGLE_FULL, 0f, 1f);
        }
        float rollLeft = rollAngle < 0 ? -rollDiff : rollDiff;  // 向左滚转：左发 -
        float rollRight = rollAngle < 0 ? rollDiff : -rollDiff;

        float leftAngle = base + yawLeft + rollLeft;
        float rightAngle = base + yawRight + rollRight;

        int tick = entity.tickCount;
        if (tick != prevTick) {
            prevLeft = leftAngle;
            prevRight = rightAngle;
            prevTick = tick;
        }
        float renderLeft = Mth.lerp(partialTicks, prevLeft, leftAngle);
        float renderRight = Mth.lerp(partialTicks, prevRight, rightAngle);

        BoneState enginLeft = instance.getBone("move_enginLeft");
        if (enginLeft != null) {
            enginLeft.rotation.rotateX(renderLeft * Mth.DEG_TO_RAD);
        }
        BoneState enginRight = instance.getBone("move_enginRight");
        if (enginRight != null) {
            enginRight.rotation.rotateX(renderRight * Mth.DEG_TO_RAD);
        }
    }
}
