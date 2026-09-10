package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F", cancellable = true)
    private static void getNightVisionScale(LivingEntity living, float num, CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue(0f);
    }

    /**
     * 遥控本项目无人车且处于第一人称相机类型时，隐藏手持物品（监控平板），
     * 避免无人车视角里看到玩家手上的物品。
     */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void dr$hideHeldItemForR6Drone(PoseStack poseStack, Camera camera, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        var stack = mc.player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) return;
        var tag = stack.getOrCreateTag();
        if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) return;
        if (com.redabysslucia.dragonrise_reforge.client.R6DroneClientLookup
                .findDrone(mc.player.level(), tag.getString(MonitorItem.LINKED_DRONE)) != null) {
            ci.cancel();
        }
    }
}
