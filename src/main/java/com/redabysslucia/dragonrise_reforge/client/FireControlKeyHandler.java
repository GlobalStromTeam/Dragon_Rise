package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.client.screen.FiringSolutionScreen;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireControlKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!ModKeyMappings.OPEN_FIRE_CONTROL.isClicked()) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null) return;

        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) return;
        if (!(vehicle instanceof IndirectFireVehicleBase)) return;
        if (vehicle.getSeatIndex(player) != vehicle.getTurretControllerIndex()) return;

        minecraft.setScreen(new FiringSolutionScreen(vehicle, player));
    }
}
