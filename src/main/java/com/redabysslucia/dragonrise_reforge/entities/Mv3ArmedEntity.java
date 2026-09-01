package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * MV-3 武装型（mv3_armed）：车顶舱盖（canggai 骨骼）随四号座位（seatIndex 3）联动。
 * 参照 superb 本体 A10 舵面动画模式：baseTick 客户端边沿触发，
 * 座位有人 → 播放 animation.mv3_armed.canggai_open（向后掀开 110°，PLAY_ONCE_HOLD 保持末帧）；
 * 座位无人 → 播放 canggai_close（回归原位）。
 */
public class Mv3ArmedEntity extends VehicleEntity {

    /** 触发舱盖的座位号（五号座位，车顶炮手位） */
    private static final int CANGGAI_SEAT_INDEX = 4;

    private boolean wasCanggaiOpen;

    public Mv3ArmedEntity(EntityType<Mv3ArmedEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.tickCanggaiAnimation();
    }

    private void tickCanggaiAnimation() {
        if (!level().isClientSide()) return;
        var animationInstance = getAnim();
        if (animationInstance == null) return;
        var ctx = animationInstance.getContext();

        boolean occupied = false;
        for (Entity passenger : getPassengers()) {
            if (getSeatIndex(passenger) == CANGGAI_SEAT_INDEX) {
                occupied = true;
                break;
            }
        }

        String openAnim = "animation.mv3_armed.canggai_open";
        String closeAnim = "animation.mv3_armed.canggai_close";

        if (occupied && !wasCanggaiOpen) {
            ctx.stopAnimation(closeAnim, 0);
            ctx.playAnimation(openAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        } else if (!occupied && wasCanggaiOpen) {
            ctx.stopAnimation(openAnim, 0);
            ctx.playAnimation(closeAnim, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        }
        wasCanggaiOpen = occupied;
    }
}
