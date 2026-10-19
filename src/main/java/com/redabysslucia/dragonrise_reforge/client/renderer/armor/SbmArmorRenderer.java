package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.github.mcmodderanchor.simplebedrockmodel.v2.client.renderer.GeoArmorRendererV2;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import com.redabysslucia.dragonrise_reforge.resource.model.ArmorModelReloadListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 护甲「穿在身上」的 SBM v2 渲染器，写法与 SuperbWarfare 对齐
 * （SBW 的护甲物品直接 {@code new GeoArmorRendererV2(model, slot, texture)} + {@code preparePose}）。
 *
 * <p>相比直接内联，这里多做了两件事，避免 38 份重复代码和两个真实缺陷：
 * <ul>
 *   <li><b>按 geo 缓存</b>：同一件护甲只创建一个渲染器实例；</li>
 *   <li><b>资源重载感知</b>：{@link ArmorModelReloadListener} 重载后会产出新的
 *       {@link TreeBedrockModel} 实例，此处检测到实例变化就重建渲染器，
 *       否则 F3+T 之后护甲仍渲染旧模型（SBW 的内联写法就是固定缓存，没有这层处理）。</li>
 * </ul>
 *
 * <p>模型缺失（资源未加载完 / 贴图包没提供）时 {@link #get} 返回 {@code null}，
 * 调用方回退到原版护甲模型而不是崩溃。
 */
public class SbmArmorRenderer extends GeoArmorRendererV2 {

    private static final Map<ResourceLocation, SbmArmorRenderer> CACHE = new HashMap<>();

    private final ResourceLocation geoPath;
    private final TreeBedrockModel cachedModel;
    private final EquipmentSlot slot;

    private SbmArmorRenderer(ResourceLocation geoPath, TreeBedrockModel model, EquipmentSlot slot, ResourceLocation texture) {
        super(model, slot, texture);
        this.geoPath = geoPath;
        this.cachedModel = model;
        this.slot = slot;
    }

    /**
     * @param geoPath 形如 {@code dragonrise_reforge:models/bedrock/armor/cn21helmet.geo.json}
     * @param slot    该护甲部件对应的槽位
     * @param texture 形如 {@code dragonrise_reforge:textures/armor/cn21helmet.png}
     * @return 渲染器；模型尚未加载时返回 {@code null}
     */
    @Nullable
    public static SbmArmorRenderer get(ResourceLocation geoPath, EquipmentSlot slot, ResourceLocation texture) {
        TreeBedrockModel model = ArmorModelReloadListener.INSTANCE.getModel(geoPath);
        if (model == null) {
            return null;
        }

        SbmArmorRenderer cached = CACHE.get(geoPath);
        if (cached != null && cached.cachedModel == model && cached.slot == slot) {
            return cached;
        }

        SbmArmorRenderer created = new SbmArmorRenderer(geoPath, model, slot, texture);
        CACHE.put(geoPath, created);
        return created;
    }

    public ResourceLocation getGeoPath() {
        return this.geoPath;
    }

    /**
     * SBM v2 默认是 {@code armorCutoutNoCull}，但那会把贴图里的半透明像素渲染成不透明。
     * DRR 有 15 张护甲贴图含半透明像素（chinese_desert / cnchest / desert07_* / jungle07* /
     * kr06_* / ocean07_* / russian_soldier / ukrainian_soldier），沿用原 GeckoLib 版的
     * {@code entityTranslucent} 以保持观感一致。
     */
    @Override
    public RenderType getRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
