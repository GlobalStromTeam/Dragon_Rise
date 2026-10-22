package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.network.message.send.VehicleFireMessage;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 遥控者的开火包走 SBW 原生的 {@code VehicleFireMessage}。
 * <p>
 * 官方客户端开火时发的就是这个包（带锁定目标与目标点）。服务端 handler 里若
 * {@code shooterVehicleId} 为空，就用 {@code player.vehicle} 兜底取载具 ——
 * 遥控者不骑乘 → 取不到 → 整包被丢弃。这里把该兜底取值改判为"该玩家正在遥控的飞机"，
 * 于是官方整条开火链（客户端循环 ➜ 原生包 ➜ 服务端 {@code vehicleShoot}）对遥控者生效，
 * 本模组自有的开火包随之删除。
 * <p>
 * <b>remap</b>：方法选择器 {@code handler} 是 SBW 自有方法（类级 remap = false），
 * 而 @At 目标 {@code ServerPlayer.getVehicle()} 是原版方法（单独 remap = true）。
 * handler 内不直接调原版成员，统一走 {@link RemotePilotHelper}。
 */
@Mixin(value = VehicleFireMessage.class, remap = false)
public class VehicleFireMessageRemoteMixin {

    @Redirect(method = "handler", require = 0,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/server/level/ServerPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$vehicleOrRemoteAircraft(ServerPlayer self) {
        return RemotePilotHelper.vehicleOrRemoteAircraft(self);
    }
}
