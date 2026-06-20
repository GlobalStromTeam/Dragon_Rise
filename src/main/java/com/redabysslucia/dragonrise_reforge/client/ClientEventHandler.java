package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.overlay.SupplyProgressOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "vehicle_background"),
            new VehicleBackgroundOverlay()
        );
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_progress"),
            new SupplyProgressOverlay()
        );
        Dragonrise_reforge.LOGGER.info("Dragonrise overlays registered");
    }
}
