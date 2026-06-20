package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.model.sbm.DragonriseModelReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonriseModelLoaders {

    public static final DragonriseModelReloadListener VEHICLE =
            new DragonriseModelReloadListener("models/bedrock/vehicle", "");

    @SubscribeEvent
    public static void onAddClientResourceListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(VEHICLE);
    }
}
