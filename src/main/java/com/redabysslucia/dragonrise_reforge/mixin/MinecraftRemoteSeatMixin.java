package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.network.NetworkRegistry;
import com.atsuishio.superbwarfare.network.message.send.SwitchVehicleWeaponMessage;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.RemoteSeatSwitchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遥控驾驶时用 <b>Shift + 数字键</b> 切换遥控席位。
 * <p>
 * SBW 原生的 {@code MinecraftMixin.handleKeybinds} 只处理"玩家真的坐在载具上"的情况
 * （{@code player.getVehicle() instanceof VehicleEntity}），遥控者不在载具上，官方那条路走不到。
 * 这里在同一个位置补一条：遥控本模组无人机时，Shift + 1..4 切席位，
 * 并吃掉这次数字键（避免同时切物品栏），与官方手感一致（官方也是 consumeClick + cancel）。
 */
@Mixin(Minecraft.class)
public class MinecraftRemoteSeatMixin {

    @Inject(method = "handleKeybinds()V", at = @At("HEAD"), cancellable = true)
    private void dragonrise$remoteSeatSwitch(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        // 只接管"遥控本模组飞机"的情形，其余一切照旧
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return;
        }
        var tag = stack.getOrCreateTag();
        if (!tag.getBoolean("Using") || !tag.getBoolean("Linked")) {
            return;
        }
        var drone = EntityFindUtil.findDrone(player.level(), tag.getString("LinkedDrone"));
        if (!(drone instanceof ShuangweixieEntity aircraft)) {
            return;
        }

        // 用 consumeClick（按下事件）而不是 isDown（按住状态）：
        // 官方 SBW 用的是 isDown，快速点按有概率漏掉；这里更灵敏。
        int index = -1;
        for (int i = 0; i < 9; ++i) {
            if (mc.options.keyHotbarSlots[i].consumeClick()) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return;
        }

        // 数字键被接管（官方骑乘载具时也是这样：数字键 = 切武器 / Shift+数字 = 切席位）
        ci.cancel();

        if (ModKeyMappings.CHANGE_SEAT.isDown()) {
            // Shift + 数字：切遥控席位
            if (index < Math.max(1, aircraft.getMaxPassengers())) {
                ModNetwork.PACKET_HANDLER.sendToServer(new RemoteSeatSwitchMessage(index));
            }
        } else {
            // 数字：切武器（官方 SwitchVehicleWeaponMessage 要求 player.vehicle，遥控者走不到）
            int seat = aircraft.getSeatIndex(player);
            if (seat >= 0 && aircraft.hasWeapon(seat)) {
                // 走 SBW 原生切武器包（第一个参数是席位号，与官方客户端一致）
                com.atsuishio.superbwarfare.network.NetworkRegistry.PACKET_HANDLER.sendToServer(
                        new SwitchVehicleWeaponMessage(seat, index, false));
            }
        }
    }
}
