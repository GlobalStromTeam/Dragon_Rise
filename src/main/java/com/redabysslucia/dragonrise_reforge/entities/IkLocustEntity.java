package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class IkLocustEntity extends VehicleEntity {

    public IkLocustEntity(EntityType<IkLocustEntity> type, Level world) {
        super(type, world);
    }

    /**
     * 抵消固定翼引擎（aircraftEngine → updateRotation）里写死的 -0.06 虚构下坠配平：
     * 原版把机头每 tick 配平向 "实际速度 + (0,-0.06,0)" 的方向，速度为零（无重力悬浮）时
     * 该方向纯朝下，机头会被缓缓拉向 +90°（下垂）。
     * 这里在 super.travel()（引擎物理已执行）之后，把因虚构下坠多产生的低头增量精确撤销，
     * 配平参考退化为"实际速度"：静止时保持玩家姿态，高速/爬升/俯冲时与原版行为几乎一致。
     */
    @Override
    public void travel() {
        super.travel();
        cancelFakeNoseDrop();
    }

    private void cancelFakeNoseDrop() {
        // 镜像 superb updateRotation 的触发条件（!onGround、tickCount > 5、|xRot| < 90）
        if (onGround() || isWreck() || tickCount <= 5) return;
        if (Math.abs(getXRot()) >= 90f) return;

        var v = getDeltaMovement();
        double horiz = v.horizontalDistance();

        // 原版配平目标（带 -0.06 虚构下坠），常量与 superb VehicleEngineUtils.updateRotation 一致
        float targetFake = (float) -Math.toDegrees(Math.atan2(v.y - 0.06, horiz));
        // 无虚构时用实际速度配平的目标
        float targetReal = (float) -Math.toDegrees(Math.atan2(v.y, horiz));

        float diffFake = Mth.wrapDegrees(targetFake - getXRot());
        float diffReal = Mth.wrapDegrees(targetReal - getXRot());
        // updateRotation 每 tick 增量 0.01 * diffFake，这里撤掉它超出真实配平的部分
        setXRot(getXRot() - 0.01f * (diffFake - diffReal));
    }
}
