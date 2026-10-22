package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.network.message.send.SwitchVehicleWeaponMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 遥控者也能用 SBW 原生的载具切武器包 {@code SwitchVehicleWeaponMessage}。
 * <p>
 * 官方客户端（数字键 / 滚轮）发的就是这个包，但服务端 handler 里用
 * {@code player.vehicle} 取载具 —— 遥控者不骑乘 → 拿不到载具 → 整包被丢弃。
 * 这里把该处取值改判为"该玩家正在遥控的飞机"，其余流程（
 * {@code vehicle.hasWeapon(getSeatIndex(player))} + {@code changeWeapon(index, value, isScroll)}）
 * 全部是官方代码。
 * <p>
 * <b>remap 说明</b>：方法选择器 {@code handler} 是 SBW 自有方法（不在原版映射表里），
 * 因此类级 {@code remap = false}；而 @At 目标 {@code ServerPlayer.getVehicle()} 是<b>原版</b>方法，
 * 生产环境是 SRG 名，需要单独让 @At 走重映射。
 */
@Mixin(value = SwitchVehicleWeaponMessage.class, remap = false)
public class SwitchVehicleWeaponMessageRemoteMixin {

    @Redirect(method = "handler", require = 0,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/server/level/ServerPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$vehicleOrRemoteAircraft(ServerPlayer self) {
        // 注意：本 mixin remap = false，handler 里不能直接调原版成员（打包会 NoSuchMethodError），
        // 所以统一走本模组的 helper。
        return com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper.vehicleOrRemoteAircraft(self);
    }
}
