package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 遥控者也走 SBW 原生的载具开火循环 {@code ClientEventHandler.handleVehicleGunShoot}。
 * <p>
 * 官方循环负责：Semi/Auto 射击模式、按武器射速的间隔、低帧率补发、过热/装填状态、
 * 客户端预测与 1P 炮声（{@code clientShootVehicle} ➜ {@code playVehicleClientSounds}）。
 * 我此前自己写了一套"按 RPM 计时 + 自己发包 + 自己播音效"，那是重复造轮子；
 * 它的门槛只有一句 {@code val vehicle = player.vehicle}，遥控者不骑乘 → 直接 return。
 * <p>
 * 这里把该处取值改判为"该玩家正在遥控的飞机"，官方循环即可照常驱动遥控者的武器。
 * <p>
 * <b>remap</b>：方法选择器 {@code handleVehicleGunShoot} 是 SBW 自有方法（类级 remap = false），
 * 而 @At 目标 {@code LocalPlayer.getVehicle()} 是原版方法（单独 remap = true）。
 * handler 内不直接调原版成员，统一走 {@link RemotePilotHelper}（remap = false 下自身引用不会被重映射）。
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerRemoteGunShootMixin {

    @Redirect(method = "handleVehicleGunShoot", require = 0,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$vehicleOrRemoteAircraft(LocalPlayer self) {
        return RemotePilotHelper.vehicleOrRemoteAircraft(self);
    }
}
