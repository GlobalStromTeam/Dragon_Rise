package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.entities.utils.INightVisionVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (player.isSpectator()) return;

        if (player.getVehicle() instanceof INightVisionVehicle vehicle) {
            if (vehicle.getNVEnable()) {
                // 应用夜视效果
                if (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().contains("night-vision")) {
                    mc.gameRenderer.loadEffect(vehicle.getNightVisionShader());
                }
            } else if (vehicle.getTVGEnable()) {
                // 应用热成像效果
                if (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().contains("thermal-vision")) {
                    mc.gameRenderer.loadEffect(vehicle.getThermalVisionShader());
                }
            } else {
                // 关闭所有效果
                if (mc.gameRenderer.currentEffect() != null && 
                    (mc.gameRenderer.currentEffect().getName().contains("night-vision") || 
                     mc.gameRenderer.currentEffect().getName().contains("thermal-vision"))) {
                    mc.gameRenderer.shutdownEffect();
                }
            }
        } else {
            // 不在车辆上，关闭所有效果
            if (mc.gameRenderer.currentEffect() != null && 
                (mc.gameRenderer.currentEffect().getName().contains("night-vision") || 
                 mc.gameRenderer.currentEffect().getName().contains("thermal-vision"))) {
                mc.gameRenderer.shutdownEffect();
            }
        }
    }
    
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {

        if (event.getEntityMounting() instanceof Player player && event.getEntityBeingMounted() instanceof INightVisionVehicle vehicle) {
            var mc = Minecraft.getInstance();
            Player localPlayer = mc.player;

            if (player == localPlayer) {
                if (event.isMounting()) {
                    // 挂载时应用相应的效果
                    if (vehicle.getNVEnable()) {
                        mc.gameRenderer.loadEffect(vehicle.getNightVisionShader());
                    } else if (vehicle.getTVGEnable()) {
                        mc.gameRenderer.loadEffect(vehicle.getThermalVisionShader());
                    }
                } else {
                    // 卸载时关闭所有效果
                    if (mc.gameRenderer.currentEffect() != null && 
                        (mc.gameRenderer.currentEffect().getName().contains("night-vision") || 
                         mc.gameRenderer.currentEffect().getName().contains("thermal-vision"))) {
                        mc.gameRenderer.shutdownEffect();
                    }
                }
            }
        }

    }
}
