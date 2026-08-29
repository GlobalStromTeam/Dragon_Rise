package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage;
import com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage;
import com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage;
import com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 1.21.1 使用 payload 体系替代 Forge 的 SimpleChannel。
 * 所有消息都注册到同一个 PROTOCOL_VERSION 下。
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(EngineChangeModeMessage.TYPE, EngineChangeModeMessage.STREAM_CODEC, EngineChangeModeMessage::handle);
        registrar.playToServer(SetFireControlMessage.TYPE, SetFireControlMessage.STREAM_CODEC, SetFireControlMessage::handle);
        registrar.playToServer(ToggleTakeoverMessage.TYPE, ToggleTakeoverMessage.STREAM_CODEC, ToggleTakeoverMessage::handle);
        registrar.playToServer(R6DroneControlMessage.TYPE, R6DroneControlMessage.STREAM_CODEC, R6DroneControlMessage::handle);
        registrar.playToClient(OwnBombMessage.TYPE, OwnBombMessage.STREAM_CODEC, OwnBombMessage::handle);
    }
}
