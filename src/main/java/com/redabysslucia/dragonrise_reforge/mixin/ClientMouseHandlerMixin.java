package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.event.ClientMouseHandler;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SBW 在遥控 monitor 时会把玩家鼠标灵敏度清零（changeSensitivity → 0），
 * 用于无人机视角由鼠标消息驱动；本项目无人车相机直接读玩家视角，
 * 因此控制本项目无人车时恢复灵敏度，让视角可自由转动。
 */
@Mixin(value = ClientMouseHandler.class, remap = false)
public class ClientMouseHandlerMixin {

    @Inject(method = "changeSensitivity", at = @At("HEAD"), cancellable = true)
    private static void dr$keepSensitivityForR6Drone(double original, CallbackInfoReturnable<Double> cir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) return;
        var tag = stack.getOrCreateTag();
        if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) return;

        if (com.redabysslucia.dragonrise_reforge.client.R6DroneClientLookup
                .findDrone(player.level(), tag.getString(MonitorItem.LINKED_DRONE)) != null) {
            cir.setReturnValue(original);
        }
    }
}
