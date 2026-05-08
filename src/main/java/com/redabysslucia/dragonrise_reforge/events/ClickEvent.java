package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.entities.utils.INightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.EngineChangeModeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClickEvent {
    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (player.isSpectator()) return;
        int key = event.getKey();
        if (key < 0) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {

            // NIGHT_VISION key temporarily disabled
            // if (player.getVehicle() instanceof INightVisionVehicle vehicle) {
            //     if (ModKeyMappings.NIGHT_VISION.isPressed()) {
                    // 循环切换模式：夜视 -> 热成像 -> 关闭
                    // if (vehicle.getNVEnable()) {
                        // 从夜视切换到热成像
                    //     vehicle.setNVEnable(false);
                    //     vehicle.setTVGEnable(true);
                    //     mc.gameRenderer.loadEffect(vehicle.getThermalVisionShader());
                    // } else if (vehicle.getTVGEnable()) {
                        // 从热成像切换到关闭
                    //     vehicle.setTVGEnable(false);
                    //     if (mc.gameRenderer.currentEffect() != null && 
                    //         (mc.gameRenderer.currentEffect().getName().contains("night-vision") || 
                    //          mc.gameRenderer.currentEffect().getName().contains("thermal-vision"))) {
                    //         mc.gameRenderer.shutdownEffect();
                    //     }
                    // } else {
                        // 从关闭切换到夜视
                    //     vehicle.setNVEnable(true);
                    //     mc.gameRenderer.loadEffect(vehicle.getNightVisionShader());
                    // }
                // }
            // }

            if(player.getVehicle() instanceof VariableEngineVehicle entity){
                if (ModKeyMappings.ENGINE_CHANGE_MODE_TOGGLE.isPressed()) {
                    //entity.nextMode();
                    ModNetwork.PACKET_HANDLER.sendToServer(new EngineChangeModeMessage());
                }
            }

        }
    }
}
