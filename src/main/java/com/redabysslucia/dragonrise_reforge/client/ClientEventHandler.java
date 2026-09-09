package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.overlay.CannonBallisticOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.M270BallisticOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.SupplyProgressOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import com.redabysslucia.dragonrise_reforge.client.particle.SignalSmokeParticle;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModParticleTypes;
import com.redabysslucia.dragonrise_reforge.resource.model.*;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.SIGNAL_SMOKE.get(), SignalSmokeParticle.Provider::new);
    }

    /**
     * 把照明弹实体注册为 Dynamic Lights 动态光源（亮度 15）。
     * 框架的实体光源集合按 DynamicLightHandlers 注册表填充（canLightUp/getLuminance），
     * 只靠 mixin 拦截帮助方法不够 —— 注册表里有条目才会把实体纳入光源集合。
     * （机制同 HandheldMoon 盈月的 Setup。）
     */
    @SubscribeEvent
    public static void registerStarShellDynamicLight(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                DynamicLightHandlers.registerDynamicLightHandler(ModEntities.STAR_SHELL.get(), entity -> 15));
        Dragonrise_reforge.LOGGER.info("Dragonrise: star shell registered as dynamic light source");
    }

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
        event.registerBelowAll(
            Dragonrise_reforge.MODID + "_m270_ballistic",
            new M270BallisticOverlay()
        );
        event.registerBelowAll(
            Dragonrise_reforge.MODID + "_cannon_ballistic",
            new CannonBallisticOverlay()
        );
        Dragonrise_reforge.LOGGER.info("Dragonrise overlays registered");
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
