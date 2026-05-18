package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.overlay.SupplyProgressOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll(
            Dragonrise_reforge.MODID + "_vehicle_background",
            new VehicleBackgroundOverlay()
        );
        event.registerBelowAll(
            Dragonrise_reforge.MODID + "_supply_progress",
            new SupplyProgressOverlay()
        );
        Dragonrise_reforge.LOGGER.info("Dragonrise overlays registered");
    }
}
