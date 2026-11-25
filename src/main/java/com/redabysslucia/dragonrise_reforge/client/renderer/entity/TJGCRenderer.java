package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * tjgc 渲染器：继承 {@link DragonriseVehicleRenderer} 以支持涂装自带模型 / 发光贴图。
 *
 * <p><b>发光贴图规则</b>
 * <ul>
 *   <li>默认涂装、{@code black}、{@code black1}：统一用 {@code textures/entity/glow/tjgc_glow.png}。
 *       这三者都没有自己的 {@code *_glow.png}，按父类命名约定会取不到发光贴图，所以在这里兜底。</li>
 *   <li>{@code skin} 涂装：自带 {@code tjgc_skin_glow.png}，走父类逻辑，不并入统一发光。</li>
 * </ul>
 */
public class TJGCRenderer extends DragonriseVehicleRenderer<TJGCEntity> {

    /** 默认 / black / black1 共用的发光贴图。 */
    private static final ResourceLocation TJGC_GLOW =
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/entity/glow/tjgc_glow.png");

    /** skin 涂装保持自己的发光贴图，不套用统一发光。 */
    private static final String SKIN_EXCLUDED = "skin";

    public TJGCRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getEmissiveTextureLocation(PoseStack poseStack, TJGCEntity entity) {
        if (SKIN_EXCLUDED.equals(entity.getSkinId())) {
            return super.getEmissiveTextureLocation(poseStack, entity);
        }
        return TJGC_GLOW;
    }
}
