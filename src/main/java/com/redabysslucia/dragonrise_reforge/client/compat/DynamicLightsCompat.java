package com.redabysslucia.dragonrise_reforge.client.compat;

import com.redabysslucia.dragonrise_reforge.compat.SoftDependencies;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

/**
 * 动态光影（dynamiclights-reforged / sodiumdynamiclights）<b>软依赖</b>兼容层。
 * <p>
 * 本 mod 的「照明弹大范围补光」完全建立在动态光影之上，但动态光影是<b>纯客户端</b>的可选 mod：
 * <ul>
 *   <li>专用服务器不会安装它；</li>
 *   <li>客户端可能装、也可能不装。</li>
 * </ul>
 * 因此所有对动态光影类的引用都集中在本类（client-only），调用前必须先
 * {@link #isLoaded()} 判断 —— 未安装时对应方法体永不执行，JVM 惰性解析不会加载那些类，
 * 从而避免 {@code NoClassDefFoundError} 让游戏崩溃。
 * <p>
 * 注意：<b>不能</b>用「是否装了动态光影」来决定物品/实体是否注册 ——
 * 那会导致客户端与服务端注册表不一致（联机被拒、旧存档实体无法加载）。
 * 注册表恒定，只有这一层纯客户端的光照集成按需启用。
 */
@OnlyIn(Dist.CLIENT)
public final class DynamicLightsCompat {

    /** 动态光影 mod 的 modId（见 {@link SoftDependencies#DYNAMIC_LIGHTS}） */
    public static final String MOD_ID = SoftDependencies.DYNAMIC_LIGHTS;

    private DynamicLightsCompat() {
    }

    /** 动态光影是否已安装（未初始化 mod 列表时返回 false，视为未安装） */
    public static boolean isLoaded() {
        return SoftDependencies.hasDynamicLights();
    }

    /**
     * 把照明弹实体注册为动态光源（亮度 15）。
     * 框架的实体光源集合按 DynamicLightHandlers 注册表填充（canLightUp/getLuminance），
     * 只有 mixin 拦截帮助方法不够 —— 注册表里有条目才会把实体纳入光源集合。
     */
    public static void registerStarShellHandler() {
        DynamicLightHandlers.registerDynamicLightHandler(ModEntities.STAR_SHELL.get(), entity -> 15);
    }

    /** 把实体从动态光源集合移除（照明弹消失时清理，避免残留光源） */
    public static void removeLightSource(Entity entity) {
        SodiumDynamicLights dynamicLights = SodiumDynamicLights.get();
        if (dynamicLights != null && entity instanceof DynamicLightSource lightSource) {
            dynamicLights.removeLightSource(lightSource);
        }
    }
}
