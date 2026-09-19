package com.redabysslucia.dragonrise_reforge.compat;

import net.minecraftforge.fml.ModList;

/**
 * 软依赖探测（<b>common 安全</b>：客户端与服务端均可调用）。
 * <p>
 * 与 {@code client.compat.DynamicLightsCompat} 的区别：本类不引用任何第三方 mod 类，
 * 只查 mod 列表，因此可以安全地用在两端都会执行的代码里（例如物品的 use / 创造栏填充）。
 * 需要真正调用动态光影 API 时用 client-only 的 DynamicLightsCompat。
 */
public final class SoftDependencies {

    /** 动态光影 modId（dynamiclights-reforged：同时提供 lambdynlights API 与 toni.sodiumdynamiclights 实现） */
    public static final String DYNAMIC_LIGHTS = "sodiumdynamiclights";

    private SoftDependencies() {
    }

    /**
     * 动态光影是否可用。
     * <p>
     * 注意这是<b>本端</b>的 mod 列表：动态光影是纯客户端 mod，专用服务器永远返回 false。
     * 正因如此，信号装备的可用性判断放在服务端（权威）—— 服务器上「没有动态光影就没有信号装备」，
     * 与客户端是否安装无关，避免两端行为不一致。
     */
    public static boolean hasDynamicLights() {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(DYNAMIC_LIGHTS);
    }
}
