package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.network.message.send.MouseMoveMessage;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * 只有 1 号席位能操控飞机：丢弃非 0 席位遥控者发来的 MouseMoveMessage。
 * <p>
 * 原版"谁在驾驶"的控制点在客户端（{@code vehicle.firstPassenger == player} 才发键盘位包），
 * 而遥控分支（手持监控平板）没有席位判断 -> 四个席位都能发 -> 谁都能开飞机。
 * 这里在服务端补一道权威判断：遥控者席位 > 0 时丢弃该包。
 * 真乘客（{@code player.vehicle != null}）保持 SBW 原行为；其余席位仍可看、可切武器、可用自己席位的武器。
 * <p>
 * <b>remap = false</b>：目标方法 destroy/handler 等都是 SBW 自有方法（不在原版映射表里），
 * 生产环境保持原名，因此不做重映射。
 */
@Mixin(value = MouseMoveMessage.class, remap = false)
public class MouseMoveMessageSeatOneOnlyMixin {

    @Inject(method = "handler(Ljava/util/function/Supplier;)V", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void dragonrise$onlySeatOneDrives(Supplier<NetworkEvent.Context> ctx, CallbackInfo ci) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer sender = context.getSender();
        if (sender == null || RemotePilotHelper.riddenVehicle(sender) != null) {
            return;     // 真乘客：保持 SBW 原行为
        }
        if (RemotePilotHelper.remoteSeat(sender) > 0) {
            ci.cancel();    // 非 1 号席位的遥控者：不参与驾驶操纵
        }
    }
}
