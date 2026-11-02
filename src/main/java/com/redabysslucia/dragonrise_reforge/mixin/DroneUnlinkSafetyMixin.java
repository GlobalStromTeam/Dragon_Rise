package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.entities.AbstractRemotePilotDrone;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 解绑安全化：SBW 的无人机被摧毁/撞毁/自爆时会解掉"控制器主手平板"的链接，
 * 但原实现<b>不检查那台平板链接的是不是自己</b>：
 * <pre>
 * val controller = findPlayer(level, this.entityData.get(CONTROLLER))
 * if (controller != null &amp;&amp; controller.mainHandItem.is(MONITOR)) disLink(controller.mainHandItem, controller)
 * </pre>
 * 于是"玩家以前绑定过的另一台无人机"（CONTROLLER 仍记着该玩家）被炸毁时，
 * 会把玩家手里那台<b>指向别的载具</b>的平板一起解绑 ——
 * 表现就是"炸到别的无人机时，有概率和控制中的无人机掉链接"。
 * <p>
 * 这里给 {@code DroneEntity} 里那几处 disLink 调用补一道与本机 UUID 的比对
 * （官方在 destroy() 的另一处循环里本来就是这么写的），只有平板确实指向本机才解绑。
 * 处理器非静态：Mixin 合并进 DroneEntity 后 this 就是那台无人机，才能拿到本机 UUID。
 * <p>
 * <b>按方法分开写</b>：{@code destroy} / {@code interact} 是"本机真死了/玩家自己解绑"，
 * 放行即可；{@code hitEntityCrash} / {@code baseTick} 是"挂着炸弹撞毁/自爆"分支，
 * 对本模组的遥控无人机一律不解绑（撞一下掉链接体验太差），只在其它 SBW 无人机上保留原语义。
 * 两者的真实解绑/拦截都会打一条 {@code [DR-LINK]} 日志，方便定位线上偶发掉链接。
 * <p>
 * <b>remap = false</b>：目标方法 destroy/handler 等都是 SBW 自有方法（不在原版映射表里），
 * 生产环境保持原名，因此不做重映射；处理器里也不直接调原版成员（统一走 remote pilot helper）。
 */
@Mixin(value = DroneEntity.class, remap = false)
public class DroneUnlinkSafetyMixin {

    private static final String DISLINK_TAG =
            "Lcom/atsuishio/superbwarfare/item/misc/MonitorItem$Companion;disLink"
                    + "(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/entity/player/Player;)V";

    /** 无人机被打爆：只有平板确实指向本机才解绑 */
    @Redirect(method = "destroy()V", require = 0,
            at = @At(value = "INVOKE", target = DISLINK_TAG))
    private void dragonrise$unlinkOnDestroy(MonitorItem.Companion companion, CompoundTag tag, Player player) {
        if (RemotePilotHelper.tagPointsAt((Entity) (Object) this, tag)) {
            RemotePilotHelper.logDroneUnlink((Entity) (Object) this, player, "destroy");
            MonitorItem.disLink(tag, player);
        } else {
            RemotePilotHelper.logDroneUnlinkSkip((Entity) (Object) this, player, "destroy");
        }
    }

    /** 玩家自己右键解绑：同样只解"确实链着本机"的那块平板 */
    @Redirect(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            require = 0, at = @At(value = "INVOKE", target = DISLINK_TAG))
    private void dragonrise$unlinkOnInteract(MonitorItem.Companion companion, CompoundTag tag, Player player) {
        if (RemotePilotHelper.tagPointsAt((Entity) (Object) this, tag)) {
            RemotePilotHelper.logDroneUnlink((Entity) (Object) this, player, "interact");
            MonitorItem.disLink(tag, player);
        } else {
            RemotePilotHelper.logDroneUnlinkSkip((Entity) (Object) this, player, "interact");
        }
    }

    /** 撞毁分支：本模组遥控无人机不解绑（SBW 原语义只对挂着炸弹的无人机成立） */
    @Redirect(method = "hitEntityCrash(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V",
            require = 0, at = @At(value = "INVOKE", target = DISLINK_TAG))
    private void dragonrise$keepLinkOnCrash(MonitorItem.Companion companion, CompoundTag tag, Player player) {
        if ((Object) this instanceof AbstractRemotePilotDrone) {
            RemotePilotHelper.logDroneUnlinkSkip((Entity) (Object) this, player, "hitEntityCrash(ours)");
            return;
        }
        if (RemotePilotHelper.tagPointsAt((Entity) (Object) this, tag)) {
            RemotePilotHelper.logDroneUnlink((Entity) (Object) this, player, "hitEntityCrash");
            MonitorItem.disLink(tag, player);
        }
    }

    /** 自爆分支（IS_KAMIKAZE）：同上，本模组遥控无人机不掉链接 */
    @Redirect(method = "baseTick()V", require = 0,
            at = @At(value = "INVOKE", target = DISLINK_TAG))
    private void dragonrise$keepLinkOnKamikaze(MonitorItem.Companion companion, CompoundTag tag, Player player) {
        if ((Object) this instanceof AbstractRemotePilotDrone) {
            RemotePilotHelper.logDroneUnlinkSkip((Entity) (Object) this, player, "baseTick(ours)");
            return;
        }
        if (RemotePilotHelper.tagPointsAt((Entity) (Object) this, tag)) {
            RemotePilotHelper.logDroneUnlink((Entity) (Object) this, player, "baseTick");
            MonitorItem.disLink(tag, player);
        }
    }
}
