package com.redabysslucia.dragonrise_reforge.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(EngineChangeModeMessage.TYPE, EngineChangeModeMessage.STREAM_CODEC, EngineChangeModeMessage::handle);
    }
}
