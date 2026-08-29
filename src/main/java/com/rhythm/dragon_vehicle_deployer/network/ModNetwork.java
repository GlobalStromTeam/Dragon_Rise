package com.rhythm.dragon_vehicle_deployer.network;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(DragonVehicleDeployer.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(id++, DeployerSettingsPacket.class,
                DeployerSettingsPacket::encode,
                DeployerSettingsPacket::decode,
                DeployerSettingsPacket::handle);
    }
}
