package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.compat.DynamicLightsCompat;
import com.redabysslucia.dragonrise_reforge.client.overlay.M270BallisticOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.SupplyProgressOverlay;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import com.redabysslucia.dragonrise_reforge.client.particle.SignalSmokeParticle;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModParticleTypes;
import com.redabysslucia.dragonrise_reforge.resource.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * 客户端事件处理器。
 *
 * <p>合并 1.20.1 时的取舍：
 * <ul>
 *   <li>GUI 用 NeoForge 1.21.1 的 {@link RegisterGuiLayersEvent}（Forge 时代的
 *       {@code RegisterGuiOverlaysEvent} 与 {@code IGuiOverlay} 在 1.21 已移除）。</li>
 *   <li>采纳 1.20.1 新增的照明弹粒子注册与动态光源注册。</li>
 *   <li>不再注册 {@code CannonBallisticOverlay}：间接瞄准射击系统已随 1.20.1
 *       的「新版本提交准备2」整体移除（连同 firecontrol 包与相关网络消息），
 *       该 overlay 类在本分支也已删除。</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    /** 照明弹粒子的 provider（1.20.1 侧新增）。 */
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.SIGNAL_SMOKE.get(), SignalSmokeParticle.Provider::new);
    }

    /**
     * 把照明弹实体注册为 Dynamic Lights 动态光源（亮度 15）。
     * 框架的实体光源集合按 DynamicLightHandlers 注册表填充（canLightUp/getLuminance），
     * 只靠 mixin 拦截帮助方法不够 —— 注册表里有条目才会把实体纳入光源集合。
     * （机制同 HandheldMoon 盈月的 Setup。）
     * <p>
     * 动态光影是软依赖（纯客户端可选 mod）：未安装时跳过注册，游戏照常运行，
     * 只是照明弹没有大范围补光效果。所有类引用都经 DynamicLightsCompat 隔离。
     */
    @SubscribeEvent
    public static void registerStarShellDynamicLight(FMLClientSetupEvent event) {
        if (!DynamicLightsCompat.isLoaded()) {
            Dragonrise_reforge.LOGGER.info(
                    "Dragonrise: dynamic lights ({}) not installed — star shell lighting disabled (soft dependency)",
                    DynamicLightsCompat.MOD_ID);
            return;
        }
        event.enqueueWork(DynamicLightsCompat::registerStarShellHandler);
        Dragonrise_reforge.LOGGER.info("Dragonrise: star shell registered as dynamic light source");
    }

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
