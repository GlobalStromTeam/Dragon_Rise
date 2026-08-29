package com.rhythm.dragon_vehicle_deployer.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(DeployerSettingsPacket.TYPE, DeployerSettingsPacket.STREAM_CODEC, DeployerSettingsPacket::handle);
    }
}
