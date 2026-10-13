package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.network.message.send.VehicleMovementMessage;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遥控驾驶时，只有 1 号中位（驾驶位）的遥控者可以驱动载具。
 *
 * <p>1.21.1 迁移：SBW 0.8.9.2 的 {@code VehicleMovementMessage#handler} 签名已从
 * Forge 的 {@code handler(Supplier<NetworkEvent.Context>)} 变为
 * NeoForge 的 {@code handler(IPayloadContext)}，注入点描述符与参数类型同步更新。
 */
@Mixin(VehicleMovementMessage.class)
public class VehicleMovementMessageSeatOneOnlyMixin {

    @Inject(method = "handler(Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void dragonrise$onlySeatOneDrives(IPayloadContext ctx, CallbackInfo ci) {
        if (!(ctx.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (RemotePilotHelper.riddenVehicle(sender) != null) {
            return;     // 真乘客：保持 SBW 原行为
        }
        if (RemotePilotHelper.remoteSeat(sender) > 0) {
            ci.cancel();    // 非 1 号席位的遥控者：不参与驾驶操纵
        }
    }
}
