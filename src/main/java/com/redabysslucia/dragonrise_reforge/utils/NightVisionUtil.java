package com.redabysslucia.dragonrise_reforge.utils;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.INightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NightVisionUtil {

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

        if (event.getEntityMounting() instanceof Player && event.getEntityBeingMounted() instanceof INightVisionVehicle vehicle) {
            var mc = Minecraft.getInstance();

            //if (player.isSpectator()) return;

            if (vehicle.getNVEnable()) {

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

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (player.isSpectator()) return;
        int key = event.getKey();
        if (key < 0) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {

            if (player.getVehicle() instanceof INightVisionVehicle vehicle) {
                if (ModKeyMappings.NIGHT_VISION.isPressed()) {

                    vehicle.setNVEnable(!vehicle.getNVEnable());

                    if (vehicle.getNVEnable()) {
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
}
