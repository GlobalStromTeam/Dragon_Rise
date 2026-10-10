package com.redabysslucia.dragonrise_reforge.client.animation;

import com.atsuishio.superbwarfare.client.animation.AnimationPlayType;
import com.redabysslucia.dragonrise_reforge.entities.HYR0Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * HYR0 的客户端动画驱动（只在客户端加载，服务端不会引用到里面的客户端类）。
 * <p>
 * <b>为什么自己驱动开火动画</b>：卓越前线的开火动画是靠名字约定 {@code animation.<武器名>.fire} 自动播的，
 * 而本包载具的武器名是 PascalCase（{@code Cannon} / {@code MainMachineGun} / {@code PassengerMachineGun}），
 * 动画名是小写（{@code animation.cannon.fire} 等），精确匹配不上；改名又会牵连模型骨骼与 OBB 武器位置，
 * 所以这里直接用每门武器的 {@code shootTimer > 0}（开火后约 5 tick 的窗口）自己驱动。
 * <ul>
 *   <li>主炮 {@code animation.cannon.fire}：单发，上升沿播放一次（不循环）</li>
 *   <li>同轴机枪 {@code animation.main_machine_gun.idle}：开火期间循环，停火淡出</li>
 *   <li>车顶武器站 {@code animation.passenger_machine_gun.fire}：开火期间循环，停火淡出</li>
 * </ul>
 * 另外负责"幻影坦克"变树动画：{@code animation.turn_to_tree.on}（PLAY_ONCE_HOLD，停在树的姿态）；
 * 变回车不播动画，直接停掉变树动画即刻恢复原始姿态（见 {@link #playTurnToTree}）。
 */
@OnlyIn(Dist.CLIENT)
public final class HYR0Animations {

    /** 变树（停在树姿态） */
    public static final String TREE_ON = "animation.turn_to_tree.on";
    /** 变回车 */
    public static final String TREE_OFF = "animation.turn_to_tree.off";
    /** 主炮开火（单发） */
    public static final String CANNON_FIRE = "animation.cannon.fire";
    /** 同轴机枪开火（循环） */
    public static final String MAIN_MG_FIRE = "animation.main_machine_gun.idle";
    /** 车顶武器站开火（循环） */
    public static final String PASSENGER_MG_FIRE = "animation.passenger_machine_gun.fire";
    /**
     * 车顶武器站 idle：注意 —— 动画文件里这段的内容与 {@code turn_to_tree.on} 完全相同（树形关键帧），
     * 必须阻止它自动播放，否则车辆常态就是一棵树。
     */
    public static final String PASSENGER_MG_IDLE = "animation.passenger_machine_gun.idle";

    private HYR0Animations() {
    }

    /**
     * 变树 / 变回车。
     * <p>
     * <b>变树</b>：播放 {@code animation.turn_to_tree.on}（PLAY_ONCE_HOLD，动画结束后停在树的姿态）。<br>
     * <b>变回车</b>：<b>不播放</b> {@code animation.turn_to_tree.off}，而是直接把变树动画停掉
     * （fade = 0，无过渡）—— 模型立刻回到原始静止姿态：{@code root.scale} 恢复 1（载具出现）、
     * {@code OU} 恢复静止尺寸（树消失），所以是"触发即变回车"，没有 5 秒回退动画。
     * 履带的显隐由 {@code HYR0Renderer} 按 {@code isTree()} 每帧处理，因此也随之一同瞬间恢复。
     */
    public static void playTurnToTree(HYR0Entity entity, boolean toTree) {
        var anim = entity.getAnimationInstance();
        if (anim == null) {
            return;
        }
        var context = anim.getContext();
        if (toTree) {
            context.playAnimation(TREE_ON, AnimationPlayType.PLAY_ONCE_HOLD, 0);
            context.stopAnimation(TREE_OFF, 0);
        } else {
            context.stopAnimation(TREE_ON, 0);      // 立即生效：模型回到原始（载具）姿态
            context.stopAnimation(TREE_OFF, 0);     // 保险：确保回退动画也不残留
        }
    }

    /**
     * 开火动画开关（只在状态翻转时调用，避免每 tick 重启动画）。
     *
     * @param animation 动画名
     * @param loop      true = 开火期间循环（机枪类）；false = 上升沿播一次（主炮）
     * @param firing    本 tick 是否处于开火窗口
     */
    public static void setFiring(HYR0Entity entity, String animation, boolean loop, boolean firing) {
        var anim = entity.getAnimationInstance();
        if (anim == null) {
            return;
        }
        var context = anim.getContext();
        if (firing) {
            context.playAnimation(animation, loop ? AnimationPlayType.LOOP : AnimationPlayType.PLAY_ONCE_STOP, 0);
        } else if (loop) {
            context.stopAnimation(animation, 4);
        }
    }

    /**
     * 首次进入客户端 tick 时做两件事：
     * <ol>
     *   <li>停掉 {@code animation.passenger_machine_gun.idle}：它的内容与 {@code turn_to_tree.on} 完全相同
     *       （树形关键帧），而卓越前线会默认循环播放所有 {@code .idle} 动画，导致车一出生就是树形态。
     *       该动画不再自动播放；等动画文件里把这段 idle 换成真正的机枪 idle，再决定是否恢复。</li>
     *   <li>停掉同轴机枪 idle（同样会被默认循环播放），改为开火时才播。</li>
     * </ol>
     */
    public static void suppressAutoIdle(HYR0Entity entity) {
        var anim = entity.getAnimationInstance();
        if (anim == null) {
            return;
        }
        var context = anim.getContext();
        context.stopAnimation(PASSENGER_MG_IDLE, 0);
        context.stopAnimation(MAIN_MG_FIRE, 0);
    }
}
