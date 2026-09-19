package com.redabysslucia.dragonrise_reforge.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin 配置插件：动态光影（软依赖）相关 mixin 的条件应用。
 * <p>
 * 背景：照明弹大范围补光的 3 个 mixin 直接注入 dynamiclights-reforged 的类
 * （{@code toni.sodiumdynamiclights.*} / {@code dev.lambdaurora.lambdynlights.api.*}）。
 * 该 mod 是<b>纯客户端可选</b>依赖：未安装时这些目标类不存在，若强行应用注入会失败。
 * <p>
 * 为什么不把 mixin 拆到单独配置文件（配 {@code required=false} / {@code mods.toml [[mixins]]}）：
 * 实测开发环境（ForgeGradle userdev）的启动参数只会带上<b>主</b> mixin 配置
 * （{@code --mixin.config} 只注册了 {@code dragonrise_reforge.mixins.json}），
 * 拆出去的那份在开发环境根本不会被加载 → 补光静默失效。
 * 因此这里统一用一个配置 + plugin 在运行时判断目标 mod 是否存在，开发/生产行为一致。
 * <p>
 * 判断时机：{@link #shouldApplyMixin} 发生在 mixin 被应用之前、目标类解析之前，
 * 未安装时直接跳过，既不报错也不影响其他 mixin。
 */
public class DragonriseMixinPlugin implements IMixinConfigPlugin {

    /** 动态光影 modId（dynamiclights-reforged） */
    private static final String DYNAMIC_LIGHTS_MOD_ID = "sodiumdynamiclights";

    /** 动态光影相关 mixin 的类名前缀（只对这些做条件判断） */
    private static final String DYNAMIC_LIGHTS_MIXIN_PREFIX =
            "com.redabysslucia.dragonrise_reforge.mixin.DynamicLights";

    /** 缓存判断结果（mod 列表在启动后不再变化） */
    private static Boolean dynamicLightsLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        // 无需预加载：判断延后到 shouldApplyMixin（此时 mod 列表必定已初始化）
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * 只有动态光影相关 mixin 需要条件判断；其余 mixin 一律应用。
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(DYNAMIC_LIGHTS_MIXIN_PREFIX)) {
            return isDynamicLightsLoaded();
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    /** 动态光影是否已加载（启动早期查 LoadingModList；查询失败时保守跳过，避免目标类缺失崩溃） */
    private static boolean isDynamicLightsLoaded() {
        if (dynamicLightsLoaded == null) {
            try {
                LoadingModList modList = LoadingModList.get();
                dynamicLightsLoaded = modList != null && modList.getModFileById(DYNAMIC_LIGHTS_MOD_ID) != null;
            } catch (Throwable t) {
                dynamicLightsLoaded = false;
            }
        }
        return dynamicLightsLoaded;
    }
}
