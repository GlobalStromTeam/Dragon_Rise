package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.TreeModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.resource.model.ArmorModelReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 护甲在物品栏 / 手持 / 掉落物状态下的 3D 模型渲染器（BER）。
 *
 * <p>写法与 SuperbWarfare 手写物品渲染器的方式对齐（参考 SBW 的
 * {@code HandGrenadeRenderer} / {@code ContainerBlockItemRenderer}）：
 * 直接继承 {@link BlockEntityWithoutLevelRenderer}，取 {@link TreeModelInstance}
 * 后 {@code translate(0.5, 0.5, 0.5)} + {@code instance.renderToBuffer(...)}。
 *
 * <p>注意：SBM 自己的 {@code AbstractGeoItemRendererV2} 在 GUI 槽位里走的是
 * {@code SlotModel} 平面图，会丢掉 3D 观感；这里按 SBW 的方式自己渲染，
 * 保证物品栏里看到的仍是 3D 模型（与改造前 GeckoLib 的行为一致）。
 */
public class SbmArmorItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final Map<ResourceLocation, SbmArmorItemRenderer> CACHE = new HashMap<>();

    private final ResourceLocation geoPath;
    private final ResourceLocation texture;
    private TreeBedrockModel cachedModel;
    private TreeModelInstance instance;

    private SbmArmorItemRenderer(ResourceLocation geoPath, ResourceLocation texture) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.geoPath = geoPath;
        this.texture = texture;
    }

    /**
     * @param geoPath 形如 {@code dragonrise_reforge:models/bedrock/armor/cn21helmet.geo.json}
     * @param texture 形如 {@code dragonrise_reforge:textures/armor/cn21helmet.png}
     * @return BER；模型尚未加载时返回 {@code null}，调用方回退到原版物品图标
     */
    @Nullable
    public static SbmArmorItemRenderer get(ResourceLocation geoPath, ResourceLocation texture) {
        if (ArmorModelReloadListener.INSTANCE.getModel(geoPath) == null) {
            return null;
        }
        return CACHE.computeIfAbsent(geoPath, key -> new SbmArmorItemRenderer(key, texture));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        TreeBedrockModel model = ArmorModelReloadListener.INSTANCE.getModel(this.geoPath);
        if (model == null) {
            return;
        }

        // 资源重载后模型实例会变，实例（含骨骼姿态缓存）需要跟着重建
        if (this.instance == null || this.cachedModel != model) {
            this.cachedModel = model;
            this.instance = model.createInstance();
        }

        this.instance.resetPose();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        this.instance.renderToBuffer(
                poseStack,
                bufferSource,
                RenderType.entityTranslucent(this.texture),
                BedrockModelRenderTypes.polyMeshCutout(this.texture),
                packedLight,
                packedOverlay);
        poseStack.popPose();
    }
}
