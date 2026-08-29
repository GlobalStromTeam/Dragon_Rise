package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.overlay.CannonBallisticOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.M270BallisticOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.SupplyProgressOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import com.redabysslucia.dragonrise_reforge.resource.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "vehicle_background"),
            new VehicleBackgroundOverlay()
        );
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_progress"),
            new SupplyProgressOverlay()
        );
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "m270_ballistic"),
            new M270BallisticOverlay()
        );
        event.registerBelowAll(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "cannon_ballistic"),
            new CannonBallisticOverlay()
        );
        Dragonrise_reforge.LOGGER.info("Dragonrise gui layers registered");
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(VehicleModelReloadListener.INSTANCE);
        event.registerReloadListener(VehicleLODModelReloadListener.INSTANCE);
        event.registerReloadListener(ArmorModelReloadListener.INSTANCE);
        event.registerReloadListener(EntityModelReloadListener.INSTANCE);
        event.registerReloadListener(ItemModelReloadListener.INSTANCE);
        event.registerReloadListener(ProjectileModelReloadListener.INSTANCE);
        event.registerReloadListener(BlockModelReloadListener.INSTANCE);
        Dragonrise_reforge.LOGGER.info("Dragonrise SBM model reload listeners registered");
    }
}
