package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.model.sbm.DragonriseModelReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonriseModelLoaders {

    public static final DragonriseModelReloadListener VEHICLE =
            new DragonriseModelReloadListener("models/bedrock/vehicle", "");

    @SubscribeEvent
    public static void onAddClientResourceListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(VEHICLE);
    }
}
