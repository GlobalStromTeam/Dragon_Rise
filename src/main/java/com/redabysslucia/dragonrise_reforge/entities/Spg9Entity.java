package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * SPG-9 无后坐力炮（73mm，苏制牵引/三脚架式）：EngineType=Fixed 的固定武器，
 * 由 SPG9Deployer 部署（物品 id dragonrise_reforge:spg9），与 DShK / M2 等固定武器同类。
 *
 * <p>开火动画（仅客户端）：开火时播放 animation.model.new（后坐，3.3333s），
 * 播完后自动接 animation.model.new2（复位，3.3333s），两者顺序播放。
 * 触发入口是 {@link com.redabysslucia.dragonrise_reforge.client.event.VehicleFireAnimationHandler}
 * （客户端开火只会派发 ClientVehicleFireEvent，不会调用 vehicleShoot）。
 */
public class Spg9Entity extends DragonriseVehicleBase {

    /** 开火后坐动画 */
    private static final String FIRE_ANIM = "animation.model.new";
    /** 复位动画（后坐结束后播放） */
    private static final String RETURN_ANIM = "animation.model.new2";
    /** 后坐动画时长（3.3333s ≈ 67 tick），到点后切复位动画 */
    private static final int FIRE_ANIM_TICKS = 67;

    /** 后坐动画剩余 tick，>0 表示序列进行中（仅客户端使用） */
    private int fireAnimTimer;

    public Spg9Entity(EntityType<?> type, Level world) {
        super(type, world);
    }

    /** 客户端开火时触发：播放后坐动画并启动复位计时 */
    public void playFireAnimation() {
        var ani = getAnimationInstance();
        if (ani == null) return;
        var ctx = ani.getContext();
        ctx.stopAnimation(RETURN_ANIM, 0);
        ctx.playAnimation(FIRE_ANIM, AnimationPlayType.PLAY_ONCE_HOLD, 0);
        this.fireAnimTimer = FIRE_ANIM_TICKS;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        // 后坐动画播完，顺序接上复位动画
        if (level().isClientSide && this.fireAnimTimer > 0 && --this.fireAnimTimer == 0) {
            var ani = getAnimationInstance();
            if (ani != null) {
                var ctx = ani.getContext();
                ctx.stopAnimation(FIRE_ANIM, 0);
                ctx.playAnimation(RETURN_ANIM, AnimationPlayType.PLAY_ONCE_HOLD, 0);
            }
        }
    }
}
