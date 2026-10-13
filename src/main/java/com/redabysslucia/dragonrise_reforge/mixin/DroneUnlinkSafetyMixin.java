package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * 这里给 DroneEntity 里那几处 disLink 调用补一道与本机 UUID 的比对
 * （官方在 destroy() 的另一处循环里本来就是这么写的），只有平板确实指向本机才解绑。
 * 处理器非静态：Mixin 合并进 DroneEntity 后 this 就是那台无人机，才能拿到本机 UUID。
 * <p>
 * <b>remap = false</b>：目标方法 destroy/handler 等都是 SBW 自有方法（不在原版映射表里），
 * 生产环境保持原名，因此不做重映射。
 */
@Mixin(value = DroneEntity.class, remap = false)
public class DroneUnlinkSafetyMixin {

    @Redirect(method = {
            "baseTick()V",
            "hitEntityCrash(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V",
            "destroy()V",
            "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
    }, require = 0, at = @At(value = "INVOKE",
            target = "Lcom/atsuishio/superbwarfare/item/misc/MonitorItem$Companion;disLink(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/entity/player/Player;)V"))
    private void dragonrise$disLinkOnlyIfPointsToMe(MonitorItem.Companion companion, net.minecraft.nbt.CompoundTag tag, Player player) {
        if (tag == null || player == null) {
            return;
        }
        // 只用本模组 helper：本 mixin remap = false，自身调原版成员打包后会 NoSuchMethodError。
        ItemStack stack = player.getMainHandItem();
        if (RemotePilotHelper.monitorPointsAt(
                ((net.minecraft.world.entity.Entity) (Object) this), stack)) {
            // SBW 0.8.9.2 起 disLink 只改 CompoundTag、不回写 ItemStack，必须自行 saveTag。
            MonitorItem.disLink(tag, player);
            com.atsuishio.superbwarfare.tools.NBTTool.saveTag(stack, tag);
        }
    }
}
