package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvent {
    private static float lastSyncCameraYRot = 0;
    private static float lastSyncCameraXRot = 0;
    private static boolean isFirstSyncCameraFrame = true;

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.getVehicle() instanceof SyncCameraVehicle vehicle) {
            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

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
