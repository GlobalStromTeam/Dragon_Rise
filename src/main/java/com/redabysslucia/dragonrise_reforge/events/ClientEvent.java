package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.entities.utils.INightVisionVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//
//        var mc = Minecraft.getInstance();
//        Player player = mc.player;
//        if (player == null) return;
//        if (player.isSpectator()) return;
//
//        if (player.getVehicle() instanceof INightVisionVehicle vehicle && vehicle.getNVEnable()) {
//            Dragonrise_reforge.LOGGER.debug("1");
//
//            Minecraft.getInstance().gameRenderer.loadEffect(vehicle.getNightVisionShader());
//        } else {
//            if(mc.gameRenderer.currentEffect()!= null && mc.gameRenderer.currentEffect().getName().contains("night-vision")) {
//                Minecraft.getInstance().gameRenderer.shutdownEffect();
//            }
//
//        }
//    }
    public static void onEntityMount(EntityMountEvent event) {

        if (event.getEntityMounting() instanceof Player player && event.getEntityBeingMounted() instanceof INightVisionVehicle vehicle) {
            var mc = Minecraft.getInstance();
            Player localPlayer = mc.player;
            //if (player.isSpectator()) return;

            if (player == localPlayer && vehicle.getNVEnable()) {

                if (event.isMounting()) {
                    mc.gameRenderer.loadEffect(vehicle.getNightVisionShader());
                } else {
                    if (mc.gameRenderer.currentEffect() != null && mc.gameRenderer.currentEffect().getName().contains("night-vision")) {
                        mc.gameRenderer.shutdownEffect();
                    }
                }
            }
        }

    }
}
