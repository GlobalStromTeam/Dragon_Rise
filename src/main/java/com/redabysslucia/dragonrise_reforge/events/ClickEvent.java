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

            if(player.getVehicle() instanceof VariableEngineVehicle entity){
                if (ModKeyMappings.ENGINE_CHANGE_MODE_TOGGLE.isPressed()) {
                    //entity.nextMode();
                    ModNetwork.PACKET_HANDLER.sendToServer(new EngineChangeModeMessage());
                }
            }

        }
    }
}
