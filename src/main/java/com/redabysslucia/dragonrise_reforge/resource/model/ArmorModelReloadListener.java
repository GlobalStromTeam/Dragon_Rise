package com.redabysslucia.dragonrise_reforge.resource.model;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * 护甲 bedrock 模型的资源重载监听器。
 *
 * <p>产出 SBM v2 的 {@link TreeBedrockModel}，与 SuperbWarfare 的做法完全一致
 * （SBW 的 {@code ArmorModelReloadListener} 同样是 {@code TreeBedrockModel.bake(pojo)}）。
 *
 * <p>模型目录：{@code assets/dragonrise_reforge/models/bedrock/armor/*.geo.json}。
 * 护甲没有动画资源（原 38 个 GeckoLib GeoModel 的 getAnimationResource 全部返回 null），
 * 因此不再扫描 {@code animations/bedrock/armor}。
 */
public class ArmorModelReloadListener extends BedrockModelReloadListener<TreeBedrockModel> {

    public static final ArmorModelReloadListener INSTANCE = new ArmorModelReloadListener();

    private ArmorModelReloadListener() {
        super("models/bedrock/armor");
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        // 父类负责清空旧的 models / animations 缓存
        super.apply(map, resourceManager, profiler);
        for (Map.Entry<ResourceLocation, BedrockModelPOJO> entry : map.entrySet()) {
            this.models.put(entry.getKey(), TreeBedrockModel.bake(entry.getValue()));
        }
    }
}
