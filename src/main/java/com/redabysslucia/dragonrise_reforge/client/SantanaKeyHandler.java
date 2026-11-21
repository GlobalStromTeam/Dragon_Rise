package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.SantanaEntity;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import com.redabysslucia.dragonrise_reforge.network.message.SantanaLightMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 桑塔纳点灯按键：V 键开关循环动画 + 循环音。
 *
 * <p>只有正坐在桑塔纳里时才发送，避免与已有的 {@code ENGINE_CHANGE_MODE_TOGGLE}
 * （同样默认 V）互相干扰 —— 那个键只在飞机上生效，这里只在桑塔纳上生效。
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SantanaKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        // consumeClick 会清空队列，用 while 防止丢按键
        while (ModKeyMappings.SANTANA_ANIMATION.isClicked()) {
            if (player.getVehicle() instanceof SantanaEntity santana) {
                PacketDistributor.sendToServer(new SantanaLightMessage(santana.getId()));
            }
        }
    }
}
