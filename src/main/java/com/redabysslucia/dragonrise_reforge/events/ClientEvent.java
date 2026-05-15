package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvent {
    private static float lastSyncCameraYRot = 0;
    private static float lastSyncCameraXRot = 0;
    private static boolean isFirstSyncCameraFrame = true;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.getVehicle() instanceof SyncCameraVehicle vehicle) {
            float partialTicks = event.renderTickTime;
            
            float prevYRot = vehicle.yRotO;
            float currYRot = vehicle.getYRot();
            float prevXRot = vehicle.xRotO;
            float currXRot = vehicle.getXRot();
            
            float interpolatedYRot = net.minecraft.util.Mth.lerp(partialTicks, prevYRot, currYRot);
            float interpolatedXRot = net.minecraft.util.Mth.lerp(partialTicks, prevXRot, currXRot);
            
            if (isFirstSyncCameraFrame) {
                lastSyncCameraYRot = interpolatedYRot;
                lastSyncCameraXRot = interpolatedXRot;
                isFirstSyncCameraFrame = false;
                return;
            }

            float yRotDelta = interpolatedYRot - lastSyncCameraYRot;
            float xRotDelta = interpolatedXRot - lastSyncCameraXRot;
            
            // 处理旋转环绕问题 (-180到180度)
            yRotDelta = net.minecraft.util.Mth.wrapDegrees(yRotDelta);
            
            player.setYHeadRot(player.getYHeadRot() + yRotDelta);
            player.setYRot(player.getYRot() + yRotDelta);
            player.setXRot(player.getXRot() + xRotDelta);
            
            lastSyncCameraYRot = interpolatedYRot;
            lastSyncCameraXRot = interpolatedXRot;
        } else {
            isFirstSyncCameraFrame = true;
        }
    }
}
